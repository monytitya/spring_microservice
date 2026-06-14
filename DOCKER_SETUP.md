# Docker Setup Guide for Banking Microservices

## Prerequisites

Before running with Docker, make sure you have:
- Docker Desktop installed ([Download](https://www.docker.com/products/docker-desktop))
- At least 8GB RAM allocated to Docker
- Docker Compose version 2.0+ (usually included with Docker Desktop)

## Quick Start (Recommended)

### Option 1: Windows PowerShell
```powershell
powershell -ExecutionPolicy Bypass -File start-docker.ps1
```

### Option 2: Windows Command Prompt
```bash
start-docker.bat
```

### Option 3: Linux/Mac
```bash
chmod +x start-docker.sh
./start-docker.sh
```

### Option 4: Manual Docker Compose (All Platforms)
```bash
docker-compose build
docker-compose up -d
```

## Supported Docker Commands

### View Running Containers
```bash
docker-compose ps
```

### View Logs (Real-time)
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api-gateway
docker-compose logs -f account-service
docker-compose logs -f transaction-service
```

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

### Rebuild Without Cache
```bash
docker-compose build --no-cache
```

### Restart a Specific Service
```bash
docker-compose restart api-gateway
```

### Execute Command in Running Container
```bash
docker-compose exec api-gateway /bin/sh
```

### View Container Logs Since Last Hour
```bash
docker-compose logs --since 1h api-gateway
```

## Access Points

Once all services are running (wait 30-60 seconds):

| Service | URL | Purpose |
|---------|-----|---------|
| API Gateway | http://localhost:8080 | Main entry point |
| Swagger UI | http://localhost:8080/swagger-ui.html | Test all APIs |
| Eureka Registry | http://localhost:8761 | Service discovery |
| Config Server | http://localhost:8888 | Configuration management |
| PostgreSQL | localhost:5432 | Database |
| Kafka | localhost:9092 | Message broker |

## Database Connection

- **Host**: localhost:5432
- **User**: admin
- **Password**: admin
- **Databases**: customer_db, account_db, transaction_db, loan_db, card_db

## Troubleshooting

### Containers Exit Immediately
```bash
# Check logs for errors
docker-compose logs
```

### Port Already in Use
```bash
# On Windows PowerShell, find what's using the port
Get-NetTCPConnection -LocalPort 8080
# Kill the process if needed
Stop-Process -Id <PID> -Force
```

### Out of Memory
Increase Docker memory in Docker Desktop settings to at least 8GB.

### Services Can't Connect to Each Other
Make sure all services are on the same network (already configured in docker-compose.yml).

### Database Connection Errors
Wait 30 seconds for PostgreSQL to fully initialize before accessing services.

## Architecture Overview

```
┌─────────────────────────────────────┐
│       Browser / Client              │
└────────────────┬────────────────────┘
                 │
                 ▼
      ┌──────────────────────┐
      │   API Gateway        │ (Port 8080)
      │   (Service Mesh)     │
      └──────────────────────┘
              │
    ┌─────────┼─────────┬─────────┬────────┐
    ▼         ▼         ▼         ▼        ▼
┌─────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ ┌────────┐
│Customer │ │ Account  │ │Transaction│ │  Loan   │ │ Card   │
│Service  │ │ Service  │ │ Service   │ │ Service │ │Service │
│ (8081)  │ │ (8082)   │ │  (8083)   │ │ (8084)  │ │(8085)  │
└────┬────┘ └────┬─────┘ └────┬──────┘ └────┬────┘ └───┬────┘
     │           │            │             │         │
     └───────────┼────────────┼─────────────┴─────────┘
                 │            │
         ┌───────┴──┐  ┌──────┴──────┐
         ▼          ▼  ▼             ▼
    ┌──────────┐ ┌──────────┐  ┌─────────┐
    │PostgreSQL│ │  Kafka   │  │ Zookeeper│
    │          │ │          │  │          │
    │(Port5432)│ │(Port9092)│  │(Port2181)│
    └──────────┘ └──────────┘  └─────────┘
```

## Performance Tips

1. **First Build**: Takes 5-10 minutes as it needs to download base images and build all services
2. **Subsequent Builds**: Much faster as Docker caches layers
3. **Memory**: Monitor memory usage - each service uses ~300-500MB
4. **Disk Space**: Ensure at least 5GB free disk space for images and volumes

## Development Workflow with Docker

### To Modify Code and Rebuild
```bash
# Make changes to your code
# Then rebuild only the affected service
docker-compose build api-gateway
docker-compose up -d api-gateway
```

### To Rebuild All Services
```bash
docker-compose build --no-cache
docker-compose up -d
```

### To Keep Logs Open While Services Start
```bash
docker-compose up
# Press Ctrl+C to detach (services keep running)
```

## Production Considerations

For production use, consider:
1. Using environment-specific compose files
2. Adding resource limits
3. Using proper secrets management instead of env variables
4. Implementing health checks
5. Setting up proper logging aggregation
6. Using container orchestration (Kubernetes)

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
