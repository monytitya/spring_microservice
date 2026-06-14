@echo off
REM Banking Microservices Startup Script
REM This script opens separate terminals for each service

echo Starting Banking Microservices...
echo.

REM Start Config Server
echo Starting Config Server on port 8888...
start "Config Server" cmd /k "cd config-server && mvn spring-boot:run"
timeout /t 5 /nobreak

REM Start Discovery Server (Eureka)
echo Starting Discovery Server on port 8761...
start "Discovery Server" cmd /k "cd discovery-server && mvn spring-boot:run"
timeout /t 5 /nobreak

REM Start Account Service
echo Starting Account Service on port 8082...
start "Account Service" cmd /k "cd account-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Start Customer Service
echo Starting Customer Service on port 8081...
start "Customer Service" cmd /k "cd customer-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Start Transaction Service
echo Starting Transaction Service on port 8083...
start "Transaction Service" cmd /k "cd transaction-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Start Loan Service
echo Starting Loan Service on port 8084...
start "Loan Service" cmd /k "cd loan-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Start Card Service
echo Starting Card Service on port 8085...
start "Card Service" cmd /k "cd card-service && mvn spring-boot:run"
timeout /t 3 /nobreak

REM Start API Gateway
echo Starting API Gateway on port 8080...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"

echo.
echo All services are starting...
echo Wait 30-60 seconds for all services to fully initialize.
echo.
echo After services are running, access Swagger UI at:
echo   http://localhost:8080/swagger-ui.html
echo.
pause
