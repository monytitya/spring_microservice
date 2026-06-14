# PowerShell Script to Start All Services
# Run this from the root project directory

Write-Host "======================================"
Write-Host "Banking Microservices Startup"
Write-Host "======================================"
Write-Host ""

# Step 1: Build all services
Write-Host "[1/2] Building all services..."
Write-Host "This may take 2-3 minutes on first run..."
mvn clean package -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Exiting..." -ForegroundColor Red
    exit 1
}

Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host ""

# Step 2: Start services
Write-Host "[2/2] Starting all services in separate windows..."
Write-Host ""

$services = @(
    @{ name = "Config Server"; port = "8888"; dir = "config-server" },
    @{ name = "Discovery Server"; port = "8761"; dir = "discovery-server" },
    @{ name = "Customer Service"; port = "8081"; dir = "customer-service" },
    @{ name = "Account Service"; port = "8082"; dir = "account-service" },
    @{ name = "Transaction Service"; port = "8083"; dir = "transaction-service" },
    @{ name = "Loan Service"; port = "8084"; dir = "loan-service" },
    @{ name = "Card Service"; port = "8085"; dir = "card-service" },
    @{ name = "API Gateway"; port = "8080"; dir = "api-gateway" }
)

foreach ($service in $services) {
    Write-Host "Starting $($service.name) on port $($service.port)..."
    $startTime = Get-Date
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PWD\$($service.dir)'; mvn spring-boot:run" -WindowStyle Normal -PassThru
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "======================================"
Write-Host "All services are starting..." -ForegroundColor Green
Write-Host "======================================"
Write-Host ""
Write-Host "Please wait 30-60 seconds for all services to initialize."
Write-Host ""
Write-Host "Access points:"
Write-Host "  - Main API Gateway: http://localhost:8080"
Write-Host "  - Swagger UI (All APIs): http://localhost:8080/swagger-ui.html"
Write-Host "  - Eureka Registry: http://localhost:8761"
Write-Host ""
