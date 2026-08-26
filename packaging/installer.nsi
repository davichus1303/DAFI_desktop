; DAFI Desktop - NSIS Installer with Dependency Check
; Requires: NSIS 3.0+

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"
!include "x64.nsh"

; ─── Configuration ───────────────────────────────────────────────────────────
Name "DAFI Desktop"
OutFile "DAFI-Desktop-Installer.exe"
InstallDir "$LOCALAPPDATA\DAFI-Desktop"
InstallDirRegKey HKCU "Software\DAFI-Desktop" "InstallDir"
RequestExecutionLevel admin
Unicode True

; ─── Version Info ────────────────────────────────────────────────────────────
VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "DAFI Desktop"
VIAddVersionKey "CompanyName" "DAFI"
VIAddVersionKey "FileDescription" "DAFI Desktop Installer"
VIAddVersionKey "FileVersion" "1.0.0"
VIAddVersionKey "ProductVersion" "1.0.0"

; ─── Variables ───────────────────────────────────────────────────────────────
Var JavaPath
Var NeedsJava

; ─── Pages ───────────────────────────────────────────────────────────────────
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "Spanish"

; ─── Macros ──────────────────────────────────────────────────────────────────
Function DetectJava
    ; Check JAVA_HOME first
    ${If} "$JAVA_HOME" != ""
        IfFileExists "$JAVA_HOME\bin\java.exe" 0 CheckPath
        StrCpy $JavaPath "$JAVA_HOME\bin\java.exe"
        StrCpy $NeedsJava "0"
        ${Return}
    ${EndIf}

    CheckPath:
    ; Check PATH
    nsExec::ExecToStack 'cmd /c where java'
    Pop $0
    ${If} $0 == "0"
        Pop $1
        StrCpy $JavaPath "$1"
        StrCpy $NeedsJava "0"
        ${Return}
    ${EndIf}

    ; Check common install locations
    ${IfFileExists "C:\Program Files\Eclipse Adoptium\jdk-*\bin\java.exe" FoundJava NotInAdoptium
    FoundJava:
        FindFirst $0 $1 "C:\Program Files\Eclipse Adoptium\jdk-*\bin\java.exe"
        StrCpy $JavaPath "$1"
        StrCpy $NeedsJava "0"
        ${Return}
    NotInAdoptium:

    ${IfFileExists "C:\Program Files\Java\jdk-*\bin\java.exe" FoundOracle NotInOracle
    FoundOracle:
        FindFirst $0 $1 "C:\Program Files\Java\jdk-*\bin\java.exe"
        StrCpy $JavaPath "$1"
        StrCpy $NeedsJava "0"
        ${Return}
    NotInOracle:

    ; Not found
    StrCpy $NeedsJava "1"
FunctionEnd

Function InstallJava
    DetailPrint "Downloading Java 17 (Temurin)..."

    ; Create temp directory
    CreateDirectory "$TEMP\java-install"

    ; Download Adoptium JDK 17 MSI
    nsExec::ExecToStack 'cmd /c curl -L -o "$TEMP\java-install\jdk17.msi" "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_windows_hotspot_17.0.13_11.msi"'
    Pop $0

    ${If} $0 != "0"
        MessageBox MB_OK|MB_ICONERROR "Error downloading Java. Please install Java 17+ manually from https://adoptium.net/"
        Abort
    ${EndIf}

    DetailPrint "Installing Java 17 (this may take a few minutes)..."
    nsExec::ExecToStack 'cmd /c msiexec /i "$TEMP\java-install\jdk17.msi" ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet /norestart'
    Pop $0

    ${If} $0 != "0"
        ${If} $0 == "3010"
            DetailPrint "Java installed successfully (restart may be required)"
        ${Else}
            MessageBox MB_OK|MB_ICONERROR "Error installing Java. Exit code: $0"
            Abort
        ${EndIf}
    ${Else}
        DetailPrint "Java installed successfully"
    ${EndIf}

    ; Refresh environment
    System::Call 'kernel32::SetEnvironmentVariable(t "JAVA_HOME", t "C:\Program Files\Eclipse Adoptium\jdk-17*" )i'

    ; Clean up
    RMDir /r "$TEMP\java-install"
FunctionEnd

; ─── Installer Sections ──────────────────────────────────────────────────────
Section "DAFI Desktop" SecMain
    SectionIn RO

    ; Detect Java
    DetailPrint "Checking Java installation..."
    Call DetectJava

    ${If} $NeedsJava == "1"
        DetailPrint "Java 17+ not found. Installing..."
        Call InstallJava
    ${Else}
        DetailPrint "Java found at: $JavaPath"
    ${EndIf}

    ; Set output path
    SetOutPath "$INSTDIR"

    ; Install application files
    DetailPrint "Installing DAFI Desktop..."
    File /r "target\jpackage\DAFI-Desktop\*.*"

    ; Install dependency scripts
    SetOutPath "$INSTDIR\scripts"
    File "packaging\check-deps.ps1"

    ; Install launcher
    SetOutPath "$INSTDIR"
    File "packaging\DAFI-Desktop.bat"

    ; Create launcher with dependency check
    FileOpen $0 "$INSTDIR\Launch-DAFI.bat" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'echo ==========================================$\r$\n'
    FileWrite $0 'echo   DAFI Desktop - Iniciando...$\r$\n'
    FileWrite $0 'echo ==========================================$\r$\n'
    FileWrite $0 'echo.$\r$\n'
    FileWrite $0 'echo Verificando dependencias...$\r$\n'
    FileWrite $0 'echo.$\r$\n'
    FileWrite $0 'powershell -ExecutionPolicy Bypass -File "%~dp0scripts\check-deps.ps1" -Silent$\r$\n'
    FileWrite $0 'if %ERRORLEVEL% neq 0 ($\r$\n'
    FileWrite $0 '    echo.$\r$\n'
    FileWrite $0 '    echo ADVERTENCIA: Algunas dependencias pueden no estar instaladas.$\r$\n'
    FileWrite $0 '    echo La aplicacion puede no funcionar sin Java 17+.$\r$\n'
    FileWrite $0 '    echo.$\r$\n'
    FileWrite $0 '    pause$\r$\n'
    FileWrite $0 ')$\r$\n'
    FileWrite $0 'echo.$\r$\n'
    FileWrite $0 'echo Iniciando DAFI Desktop...$\r$\n'
    FileWrite $0 'echo.$\r$\n'
    FileWrite $0 'call "%~dp0DAFI-Desktop.bat"$\r$\n'
    FileClose $0

    ; Create Start Menu shortcuts
    CreateDirectory "$SMPROGRAMS\DAFI Desktop"
    CreateShortCut "$SMPROGRAMS\DAFI Desktop\DAFI Desktop.lnk" "$INSTDIR\Launch-DAFI.bat" "" "$INSTDIR\app\DAFI-Desktop.ico"
    CreateShortCut "$SMPROGRAMS\DAFI Desktop\Desinstalar.lnk" "$INSTDIR\uninstall.exe"

    ; Create Desktop shortcut
    CreateShortCut "$DESKTOP\DAFI Desktop.lnk" "$INSTDIR\Launch-DAFI.bat" "" "$INSTDIR\app\DAFI-Desktop.ico"

    ; Store install path
    WriteRegStr HKCU "Software\DAFI-Desktop" "InstallDir" "$INSTDIR"

    ; Create uninstaller
    WriteUninstaller "$INSTDIR\uninstall.exe"

    ; Add to Add/Remove Programs
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "DisplayName" "DAFI Desktop"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "UninstallString" '"$INSTDIR\uninstall.exe"'
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "InstallLocation" "$INSTDIR"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "DisplayVersion" "1.0.0"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "Publisher" "DAFI"
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "NoModify" 1
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "NoRepair" 1

    ; Calculate install size
    ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
    IntFmt $0 "0x%08X" $0
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "EstimatedSize" "$0"

    DetailPrint "DAFI Desktop installed successfully!"
SectionEnd

; ─── Uninstaller Section ─────────────────────────────────────────────────────
Section "Uninstall"
    ; Remove files
    RMDir /r "$INSTDIR"

    ; Remove shortcuts
    RMDir /r "$SMPROGRAMS\DAFI Desktop"
    Delete "$DESKTOP\DAFI Desktop.lnk"

    ; Remove registry keys
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop"
    DeleteRegKey HKCU "Software\DAFI-Desktop"

    DetailPrint "DAFI Desktop uninstalled successfully."
SectionEnd
