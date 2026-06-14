# PowerShell Script to Build and Start All Services with Docker

Write-Host "======================================"
Write-Host "Banking Microservices - Docker Setup"
Write-Host "======================================"
Write-Host ""

# Check if Docker is installed and running
try {
    docker info > $null 2>&1
}
catch {
    Write-Host "Error: Docker is not installed or not running!" -ForegroundColor Red
    exit 1
}

Write-Host "[1/3] Building Docker images..." -ForegroundColor Green
Write-Host "This may take 5-10 minutes on first build..."
Write-Host ""

docker-compose build

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/3] Starting containers..." -ForegroundColor Green
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start containers!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/3] Waiting for services to be ready (30 seconds)..." -ForegroundColor Green
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "======================================"
Write-Host "All services are running!" -ForegroundColor Green
Write-Host "======================================"
Write-Host ""

Write-Host "Docker Containers:" -ForegroundColor Cyan
docker-compose ps
Write-Host ""

Write-Host "Access points:" -ForegroundColor Cyan
Write-Host "  - API Gateway: http://localhost:8080"
Write-Host "  - Swagger UI: http://localhost:8080/swagger-ui.html"
Write-Host "  - Eureka Registry: http://localhost:8761"
Write-Host "  - Config Server: http://localhost:8888"
Write-Host ""

Write-Host "Database:" -ForegroundColor Cyan
Write-Host "  - Host: localhost:5432"
Write-Host "  - User: admin"
Write-Host "  - Password: admin"
Write-Host ""

Write-Host "Kafka:" -ForegroundColor Cyan
Write-Host "  - Bootstrap Server: localhost:9092"
Write-Host ""

Write-Host "Useful Commands:" -ForegroundColor Yellow
Write-Host "  View logs: docker-compose logs -f [service_name]"
Write-Host "  Stop all: docker-compose down"
Write-Host "  Remove volumes: docker-compose down -v"
Write-Host "  Rebuild: docker-compose build --no-cache"
Write-Host ""
