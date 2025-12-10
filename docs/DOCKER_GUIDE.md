# Docker Deployment Guide

## Quick Start

### Build and Run

```bash
# Build the Docker image
docker build -t sensorbite:latest .

# Run the container
docker run -d \
  --name sensorbite \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  sensorbite:latest

# Check logs
docker logs -f sensorbite

# Stop container
docker stop sensorbite
docker rm sensorbite
```

### Docker Compose (Recommended)

```bash
# Start all services (app + monitoring)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

## Services

### Application (Port 8080)
- **URL**: http://localhost:8080
- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Swagger**: http://localhost:8080/swagger-ui.html

### Prometheus (Port 9090)
- **URL**: http://localhost:9090
- **Targets**: http://localhost:9090/targets
- **Queries**: Pre-configured for Sensorbite metrics

### Grafana (Port 3000)
- **URL**: http://localhost:3000
- **Login**: admin / admin
- **Datasource**: Pre-configured Prometheus

## Configuration

### Environment Variables

```bash
# Required for Sentinel Hub integration
export SENTINEL_HUB_CLIENT_ID="your-client-id"
export SENTINEL_HUB_CLIENT_SECRET="your-client-secret"

# Optional
export SPRING_PROFILES_ACTIVE="prod"
export JAVA_OPTS="-Xmx512m"
```

### docker-compose.yml Environment

```yaml
services:
  sensorbite:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SENTINEL_HUB_CLIENT_ID=${SENTINEL_HUB_CLIENT_ID}
      - SENTINEL_HUB_CLIENT_SECRET=${SENTINEL_HUB_CLIENT_SECRET}
```

## Volumes

### Log Files
```bash
# Logs are persisted to ./logs directory
tail -f logs/sensorbite.log
tail -f logs/sensorbite-error.log
```

### Sample Data
```bash
# Sample network data mounted from ./data
ls -la data/
```

## Health Checks

### Container Health
```bash
docker ps
# HEALTHY status indicates all checks passing
```

### Manual Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Health Check Details
- Interval: 30 seconds
- Timeout: 10 seconds
- Retries: 3
- Start period: 60 seconds

## Resource Limits

### Default Configuration
- **Memory Limit**: 512MB
- **Memory Reservation**: 256MB
- **CPU Limit**: 1.0 core
- **JVM Max RAM**: 75% of container memory

### Custom Limits
```yaml
services:
  sensorbite:
    mem_limit: 1g
    mem_reservation: 512m
    cpus: 2.0
```

## Monitoring

### Prometheus Metrics
```bash
# View metrics endpoint
curl http://localhost:8080/actuator/prometheus

# Query Prometheus
curl 'http://localhost:9090/api/v1/query?query=up'
```

### Grafana Dashboards
1. Login to http://localhost:3000
2. Navigate to Dashboards
3. Import dashboard JSON from `docker/grafana/dashboards/`

## Troubleshooting

### Container won't start
```bash
# Check logs
docker logs sensorbite

# Check compose status
docker-compose ps

# Restart services
docker-compose restart
```

### Port already in use
```bash
# Change ports in docker-compose.yml
ports:
  - "8081:8080"  # Use 8081 instead of 8080
```

### Out of memory
```bash
# Increase memory limit
mem_limit: 1g
```

### Logs not appearing
```bash
# Check volume mount
ls -la logs/

# Check permissions
chmod 755 logs/
```

## Production Deployment

### Build optimized image
```bash
docker build \
  --build-arg GRADLE_OPTIONS="--no-daemon -Dorg.gradle.jvmargs=-Xmx1g" \
  -t sensorbite:v1.0.0 .
```

### Tag and push to registry
```bash
docker tag sensorbite:v1.0.0 your-registry/sensorbite:v1.0.0
docker push your-registry/sensorbite:v1.0.0
```

### Production docker-compose
```yaml
services:
  sensorbite:
    image: your-registry/sensorbite:v1.0.0
    restart: always
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## Security

### Run as non-root
- Container runs as `appuser` (non-root)
- UID/GID: Automatically assigned by Alpine

### Network isolation
- Services communicate via private bridge network
- Only necessary ports exposed to host

### Secrets management
- Use environment variables
- Never commit credentials
- Use Docker secrets in Swarm mode

## Performance Tuning

### JVM Options
```yaml
environment:
  - JAVA_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### Resource allocation
```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 1G
    reservations:
      cpus: '1.0'
      memory: 512M
```

## Maintenance

### Update base image
```bash
# Pull latest base images
docker-compose pull

# Rebuild
docker-compose up -d --build
```

### Cleanup
```bash
# Remove stopped containers
docker container prune

# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune
```

### Backup
```bash
# Backup logs
tar -czf logs-backup-$(date +%Y%m%d).tar.gz logs/

# Backup Prometheus data
docker-compose exec prometheus tar -czf /prometheus-backup.tar.gz /prometheus
```

## Commands Reference

```bash
# Build
docker build -t sensorbite .
docker-compose build

# Run
docker run -d -p 8080:8080 sensorbite
docker-compose up -d

# Logs
docker logs sensorbite -f
docker-compose logs -f sensorbite

# Shell access
docker exec -it sensorbite sh
docker-compose exec sensorbite sh

# Stop
docker stop sensorbite
docker-compose down

# Remove
docker rm sensorbite
docker-compose down -v

# Stats
docker stats sensorbite
docker-compose top
```

## Support

For issues or questions:
- Check logs: `docker logs sensorbite`
- Review documentation: README.md
- Check health endpoint: http://localhost:8080/actuator/health
