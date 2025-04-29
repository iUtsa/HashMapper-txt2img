@echo off
setlocal enabledelayedexpansion

echo === Checking Java directory ===
echo.

set JAVA_DIR=%CD%\java
set LIB_DIR=%CD%\lib

if not exist "%JAVA_DIR%" (
    echo Error: Java directory not found: %JAVA_DIR%
    echo Please make sure the 'java' directory exists in the current directory.
    exit /b 1
)

echo Found Java files:
for %%F in ("%JAVA_DIR%\*.java") do (
    echo   - %%~nxF
)

echo.
echo === Checking library directory ===
echo.

if not exist "%LIB_DIR%" (
    echo Warning: Library directory not found: %LIB_DIR%
    echo Creating library directory...
    mkdir "%LIB_DIR%"
)

if not exist "%LIB_DIR%\jfreechart.jar" (
    echo JFreeChart JAR not found. Would you like to download it? (y/n)
    set /p DOWNLOAD=
    if /i "!DOWNLOAD!"=="y" (
        echo Downloading JFreeChart...
        powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar' -OutFile '%LIB_DIR%\jfreechart.jar'}"
        echo Downloading JCommon...
        powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/jfree/jcommon/1.0.24/jcommon-1.0.24.jar' -OutFile '%LIB_DIR%\jcommon.jar'}"
    )
)

echo.
echo === Compiling Java files ===
echo.

set CLASSPATH=%JAVA_DIR%;%LIB_DIR%\*

javac -cp "%CLASSPATH%" -d "%JAVA_DIR%" "%JAVA_DIR%\*.java"

if %ERRORLEVEL% EQU 0 (
    echo Java files compiled successfully!
    echo You can now run the Flask application with: python app.py
) else (
    echo Error: Failed to compile Java files.
    exit /b 1
)

endlocal