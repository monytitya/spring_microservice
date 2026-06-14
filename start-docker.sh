#!/bin/bash

# Docker Microservices Startup Script

echo "======================================"
echo "Banking Microservices - Docker Setup"
echo "======================================"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker daemon is not running!"
    exit 1
fi

echo "[1/3] Building Docker images..."
echo "This may take 5-10 minutes on first build..."
docker-compose build

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "[2/3] Starting containers..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "Failed to start containers!"
    exit 1
fi

echo ""
echo "[3/3] Waiting for services to be ready..."
sleep 30

echo ""
echo "======================================"
echo "All services are running!" 
echo "======================================"
echo ""
echo "Docker Containers:"
docker-compose ps
echo ""
echo "Access points:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo "  - Eureka Registry: http://localhost:8761"
echo "  - Config Server: http://localhost:8888"
echo ""
echo "Database:"
echo "  - Host: localhost:5432"
echo "  - User: admin"
echo "  - Password: admin"
echo ""
echo "Kafka:"
echo "  - Bootstrap Server: localhost:9092"
echo ""
echo "View logs:"
echo "  docker-compose logs -f [service_name]"
echo ""
echo "Stop all containers:"
echo "  docker-compose down"
echo ""
