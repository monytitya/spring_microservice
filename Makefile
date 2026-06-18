# =============================================================================
# Makefile — Banking Microservices DevOps Shortcuts
# Usage: make <target>
# Requires: Docker, Docker Compose, Maven (or use mvnw)
# =============================================================================

.PHONY: help build test clean \
        docker-build docker-up docker-down docker-restart docker-logs docker-ps \
        monitor-up monitor-down \
        release-patch release-minor release-major \
        health-check

# ── Colors for pretty output ──────────────────────────────────────────────────
GREEN  := \033[0;32m
YELLOW := \033[0;33m
CYAN   := \033[0;36m
RESET  := \033[0m

# ── Default target: show help ─────────────────────────────────────────────────
help:
	@echo ""
	@echo "$(CYAN)╔══════════════════════════════════════════════════════════╗$(RESET)"
	@echo "$(CYAN)║       Banking Microservices — DevOps Commands            ║$(RESET)"
	@echo "$(CYAN)╚══════════════════════════════════════════════════════════╝$(RESET)"
	@echo ""
	@echo "$(YELLOW)── Build ───────────────────────────────────────────────────$(RESET)"
	@echo "  $(GREEN)make build$(RESET)             Build all service JARs (skip tests)"
	@echo "  $(GREEN)make build-service S=customer-service$(RESET)  Build one service"
	@echo "  $(GREEN)make test$(RESET)              Run all unit tests"
	@echo "  $(GREEN)make clean$(RESET)             Clean all build artifacts"
	@echo ""
	@echo "$(YELLOW)── Docker ──────────────────────────────────────────────────$(RESET)"
	@echo "  $(GREEN)make docker-build$(RESET)      Build all Docker images"
	@echo "  $(GREEN)make docker-up$(RESET)         Start all services (detached)"
	@echo "  $(GREEN)make docker-down$(RESET)       Stop and remove containers"
	@echo "  $(GREEN)make docker-restart$(RESET)    Restart all services"
	@echo "  $(GREEN)make docker-logs$(RESET)       Tail logs from all services"
	@echo "  $(GREEN)make docker-ps$(RESET)         Show running containers"
	@echo "  $(GREEN)make docker-clean$(RESET)      Remove containers + volumes"
	@echo ""
	@echo "$(YELLOW)── Monitoring ──────────────────────────────────────────────$(RESET)"
	@echo "  $(GREEN)make monitor-up$(RESET)        Start monitoring stack"
	@echo "  $(GREEN)make monitor-down$(RESET)      Stop monitoring stack"
	@echo ""
	@echo "$(YELLOW)── Health ──────────────────────────────────────────────────$(RESET)"
	@echo "  $(GREEN)make health-check$(RESET)      Check all service health endpoints"
	@echo ""
	@echo "$(YELLOW)── Release ─────────────────────────────────────────────────$(RESET)"
	@echo "  $(GREEN)make release-patch$(RESET)     Tag and push a patch release (v*.*.X)"
	@echo "  $(GREEN)make release-minor$(RESET)     Tag and push a minor release (v*.X.0)"
	@echo "  $(GREEN)make release-major$(RESET)     Tag and push a major release (vX.0.0)"
	@echo ""

# =============================================================================
# BUILD
# =============================================================================

## Build all service JARs (skip tests for speed)
build:
	@echo "$(CYAN)🔨 Building all services...$(RESET)"
	./mvnw clean package -DskipTests -B
	@echo "$(GREEN)✅ Build complete$(RESET)"

## Build a single service: make build-service S=customer-service
build-service:
	@echo "$(CYAN)🔨 Building $(S)...$(RESET)"
	./mvnw clean package -pl $(S) -am -DskipTests -B
	@echo "$(GREEN)✅ $(S) built$(RESET)"

## Run all unit tests
test:
	@echo "$(CYAN)🧪 Running tests...$(RESET)"
	./mvnw test -B
	@echo "$(GREEN)✅ Tests complete$(RESET)"

## Clean all build artifacts
clean:
	@echo "$(CYAN)🧹 Cleaning build artifacts...$(RESET)"
	./mvnw clean -B
	@echo "$(GREEN)✅ Clean complete$(RESET)"

# =============================================================================
# DOCKER
# =============================================================================

## Build all Docker images locally
docker-build: build
	@echo "$(CYAN)🐳 Building Docker images...$(RESET)"
	docker compose build --parallel
	@echo "$(GREEN)✅ Docker images built$(RESET)"

## Start all services in detached mode
docker-up:
	@echo "$(CYAN)🚀 Starting all services...$(RESET)"
	docker compose up -d
	@echo "$(GREEN)✅ Services started$(RESET)"
	@echo "$(YELLOW)   API Gateway  → http://localhost:8080$(RESET)"
	@echo "$(YELLOW)   Eureka       → http://localhost:8761$(RESET)"
	@echo "$(YELLOW)   Config       → http://localhost:8888$(RESET)"

## Stop and remove all containers (keep volumes)
docker-down:
	@echo "$(CYAN)🛑 Stopping services...$(RESET)"
	docker compose down
	@echo "$(GREEN)✅ Services stopped$(RESET)"

## Restart all services
docker-restart:
	@echo "$(CYAN)♻️  Restarting services...$(RESET)"
	docker compose restart
	@echo "$(GREEN)✅ Services restarted$(RESET)"

## Tail logs from all services
docker-logs:
	docker compose logs -f --tail=100

## Tail logs from a single service: make service-logs S=customer-service
service-logs:
	docker compose logs -f --tail=200 $(S)

## Show container status
docker-ps:
	docker compose ps

## Full cleanup: remove containers, volumes, and dangling images
docker-clean:
	@echo "$(CYAN)🗑️  Full cleanup (containers + volumes)...$(RESET)"
	docker compose down -v --remove-orphans
	docker image prune -f
	@echo "$(GREEN)✅ Cleanup complete$(RESET)"

# =============================================================================
# MONITORING
# =============================================================================

## Start the observability stack (Prometheus + Grafana + Kafka UI + pgAdmin)
monitor-up:
	@echo "$(CYAN)📊 Starting monitoring stack...$(RESET)"
	docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
	@echo "$(GREEN)✅ Monitoring stack started$(RESET)"
	@echo "$(YELLOW)   Grafana     → http://localhost:3000  (admin/admin)$(RESET)"
	@echo "$(YELLOW)   Prometheus  → http://localhost:9090$(RESET)"
	@echo "$(YELLOW)   Kafka UI    → http://localhost:8090$(RESET)"
	@echo "$(YELLOW)   pgAdmin     → http://localhost:5050$(RESET)"

## Stop the observability stack only
monitor-down:
	@echo "$(CYAN)🛑 Stopping monitoring stack...$(RESET)"
	docker compose -f docker-compose.yml -f docker-compose.monitoring.yml down
	@echo "$(GREEN)✅ Monitoring stopped$(RESET)"

# =============================================================================
# HEALTH CHECK
# =============================================================================

## Hit all actuator health endpoints
health-check:
	@echo "$(CYAN)🏥 Checking service health...$(RESET)"
	@echo ""
	@curl -sf http://localhost:8888/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Config Server$(RESET)"       || echo "$(YELLOW)  ⚠️  Config Server — not ready$(RESET)"
	@curl -sf http://localhost:8761/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Discovery Server$(RESET)"    || echo "$(YELLOW)  ⚠️  Discovery Server — not ready$(RESET)"
	@curl -sf http://localhost:8080/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ API Gateway$(RESET)"         || echo "$(YELLOW)  ⚠️  API Gateway — not ready$(RESET)"
	@curl -sf http://localhost:8081/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Customer Service$(RESET)"    || echo "$(YELLOW)  ⚠️  Customer Service — not ready$(RESET)"
	@curl -sf http://localhost:8082/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Account Service$(RESET)"     || echo "$(YELLOW)  ⚠️  Account Service — not ready$(RESET)"
	@curl -sf http://localhost:8083/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Transaction Service$(RESET)" || echo "$(YELLOW)  ⚠️  Transaction Service — not ready$(RESET)"
	@curl -sf http://localhost:8084/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Loan Service$(RESET)"        || echo "$(YELLOW)  ⚠️  Loan Service — not ready$(RESET)"
	@curl -sf http://localhost:8085/actuator/health | python3 -m json.tool 2>/dev/null && echo "$(GREEN)  ✅ Card Service$(RESET)"        || echo "$(YELLOW)  ⚠️  Card Service — not ready$(RESET)"
	@echo ""

# =============================================================================
# RELEASE — Git tag-based versioning
# =============================================================================

## Bump patch version and push tag (triggers CD production pipeline)
release-patch:
	@CURRENT=$$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0"); \
	MAJOR=$$(echo $$CURRENT | cut -d. -f1 | tr -d v); \
	MINOR=$$(echo $$CURRENT | cut -d. -f2); \
	PATCH=$$(echo $$CURRENT | cut -d. -f3); \
	NEW="v$$MAJOR.$$MINOR.$$((PATCH+1))"; \
	echo "$(CYAN)🏷️  Tagging release: $$CURRENT → $$NEW$(RESET)"; \
	git tag -a $$NEW -m "Release $$NEW"; \
	git push origin $$NEW; \
	echo "$(GREEN)✅ Release $$NEW pushed — CD pipeline triggered$(RESET)"

## Bump minor version
release-minor:
	@CURRENT=$$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0"); \
	MAJOR=$$(echo $$CURRENT | cut -d. -f1 | tr -d v); \
	MINOR=$$(echo $$CURRENT | cut -d. -f2); \
	NEW="v$$MAJOR.$$((MINOR+1)).0"; \
	echo "$(CYAN)🏷️  Tagging release: $$CURRENT → $$NEW$(RESET)"; \
	git tag -a $$NEW -m "Release $$NEW"; \
	git push origin $$NEW; \
	echo "$(GREEN)✅ Release $$NEW pushed — CD pipeline triggered$(RESET)"

## Bump major version
release-major:
	@CURRENT=$$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0"); \
	MAJOR=$$(echo $$CURRENT | cut -d. -f1 | tr -d v); \
	NEW="v$$((MAJOR+1)).0.0"; \
	echo "$(CYAN)🏷️  Tagging release: $$CURRENT → $$NEW$(RESET)"; \
	git tag -a $$NEW -m "Release $$NEW"; \
	git push origin $$NEW; \
	echo "$(GREEN)✅ Release $$NEW pushed — CD pipeline triggered$(RESET)"
