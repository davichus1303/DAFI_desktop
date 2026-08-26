; DAFI Desktop - NSIS Installer
; Requires: NSIS 3.0+

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"

Name "DAFI Desktop"
OutFile "DAFI-Desktop-Installer.exe"
InstallDir "$LOCALAPPDATA\DAFI-Desktop"
InstallDirRegKey HKCU "Software\DAFI-Desktop" "InstallDir"
RequestExecutionLevel user

VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "DAFI Desktop"
VIAddVersionKey "CompanyName" "DAFI"
VIAddVersionKey "FileDescription" "DAFI Desktop Installer"
VIAddVersionKey "FileVersion" "1.0.0"

Var JavaFound

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "Spanish"

Function .onInit
    StrCpy $JavaFound "0"
FunctionEnd

Section "DAFI Desktop" SecMain
    SectionIn RO

    ; --- Check Java via simple file check ---
    DetailPrint "Verificando Java 21+..."
    IfFileExists "$PROGRAMFILES\Eclipse Adoptium\jdk-21*\bin\java.exe" 0 +3
        DetailPrint "Java encontrado (Adoptium)"
        StrCpy $JavaFound "1"

    ${If} $JavaFound == "0"
        IfFileExists "$PROGRAMFILES\Java\jdk-21*\bin\java.exe" 0 +3
            DetailPrint "Java encontrado (Oracle)"
            StrCpy $JavaFound "1"
    ${EndIf}

    ${If} $JavaFound == "0"
        IfFileExists "$PROGRAMFILES\Microsoft\jdk-21*\bin\java.exe" 0 +3
            DetailPrint "Java encontrado (Microsoft)"
            StrCpy $JavaFound "1"
    ${EndIf}

    ${If} $JavaFound == "0"
        MessageBox MB_YESNO "Java 21+ no encontrado.$\r$\n$\r$\nDesea descargar e instalar Java 21 automaticamente?$\r$\n(Si elige No, la app puede no funcionar)" IDYES DownloadJava IDNO SkipJava
    ${EndIf}
    Goto InstallApp

    DownloadJava:
        DetailPrint "Descargando Java 21..."
        CreateDirectory "$TEMP\dafi-java"
        nsExec::ExecToStack 'cmd /c curl -L -o "$TEMP\dafi-java\jdk21.msi" "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5%2B11/OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.msi"'
        Pop $0
        ${If} $0 != "0"
            MessageBox MB_OK "No se pudo descargar Java 21.$\r$\nInstale manualmente desde https://adoptium.net/"
            Goto SkipJava
        ${EndIf}
        DetailPrint "Instalando Java 21..."
        nsExec::ExecToStack 'cmd /c msiexec /i "$TEMP\dafi-java\jdk21.msi" ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet /norestart'
        Pop $0
        RMDir /r "$TEMP\dafi-java"
        ${If} $0 == "0"
            DetailPrint "Java 21 instalado correctamente"
        ${ElseIf} $0 == "3010"
            DetailPrint "Java 21 instalado (puede requerir reinicio)"
        ${Else}
            MessageBox MB_OK "Error al instalar Java. Continuando sin Java..."
        ${EndIf}

    SkipJava:
    DetailPrint "Continuando con la instalacion..."

    InstallApp:
    ; --- Install application files ---
    SetOutPath "$INSTDIR"
    DetailPrint "Instalando DAFI Desktop..."
    File /r "..\target\jpackage\DAFI-Desktop\*.*"

    ; --- Shortcuts point directly to jpackage launcher (no wrapper needed) ---
    CreateShortCut "$DESKTOP\DAFI Desktop.lnk" "$INSTDIR\DAFI-Desktop.exe" "" "$INSTDIR\app\DAFI-Desktop.ico"
    CreateDirectory "$SMPROGRAMS\DAFI Desktop"
    CreateShortCut "$SMPROGRAMS\DAFI Desktop\DAFI Desktop.lnk" "$INSTDIR\DAFI-Desktop.exe" "" "$INSTDIR\app\DAFI-Desktop.ico"
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

    DetailPrint "DAFI Desktop instalado correctamente!"
SectionEnd

Section "Uninstall"
    RMDir /r "$INSTDIR"
    RMDir /r "$SMPROGRAMS\DAFI Desktop"
    Delete "$DESKTOP\DAFI Desktop.lnk"
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DAFI-Desktop"
    DeleteRegKey HKCU "Software\DAFI-Desktop"
SectionEnd
