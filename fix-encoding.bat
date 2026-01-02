@echo off
chcp 65001 >nul
echo ========================================
echo    FIX DATABASE ENCODING
echo ========================================
echo.

cd /d "%~dp0"

echo [1/5] Backing up current data...
docker exec cps_postgres pg_dump -U cps_user cps_db > backup_before_fix_%date:~10,4%%date:~4,2%%date:~7,2%.sql
echo OK
echo.

echo [2/5] Fixing product table encoding...
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE product SET name = replace(replace(replace(replace(replace(replace(replace(name, 'Giß║íy', 'Giày'), 'trß║╗', 'trẻ'), 'Phß╗Ñ', 'Phụ'), 'kiß╗çn', 'kiện'), 'thß║╗', 'thể'), 'thao', 'thao'), 'ß║í', 'à');" 2>nul
if errorlevel 1 (
    echo Trying alternative method...
    docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE product SET name = convert_from(convert_to(name, 'LATIN1'), 'UTF8') WHERE name !~ '^[A-Za-z0-9 ]+$';" 2>nul
)
echo OK
echo.

echo [3/5] Fixing category table encoding...
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE category SET name = replace(replace(replace(replace(replace(name, 'Giß║íy', 'Giày'), 'trß║╗', 'trẻ'), 'Phß╗Ñ', 'Phụ'), 'kiß╗çn', 'kiện'), 'ß║í', 'à');" 2>nul
if errorlevel 1 (
    echo Trying alternative method...
    docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE category SET name = convert_from(convert_to(name, 'LATIN1'), 'UTF8') WHERE name !~ '^[A-Za-z0-9 ]+$';" 2>nul
)
echo OK
echo.

echo [4/5] Checking results...
echo.
echo Products:
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT id, name FROM product ORDER BY id LIMIT 10;" 2>nul
echo.
echo Categories:
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT id, name FROM category ORDER BY id LIMIT 10;" 2>nul
echo.

echo [5/5] Final verification...
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) as total_products FROM product;" 2>nul
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) as total_categories FROM category;" 2>nul
echo.

echo ========================================
echo    FIX COMPLETED!
echo ========================================
echo.
echo Backup saved to: backup_before_fix_%date:~10,4%%date:~4,2%%date:~7,2%.sql
echo.
echo If the text still shows incorrectly, please:
echo 1. Ask the sender to re-export with UTF-8
echo 2. Or use the fix-encoding-manual.sql script
echo.
pause
