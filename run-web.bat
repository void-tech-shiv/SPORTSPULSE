@echo off
echo ========================================================
echo SPORTSPULSE - WEB SERVER RUN SCRIPT
echo ========================================================

java -cp "bin;lib\postgresql-42.7.14.jar" sportspulse.web.WebMain
pause
