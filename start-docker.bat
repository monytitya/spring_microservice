@echo off
REM Docker Microservices Startup Script for Windows

setlocal enabledelayedexpansion

echo ======================================
echo Banking Microservices - Docker Setup
echo ======================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not installed or not running!
    pause
    exit /b 1
)

echo [1/3] Building Docker images...
echo This may take 5-10 minutes on first build...
echo.

docker-compose build

if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Starting containers...
docker-compose up -d

if errorlevel 1 (
    echo Failed to start containers!
    pause
    exit /b 1
)

echo.
echo [3/3] Waiting for services to be ready ^(30 seconds^)...
timeout /t 30 /nobreak

echo.
echo ======================================
echo All services are running!
echo ======================================
echo.

echo Docker Containers:
docker-compose ps
echo.

echo Access points:
echo   - API Gateway: http://localhost:8080
echo   - Swagger UI: http://localhost:8080/swagger-ui.html
echo   - Eureka Registry: http://localhost:8761
echo   - Config Server: http://localhost:8888
echo.

echo Database:
echo   - Host: localhost:5432
echo   - User: admin
echo   - Password: admin
echo.

echo Kafka:
echo   - Bootstrap Server: localhost:9092
echo.

echo Useful Commands:
echo   View logs: docker-compose logs -f [service_name]
echo   Stop all: docker-compose down
echo   Remove volumes: docker-compose down -v
echo   Rebuild: docker-compose build --no-cache
echo.

pause
