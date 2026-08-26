; DAFI Desktop - NSIS Installer
; Requires: NSIS 3.0+

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"

Name "DAFI Desktop"
OutFile "DAFI-Desktop-Installer.exe"
InstallDir "$LOCALAPPDATA\DAFI-Desktop"
InstallDirRegKey HKCU "Software\DAFI-Desktop" "InstallDir"
RequestExecutionLevel admin
Unicode True

VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "DAFI Desktop"
VIAddVersionKey "CompanyName" "DAFI"
VIAddVersionKey "FileDescription" "DAFI Desktop Installer"
VIAddVersionKey "FileVersion" "1.0.0"
VIAddVersionKey "LegalCopyright" "DAFI 2024"

Var JavaPath
Var NeedsJava
Var JAVA_HOME_VAL

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "Spanish"

Function DetectJava
    StrCpy $NeedsJava "1"
    ReadEnvStr $JAVA_HOME_VAL "JAVA_HOME"

    ${If} "$JAVA_HOME_VAL" != ""
        IfFileExists "$JAVA_HOME_VAL\bin\java.exe" 0 CheckPath
        StrCpy $JavaPath "$JAVA_HOME_VAL\bin\java.exe"
        StrCpy $NeedsJava "0"
    ${EndIf}

    CheckPath:
    ${If} $NeedsJava == "1"
        IfFileExists "C:\Program Files\Eclipse Adoptium\jdk-*\bin\java.exe" 0 CheckOracle
        FindFirst $0 $1 "C:\Program Files\Eclipse Adoptium\jdk-*\bin\java.exe"
        FindClose $0
        StrCpy $JavaPath "$1"
        StrCpy $NeedsJava "0"
    ${EndIf}

    CheckOracle:
    ${If} $NeedsJava == "1"
        IfFileExists "C:\Program Files\Java\jdk-*\bin\java.exe" 0 CheckDone
        FindFirst $0 $1 "C:\Program Files\Java\jdk-*\bin\java.exe"
        FindClose $0
        StrCpy $JavaPath "$1"
        StrCpy $NeedsJava "0"
    ${EndIf}

    CheckMicrosoft:
    ${If} $NeedsJava == "1"
        IfFileExists "C:\Program Files\Microsoft\jdk-*\bin\java.exe" 0 CheckDone
        FindFirst $0 $1 "C:\Program Files\Microsoft\jdk-*\bin\java.exe"
        FindClose $0
        StrCpy $JavaPath "$1"
        StrCpy $NeedsJava "0"
    ${EndIf}

    CheckDone:
FunctionEnd

Function InstallJava
    DetailPrint "Descargando Java 17 (Temurin)..."
    CreateDirectory "$TEMP\java-install"

    nsExec::ExecToStack 'cmd /c curl -L -o "$TEMP\java-install\jdk17.msi" "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.13%2B11/OpenJDK17U-jdk_x64_windows_hotspot_17.0.13_11.msi"'
    Pop $0

    ${If} $0 != "0"
        MessageBox MB_OK "No se pudo descargar Java 17. Instale manualmente desde https://adoptium.net/"
        Abort
    ${EndIf}

    DetailPrint "Instalando Java 17 (esto puede tardar unos minutos)..."
    nsExec::ExecToStack 'cmd /c msiexec /i "$TEMP\java-install\jdk17.msi" ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet /norestart'
    Pop $0

    ${If} $0 == "3010"
        DetailPrint "Java 17 instalado correctamente (puede requerir reinicio)"
    ${ElseIf} $0 == "0"
        DetailPrint "Java 17 instalado correctamente"
    ${Else}
        MessageBox MB_OK "Error al instalar Java. Codigo: $0"
        Abort
    ${EndIf}

    RMDir /r "$TEMP\java-install"
FunctionEnd

Section "DAFI Desktop" SecMain
    SectionIn RO

    ; --- Check and install Java ---
    DetailPrint "Verificando Java 17+..."
    Call DetectJava

    ${If} $NeedsJava == "1"
        DetailPrint "Java 17+ no encontrado. Instalando..."
        Call InstallJava
    ${Else}
        DetailPrint "Java encontrado: $JavaPath"
    ${EndIf}

    ; --- Install application ---
    SetOutPath "$INSTDIR"
    DetailPrint "Instalando DAFI Desktop..."
    File /r "..\target\jpackage\DAFI-Desktop\*.*"

    ; --- Create clean launcher (no terminal, no dependency check) ---
    FileOpen $0 "$INSTDIR\DAFI-Desktop-Launcher.bat" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'cd /d "%~dp0"$\r$\n'
    FileWrite $0 'start "" "app\DAFI-Desktop.bat"$\r$\n'
    FileClose $0

    ; --- Desktop shortcut ---
    CreateShortCut "$DESKTOP\DAFI Desktop.lnk" "$INSTDIR\DAFI-Desktop-Launcher.bat" "" "$INSTDIR\app\DAFI-Desktop.ico"

    ; --- Start Menu ---
    CreateDirectory "$SMPROGRAMS\DAFI Desktop"
    CreateShortCut "$SMPROGRAMS\DAFI Desktop\DAFI Desktop.lnk" "$INSTDIR\DAFI-Desktop-Launcher.bat" "" "$INSTDIR\app\DAFI-Desktop.ico"
    CreateShortCut "$SMPROGRAMS\DAFI Desktop\Desinstalar.lnk" "$INSTDIR\uninstall.exe"

    ; --- Registry ---
    WriteRegStr HKCU "Software\DAFI-Desktop" "InstallDir" "$INSTDIR"
    WriteUninstaller "$INSTDIR\uninstall.exe"

    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "DisplayName" "DAFI Desktop"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "UninstallString" '"$INSTDIR\uninstall.exe"'
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "InstallLocation" "$INSTDIR"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "DisplayVersion" "1.0.0"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "Publisher" "DAFI"
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "NoModify" 1
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "NoRepair" 1

    ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
    IntFmt $0 "0x%08X" $0
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop" "EstimatedSize" "$0"

    DetailPrint "DAFI Desktop instalado correctamente!"
SectionEnd

Section "Uninstall"
    RMDir /r "$INSTDIR"
    RMDir /r "$SMPROGRAMS\DAFI Desktop"
    Delete "$DESKTOP\DAFI Desktop.lnk"
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop"
    DeleteRegKey HKCU "Software\DAFI-Desktop"
SectionEnd
