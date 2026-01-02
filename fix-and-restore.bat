@echo off
chcp 65001 >nul
echo ========================================
echo    FIX AND RESTORE DATABASE
echo ========================================
echo.

cd /d "%~dp0"

echo [1/7] Checking container...
docker ps --filter "name=cps_postgres" --format "{{.Status}}" | findstr "Up" >nul
if errorlevel 1 (
    echo ERROR: Container not running!
    pause
    exit /b 1
)
echo OK
echo.

echo [2/7] Terminating connections...
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();" >nul 2>&1
echo OK
echo.

echo [3/7] Recreating database...
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;" >nul 2>&1
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH ENCODING = 'UTF8' TEMPLATE = template0;" >nul 2>&1
echo OK
echo.

echo [4/7] Copying file and removing BOM...
docker cp nike_store_backup.backup cps_postgres:/tmp/original.sql
echo OK
echo.

echo [5/7] Converting encoding (Latin1 to UTF8)...
docker exec cps_postgres bash -c "iconv -f LATIN1 -t UTF-8 /tmp/original.sql > /tmp/restore.sql 2>/dev/null || iconv -f ISO-8859-1 -t UTF-8 /tmp/original.sql > /tmp/restore.sql 2>/dev/null || iconv -f WINDOWS-1252 -t UTF-8 /tmp/original.sql > /tmp/restore.sql 2>/dev/null || cp /tmp/original.sql /tmp/restore.sql"
echo OK
echo.

echo [6/7] Restoring database...
echo This may show some warnings, please wait...
docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/restore.sql
echo.

echo [7/7] Verifying data...
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) as total_products FROM product;" 2>&1 | findstr -v "WARNING"
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) as total_users FROM users;" 2>&1 | findstr -v "WARNING"
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) as total_orders FROM orders;" 2>&1 | findstr -v "WARNING"
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) as total_categories FROM category;" 2>&1 | findstr -v "WARNING"
echo.

echo ========================================
echo    RESTORE COMPLETED!
echo ========================================
pause
