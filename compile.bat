@echo off
setlocal

echo ========================================================
echo SPORTSPULSE - COMPILE SCRIPT
echo ========================================================

if not exist "bin" mkdir bin
if not exist "lib" mkdir lib

set "PG_JAR=lib\postgresql-42.7.14.jar"
if not exist "%PG_JAR%" (
    echo Downloading PostgreSQL JDBC driver...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar' -OutFile '%PG_JAR%'"
    if errorlevel 1 (
        echo Failed to download PostgreSQL JDBC driver.
        pause
        exit /b 1
    )
)

echo Compiling project...
javac -cp "%PG_JAR%" -d bin src\sportspulse\*.java src\sportspulse\models\*.java src\sportspulse\utils\*.java src\sportspulse\db\*.java src\sportspulse\dao\*.java src\sportspulse\cli\*.java src\sportspulse\algorithms\*.java src\sportspulse\web\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
pause
endlocal
