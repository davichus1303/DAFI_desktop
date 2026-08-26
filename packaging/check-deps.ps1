# DAFI Desktop - Dependency Installer for Windows
# Checks and installs Java 17+ and Maven 3.6+ if not present

param(
    [switch]$Silent,
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# Configuration
$JAVA_MIN_VERSION = 17
$MAVEN_MIN_VERSION = "3.6"
$INSTALL_DIR = "$env:LOCALAPPDATA\DAFI-Desktop"
$JAVA_DIR = "$INSTALL_DIR\jdk"
$MAVEN_DIR = "$INSTALL_DIR\maven"
$LOG_FILE = "$INSTALL_DIR\install.log"

# URLs
$JAVA_URL = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B11/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_11.msi"
$MAVEN_URL = "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    Write-Host $logMessage -ForegroundColor $(if($Level -eq "ERROR"){"Red"} elseif($Level -eq "WARN"){"Yellow"} else{"Green"})
    Add-Content -Path $LOG_FILE -Value $logMessage -ErrorAction SilentlyContinue
}

function Test-Admin {
    $currentUser = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($currentUser)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Get-JavaVersion {
    try {
        $javaExe = Get-Command java -ErrorAction SilentlyContinue
        if ($javaExe) {
            $output = & java -version 2>&1 | Select-Object -First 1
            if ($output -match 'version "([0-9]+)') {
                return [int]$Matches[1]
            }
        }
    } catch {
        Write-Log "Error checking Java version: $_" "WARN"
    }
    return 0
}

function Get-MavenVersion {
    try {
        $mvnExe = Get-Command mvn -ErrorAction SilentlyContinue
        if ($mvnExe) {
            $output = & mvn --version 2>&1 | Select-Object -First 1
            if ($output -match 'Maven ([0-9]+\.[0-9]+)') {
                return $Matches[1]
            }
        }
    } catch {
        Write-Log "Error checking Maven version: $_" "WARN"
    }
    return "0.0"
}

function Install-Java {
    Write-Log "Installing Java JDK 17..."
    
    if (-not (Test-Path $JAVA_DIR)) {
        New-Item -ItemType Directory -Path $JAVA_DIR -Force | Out-Null
    }
    
    $msiPath = "$INSTALL_DIR\java-installer.msi"
    
    try {
        Write-Log "Downloading Java from Adoptium..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $JAVA_URL -OutFile $msiPath -UseBasicParsing
        
        Write-Log "Installing Java (this may take a few minutes)..."
        $msiArgs = @(
            "/i", $msiPath,
            "/quiet",
            "/norestart",
            "INSTALLDIR=$JAVA_DIR",
            "ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome"
        )
        $process = Start-Process msiexec.exe -ArgumentList $msiArgs -Wait -PassThru -NoNewWindow
        
        if ($process.ExitCode -eq 0 -or $process.ExitCode -eq 3010) {
            Write-Log "Java installed successfully"
            
            # Update PATH for current session
            $javaBin = Get-ChildItem -Path $JAVA_DIR -Recurse -Filter "java.exe" | Select-Object -First 1
            if ($javaBin) {
                $javaHome = $javaBin.Directory.Parent.Parent.FullName
                $env:JAVA_HOME = $javaHome
                $env:Path = "$javaHome\bin;$env:Path"
                Write-Log "JAVA_HOME set to: $javaHome"
            }
            
            # Clean up installer
            Remove-Item -Path $msiPath -Force -ErrorAction SilentlyContinue
            return $true
        } else {
            Write-Log "Java installation failed with exit code: $($process.ExitCode)" "ERROR"
            return $false
        }
    } catch {
        Write-Log "Error installing Java: $_" "ERROR"
        Remove-Item -Path $msiPath -Force -ErrorAction SilentlyContinue
        return $false
    }
}

function Install-Maven {
    Write-Log "Installing Apache Maven..."
    
    if (-not (Test-Path $MAVEN_DIR)) {
        New-Item -ItemType Directory -Path $MAVEN_DIR -Force | Out-Null
    }
    
    $zipPath = "$INSTALL_DIR\maven.zip"
    
    try {
        Write-Log "Downloading Maven from Apache..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $MAVEN_URL -OutFile $zipPath -UseBasicParsing
        
        Write-Log "Extracting Maven..."
        Expand-Archive -Path $zipPath -DestinationPath $MAVEN_DIR -Force
        
        # Find the extracted directory
        $mavenHome = Get-ChildItem -Path $MAVEN_DIR -Directory | Select-Object -First 1
        
        if ($mavenHome) {
            # Update PATH for current session
            $env:MAVEN_HOME = $mavenHome.FullName
            $env:Path = "$($mavenHome.FullName)\bin;$env:Path"
            Write-Log "MAVEN_HOME set to: $($mavenHome.FullName)"
            
            # Clean up installer
            Remove-Item -Path $zipPath -Force -ErrorAction SilentlyContinue
            return $true
        } else {
            Write-Log "Maven extraction failed" "ERROR"
            return $false
        }
    } catch {
        Write-Log "Error installing Maven: $_" "ERROR"
        Remove-Item -Path $zipPath -Force -ErrorAction SilentlyContinue
        return $false
    }
}

function Set-EnvironmentVariables {
    Write-Log "Setting persistent environment variables..."
    
    # Set JAVA_HOME
    if ($env:JAVA_HOME) {
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $env:JAVA_HOME, "User")
        Write-Log "JAVA_HOME persisted: $($env:JAVA_HOME)"
    }
    
    # Add to PATH
    $userPath = [System.Environment]::GetEnvironmentVariable("PATH", "User")
    $pathsToAdd = @()
    
    if ($env:JAVA_HOME -and -not $userPath.Contains("$env:JAVA_HOME\bin")) {
        $pathsToAdd += "$env:JAVA_HOME\bin"
    }
    
    if ($env:MAVEN_HOME -and -not $userPath.Contains("$env:MAVEN_HOME\bin")) {
        $pathsToAdd += "$env:MAVEN_HOME\bin"
    }
    
    if ($pathsToAdd.Count -gt 0) {
        $newPath = ($pathsToAdd -join ";") + ";" + $userPath
        [System.Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
        Write-Log "PATH updated with new tools"
    }
}

function Show-Status {
    param([string]$Tool, [string]$Status, [string]$Version)
    $color = if($Status -eq "OK"){"Green"} elseif($Status -eq "MISSING"){"Yellow"} else{"Red"}
    Write-Host "  $Tool : $Status" -ForegroundColor $color -NoNewline
    if ($Version) {
        Write-Host " ($Version)" -ForegroundColor Gray
    } else {
        Write-Host ""
    }
}

# Main execution
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DAFI Desktop - Dependency Installer" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create install directory
if (-not (Test-Path $INSTALL_DIR)) {
    New-Item -ItemType Directory -Path $INSTALL_DIR -Force | Out-Null
}

# Initialize log
Write-Log "Starting dependency check..."

# Check current status
Write-Host "Checking dependencies..." -ForegroundColor White
Write-Host ""

$javaVersion = Get-JavaVersion
$mavenVersion = Get-MavenVersion

Show-Status "Java" $(if($javaVersion -ge $JAVA_MIN_VERSION){"OK"} elseif($javaVersion -gt 0){"OUTDATED"} else{"MISSING"}) $javaVersion
Show-Status "Maven" $(if([version]$mavenVersion -ge [version]$MAVEN_MIN_VERSION){"OK"} elseif($javaVersion -gt 0){"OUTDATED"} else{"MISSING"}) $mavenVersion

Write-Host ""

# Determine what needs to be installed
$needJava = $javaVersion -lt $JAVA_MIN_VERSION
$needMaven = [version]$mavenVersion -lt [version]$MAVEN_MIN_VERSION

if (-not $needJava -and -not $needMaven) {
    Write-Host "All dependencies are satisfied!" -ForegroundColor Green
    Write-Log "All dependencies OK"
    exit 0
}

# Check if running as admin (needed for Java MSI)
if ($needJava -and -not (Test-Admin)) {
    Write-Host ""
    Write-Host "WARNING: Java installation requires administrator privileges." -ForegroundColor Yellow
    Write-Host "The installer will request elevation automatically." -ForegroundColor Yellow
    Write-Host ""
}

# Install dependencies
$installErrors = @()

if ($needJava) {
    Write-Host "Installing Java..." -ForegroundColor Cyan
    if (-not (Install-Java)) {
        $installErrors += "Java installation failed"
    }
}

if ($needMaven) {
    Write-Host "Installing Maven..." -ForegroundColor Cyan
    if (-not (Install-Maven)) {
        $installErrors += "Maven installation failed"
    }
}

# Set environment variables
if ($installErrors.Count -eq 0) {
    Set-EnvironmentVariables
}

# Final status
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

if ($installErrors.Count -gt 0) {
    Write-Host "  Installation completed with errors:" -ForegroundColor Red
    $installErrors | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host ""
    Write-Host "  Please install manually:" -ForegroundColor Yellow
    Write-Host "  Java: https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "  Maven: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "  All dependencies installed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Java: $JAVA_MIN_VERSION+" -ForegroundColor White
    Write-Host "  Maven: $MAVEN_MIN_VERSION+" -ForegroundColor White
    Write-Host ""
    Write-Host "  Note: You may need to restart your terminal" -ForegroundColor Yellow
    Write-Host "  for environment variables to take effect." -ForegroundColor Yellow
    exit 0
}
