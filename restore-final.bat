@echo off
chcp 65001 >nul
echo ========================================
echo    RESTORE NIKE STORE DATABASE
echo    FROM: nike_store_export_20260101_160831.sql
echo ========================================
echo.

cd /d "%~dp0"

set SQL_FILE=nike_store_export_20260101_160831.sql

if not exist "%SQL_FILE%" (
    echo ERROR: File %SQL_FILE% not found!
    pause
    exit /b 1
)

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
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();" >nul 2>&1
echo OK
echo.

echo [3/6] Recreating database...
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;" 2>nul
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH ENCODING = 'UTF8' TEMPLATE = template0;"
if errorlevel 1 (
    echo ERROR: Cannot create database!
    pause
    exit /b 1
)
echo OK - Fresh database created
echo.

echo [4/6] Copying SQL file to container...
docker cp "%SQL_FILE%" cps_postgres:/tmp/original.sql
if errorlevel 1 (
    echo ERROR: Cannot copy file!
    pause
    exit /b 1
)
echo OK - File copied
echo.

echo [5/6] Removing BOM and preparing file...
docker exec cps_postgres bash -c "dos2unix /tmp/original.sql 2>/dev/null; sed '1s/^\xEF\xBB\xBF//; 1s/^\xFF\xFE//' /tmp/original.sql | sed 's/\r$//' > /tmp/restore.sql" 2>nul
if errorlevel 1 (
    echo Trying alternative method...
    docker exec cps_postgres bash -c "tail -c +4 /tmp/original.sql > /tmp/restore.sql" 2>nul
    if errorlevel 1 (
        docker exec cps_postgres cp /tmp/original.sql /tmp/restore.sql
    )
)
echo OK - File prepared
echo.

echo [6/6] Restoring database...
echo This will take 1-2 minutes, please wait...
docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/restore.sql 2>&1 | findstr /V "WARNING:"
echo.

echo [7/7] Verifying restored data...
echo.
echo Tables created:
docker exec cps_postgres psql -U cps_user -d cps_db -c "\dt" 2>nul
echo.
echo Data counts:
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT 'Products' as table_name, COUNT(*)::text as records FROM product UNION ALL SELECT 'Users', COUNT(*)::text FROM users UNION ALL SELECT 'Orders', COUNT(*)::text FROM orders UNION ALL SELECT 'Categories', COUNT(*)::text FROM category UNION ALL SELECT 'Reviews', COUNT(*)::text FROM reviews UNION ALL SELECT 'Carts', COUNT(*)::text FROM carts UNION ALL SELECT 'Favorites', COUNT(*)::text FROM favorites UNION ALL SELECT 'Notifications', COUNT(*)::text FROM notifications UNION ALL SELECT 'Chat Messages', COUNT(*)::text FROM chat_messages;" 2>nul
echo.

echo ========================================
echo    DATABASE RESTORED SUCCESSFULLY!
echo ========================================
echo.
echo Your Nike Store database is ready!
echo You can now start the Spring Boot application.
echo.
pause
