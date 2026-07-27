@echo off
chcp 65001 >nul
echo ========================================
echo    RESTORE DATABASE FROM BACKUP
echo ========================================
echo.

cd /d "%~dp0"

echo [1/6] Checking container...
docker ps --filter "name=cps_postgres" --format "{{.Status}}"
if errorlevel 1 (
    echo ERROR: Container not found!
    pause
    exit /b 1
)
echo OK - Container is running
echo.

echo [2/6] Fixing template database collation...
docker exec cps_postgres psql -U cps_user -d postgres -c "ALTER DATABASE template1 REFRESH COLLATION VERSION;"
echo.

echo [3/6] Terminating active connections...
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();"
echo.

echo [4/6] Creating fresh database...
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;"
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH TEMPLATE = template0 ENCODING = 'UTF8';"
if errorlevel 1 (
    echo ERROR: Cannot create database!
    pause
    exit /b 1
)
echo OK - Database created
echo.

echo [4/6] Copying backup file to container...
docker exec cps_postgres rm -f /tmp/backup.backup 2>nul
docker cp nike_store_backup.backup cps_postgres:/tmp/backup.backup
if errorlevel 1 (
    echo ERROR: Cannot copy backup file!
    pause
    exit /b 1
)
echo OK - File copied
echo.

echo [5/6] Checking backup file format...
docker exec cps_postgres file /tmp/backup.backup
echo.

echo [6/6] Restoring database...
echo Trying pg_restore (custom format)...
docker exec cps_postgres pg_restore -U cps_user -d cps_db -v /tmp/backup.backup 2>nul
if errorlevel 1 (
    echo Failed. Trying plain SQL format...
    docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/backup.backup 2>nul
    if errorlevel 1 (
        echo Failed. File may be corrupted or wrong format.
        echo.
        echo ========================================
        echo    FILE BACKUP INVALID!
        echo ========================================
        echo.
        echo The backup file appears to be corrupted or in wrong format.
        echo Please ask the sender to re-export the database using:
        echo.
        echo   pg_dump -U cps_user -Fc -f nike_store_backup.backup cps_db
        echo.
        echo Or export as plain SQL:
        echo   pg_dump -U cps_user -f nike_store_backup.sql cps_db
        echo.
        pause
        exit /b 1
    )
)
echo.

echo [7/7] Verifying data...
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT 'Products: ' || COUNT(*)::text FROM product UNION ALL SELECT 'Users: ' || COUNT(*)::text FROM users UNION ALL SELECT 'Orders: ' || COUNT(*)::text FROM orders UNION ALL SELECT 'Categories: ' || COUNT(*)::text FROM category;"
echo.

echo ========================================
echo    RESTORE COMPLETED!
echo ========================================
pause
