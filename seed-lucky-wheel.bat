@echo off
echo ==================================
echo LUCKY WHEEL - SEED PRIZES DATA
echo ==================================
echo.

REM Database configuration
SET DB_HOST=localhost
SET DB_PORT=5433
SET DB_NAME=nike_store
SET DB_USER=postgres
SET PGPASSWORD=12345
SET SQL_FILE=db\lucky_wheel_init.sql

echo Database: %DB_NAME%
echo Host: %DB_HOST%:%DB_PORT%
echo SQL File: %SQL_FILE%
echo.

REM Check if SQL file exists
if not exist "%SQL_FILE%" (
    echo ERROR: SQL file not found: %SQL_FILE%
    pause
    exit /b 1
)

echo Running SQL script...
echo.

REM Run using docker if psql not available
docker exec -i nikestore-postgres psql -U %DB_USER% -d %DB_NAME% < %SQL_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! Prizes data has been seeded!
    echo.
    echo Summary:
    echo - 6 prizes added to database
    echo - Now you can test the Lucky Wheel in the app!
) else (
    echo.
    echo ERROR: Failed to seed data
    exit /b 1
)

echo.
pause
