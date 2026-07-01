@echo off
REM Ping Log Monitor - build normal-form file from a folder of raw logs
REM Usage: run_build.bat <root_folder> [output_file]
cd /d "%~dp0"
if not exist venv (
    echo First run: creating a virtual environment and installing dependencies...
    python -m venv venv
    call venv\Scripts\activate.bat
    pip install -r requirements.txt
) else (
    call venv\Scripts\activate.bat
)

set ROOT=%1
set OUT=%2
if "%ROOT%"=="" (
    echo Usage: run_build.bat ^<root_folder^> [output_file]
    pause
    exit /b 1
)
if "%OUT%"=="" set OUT=PingLogs_NormalForm.txt

python cli.py build --root "%ROOT%" --out "%OUT%" --verbose
pause
