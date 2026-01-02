@echo off
chcp 65001 >nul
echo ========================================
echo    FIX ENCODING - ADVANCED METHOD
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] Fixing categories with exact replacements...

REM Fix "Giày trẻ em"
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE category SET name = 'Giày trẻ em' WHERE name LIKE 'Gi%%y tr%%em';" 2>nul

REM Fix "Giày bóng rổ"
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE category SET name = 'Giày bóng rổ' WHERE name LIKE 'Gi%%y b%%ng r%%';" 2>nul

REM Fix "Phụ kiện"
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE category SET name = 'Phụ kiện' WHERE name LIKE 'Ph%%ki%%n';" 2>nul

REM Fix "Giày thể thao"
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE category SET name = 'Giày thể thao' WHERE name LIKE 'Gi%%y th%%thao';" 2>nul

echo OK
echo.

echo [2/3] Fixing products...

REM Fix common patterns in products
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE product SET name = regexp_replace(name, 'Gi[^a-zA-Z0-9]+y', 'Giày', 'g');" 2>nul
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE product SET name = regexp_replace(name, 'tr[^a-zA-Z0-9]+', 'trẻ ', 'g');" 2>nul
docker exec cps_postgres psql -U cps_user -d cps_db -c "UPDATE product SET name = regexp_replace(name, 'Ph[^a-zA-Z0-9]+', 'Phụ ', 'g');" 2>nul

echo OK
echo.

echo [3/3] Checking results...
echo.
echo Categories:
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT id, name FROM category ORDER BY id LIMIT 15;" 2>nul
echo.
echo Products:
docker exec cps_postgres psql -U cps_user -d cps_db -c "SELECT id, name FROM product ORDER BY id LIMIT 10;" 2>nul
echo.

echo ========================================
echo    FIX COMPLETED!
echo ========================================
echo.
pause
