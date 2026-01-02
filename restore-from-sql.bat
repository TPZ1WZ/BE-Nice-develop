@echo off
chcp 65001 >nul
echo ========================================
echo    RESTORE FROM SQL FILE
echo ========================================
echo.

cd /d "%~dp0"

set SQL_FILE=nike_store_backup.sql

if not exist "%SQL_FILE%" (
    echo ERROR: File %SQL_FILE% not found!
    echo.
    echo Please make sure you have nike_store_backup.sql file
    pause
    exit /b 1
)

echo [1/5] Terminating active connections...
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();"
echo.

echo [2/5] Recreating database...
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;"
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH TEMPLATE = template0 ENCODING = 'UTF8';"
echo OK
echo.

echo [3/5] Copying SQL file to container...
docker cp %SQL_FILE% cps_postgres:/tmp/restore.sql
echo OK
echo.

echo [4/5] Restoring from SQL...
docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/restore.sql
echo.

echo [5/5] Verifying data...
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT 'Products: ' || COUNT(*)::text FROM product UNION ALL SELECT 'Users: ' || COUNT(*)::text FROM users UNION ALL SELECT 'Orders: ' || COUNT(*)::text FROM orders;"
echo.

echo ========================================
echo    RESTORE COMPLETED!
echo ========================================
pause
