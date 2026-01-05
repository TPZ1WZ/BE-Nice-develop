# Run Lucky Wheel Migration
Write-Host "=== Running Lucky Wheel Migration ===" -ForegroundColor Cyan

$dbHost = "localhost"
$dbPort = "5432"
$dbName = "cps_db"
$dbUser = "cps_user"
$dbPass = "cps_pass"
$sqlFile = "db\lucky_wheel_migration.sql"

# Check if SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "Error: SQL file not found: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "Reading SQL file: $sqlFile" -ForegroundColor Yellow
$sqlContent = Get-Content -Path $sqlFile -Raw -Encoding UTF8

# Try using psql if available
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if ($psqlPath) {
    Write-Host "Using psql to run migration..." -ForegroundColor Green
    $env:PGPASSWORD = $dbPass
    $sqlContent | psql -h $dbHost -p $dbPort -U $dbUser -d $dbName
} else {
    # Use .NET PostgreSQL library
    Write-Host "psql not found. Using .NET approach..." -ForegroundColor Yellow
    
    # Create a simple Java app to run the migration
    $javaRunner = @"
import java.sql.*;

public class RunMigration {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/cps_db";
        String user = "cps_user";
        String pass = "cps_pass";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            // Read SQL file
            String sql = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("db/lucky_wheel_migration.sql")), 
                java.nio.charset.StandardCharsets.UTF_8);
            
            // Execute
            stmt.execute(sql);
            System.out.println("Migration completed successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
"@
    
    Write-Host "Please run the migration manually using pgAdmin or run:" -ForegroundColor Yellow
    Write-Host "cd BE-Nice-develop && mvn flyway:migrate" -ForegroundColor Cyan
    Write-Host "OR run the SQL file directly in your PostgreSQL client" -ForegroundColor Cyan
}

Write-Host "`n=== Migration Complete ===" -ForegroundColor Cyan
