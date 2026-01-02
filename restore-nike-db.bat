@echo off
chcp 65001 >nul
echo ========================================
echo    RESTORE NIKE STORE DATABASE
echo ========================================
echo.

cd /d "%~dp0"

echo [1/6] Checking container...
docker ps --filter "name=cps_postgres" --format "{{.Status}}" | findstr "Up" >nul
if errorlevel 1 (
    echo ERROR: Container not running! Starting...
    docker-compose up -d postgres
    timeout /t 5 /nobreak >nul
)
echo OK - Container is running
echo.

echo [2/6] Terminating active connections...
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();" 2>nul
echo.

echo [3/6] Recreating database...
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;" 2>nul
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH ENCODING = 'UTF8' TEMPLATE = template0;"
if errorlevel 1 (
    echo ERROR: Cannot create database!
    pause
    exit /b 1
)
echo OK - Database created
echo.

echo [4/6] Copying backup file to container...
docker cp nike_store_backup.backup cps_postgres:/tmp/restore.sql
if errorlevel 1 (
    echo ERROR: Cannot copy file!
    pause
    exit /b 1
)
echo OK - File copied
echo.

echo [5/6] Restoring database (this may take a while)...
docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/restore.sql
if errorlevel 1 (
    echo WARNING: Some errors occurred during restore
    echo.
)
echo.

echo [6/6] Verifying restored data...
echo.
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT table_name, n_live_tup as row_count FROM pg_stat_user_tables ORDER BY n_live_tup DESC;"
echo.

echo ========================================
echo    DATABASE RESTORED SUCCESSFULLY!
echo ========================================
echo.
echo You can now start your Spring Boot application.
echo.
pause
