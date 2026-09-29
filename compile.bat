@echo off
echo ========================================================
echo SPORTSPULSE - COMPILE SCRIPT
echo ========================================================

if not exist "bin" mkdir bin

echo Compiling project...
javac -d bin src\sportspulse\*.java src\sportspulse\models\*.java src\sportspulse\utils\*.java src\sportspulse\dao\*.java src\sportspulse\cli\*.java src\sportspulse\algorithms\*.java src\sportspulse\web\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
pause
