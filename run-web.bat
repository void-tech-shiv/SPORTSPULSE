@echo off
echo ========================================================
echo SPORTSPULSE - WEB SERVER RUN SCRIPT
echo ========================================================

echo DATABASE_URL configured: %DATABASE_URL:~0,0%
java -cp "bin;lib\postgresql-42.7.14.jar" sportspulse.web.WebMain
pause
