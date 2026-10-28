@echo off
chcp 65001 >nul
cd /d %~dp0
set JDK=%USERPROFILE%\.jdks\ms-17.0.20.1
if not exist "%JDK%\bin\javac.exe" set JDK=%USERPROFILE%\.jdks\openjdk-27
rmdir /s /q out 2>nul
mkdir out
dir /s /b src\*.java test\*.java > sources.txt
"%JDK%\bin\javac.exe" -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (echo ОШИБКА КОМПИЛЯЦИИ & pause & exit /b 1)
"%JDK%\bin\java.exe" -Dfile.encoding=UTF-8 -cp out ru.grapheditor.TestRunner
pause
