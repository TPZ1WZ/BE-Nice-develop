@echo off
chcp 65001 >nul
echo ========================================
echo    RESTORE WITH SQL_ASCII ENCODING
echo ========================================
echo.

cd /d "%~dp0"

echo [1/5] Terminating connections...
docker exec cps_postgres psql -U cps_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'cps_db' AND pid <> pg_backend_pid();" >nul 2>&1
echo OK
echo.

echo [2/5] Creating database with SQL_ASCII encoding...
docker exec cps_postgres psql -U cps_user -d postgres -c "DROP DATABASE IF EXISTS cps_db;" >nul 2>&1
docker exec cps_postgres psql -U cps_user -d postgres -c "CREATE DATABASE cps_db WITH ENCODING = 'SQL_ASCII' TEMPLATE = template0;"
echo OK
echo.

echo [3/5] Copying backup file...
docker cp nike_store_backup.backup cps_postgres:/tmp/restore.sql
echo OK
echo.

echo [4/5] Restoring database (may take 1-2 minutes)...
docker exec cps_postgres psql -U cps_user -d cps_db -f /tmp/restore.sql
echo.

echo [5/5] Checking results...
docker exec cps_postgres psql -U cps_user -d cps_db -c "\dt" 2>&1 | findstr -v "WARNING"
echo.
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT 'Products: ' || COUNT(*) FROM product UNION ALL SELECT 'Users: ' || COUNT(*) FROM users UNION ALL SELECT 'Orders: ' || COUNT(*) FROM orders UNION ALL SELECT 'Categories: ' || COUNT(*) FROM category;" 2>&1 | findstr -v "WARNING"
echo.

echo ========================================
echo    RESTORE COMPLETED!
echo ========================================
echo.
echo Note: Database is using SQL_ASCII encoding.
echo This should work for now, but consider re-exporting 
echo with proper UTF-8 encoding in the future.
echo.
pause
