@echo off
echo ========================================================
echo SPORTSPULSE - BUILD AND RUN SCRIPT
echo ========================================================

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile all java files
echo Compiling project...
javac -d bin src\sportspulse\*.java src\sportspulse\models\*.java src\sportspulse\utils\*.java src\sportspulse\cli\*.java src\sportspulse\algorithms\*.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo.
echo Starting SportsPulse...
echo.

REM Run the application
java -cp bin sportspulse.Main

pause
