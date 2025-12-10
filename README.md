# 🏢 Sensorbite - Building Sensor Management System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![CI](https://github.com/yourusername/sensorbite/workflows/CI/badge.svg)](https://github.com/yourusername/sensorbite/actions)

A comprehensive REST API system for managing building infrastructure, sensors, and emergency evacuation routing with real-time hazard detection.

## ✨ Features

### 🏗️ Building Management
- Complete CRUD operations for buildings, floors, rooms, and sensors
- RESTful API design with proper HTTP status codes
- Automatic sample data loading
- H2 in-memory database with JPA

### 🚨 Emergency Evacuation System
- **Smart Routing**: Modified Dijkstra's algorithm for optimal evacuation paths
- **Hazard Detection**: Real-time flood zone detection via Sentinel Hub API
- **Safety Scoring**: Calculate route safety scores (0-100 scale)
- **GeoJSON Support**: Load custom road networks and return map-compatible routes

### 🛡️ Enterprise Features
- **Resilience**: Circuit breaker pattern, retry mechanisms, and fallback handling
- **Performance**: Caffeine caching with intelligent TTL management
- **Observability**: Prometheus metrics, Grafana dashboards, structured logging
- **Security**: OAuth2 integration, comprehensive error handling

### 🔍 Code Quality
- Spotless code formatting (Google Java Format)
- Static analysis (PMD, SpotBugs, Checkstyle)
- 80%+ test coverage with JaCoCo
- GitHub Actions CI/CD pipeline
- Automated dependency updates via Dependabot

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker (optional, for containerized deployment)

### Run Locally

```bash
# Clone the repository
git clone https://github.com/yourusername/sensorbite.git
cd sensorbite

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

### Access Points
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:sensorbite`
  - Username: `sa`
  - Password: *(empty)*

### Run with Docker

```bash
# Build and run with Docker Compose (includes Prometheus + Grafana)
docker-compose up -d

# Access Grafana
open http://localhost:3000
# Default credentials: admin/admin
```

## 📚 API Documentation

### Evacuation Routing

Calculate safe evacuation routes avoiding hazard zones:

```bash
curl "http://localhost:8080/api/evac/route?startLat=52.0&startLon=21.0&endLat=52.04&endLon=21.04"
```

Response includes:
- Optimal route coordinates (GeoJSON format)
- Total distance and safety score
- List of hazard zones to avoid

### Building Management

```bash
# List all buildings
curl http://localhost:8080/api/buildings

# Create a building
curl -X POST http://localhost:8080/api/buildings \
  -H "Content-Type: application/json" \
  -d '{"name": "Office Tower", "address": "123 Main St"}'

# Get sensor readings
curl http://localhost:8080/api/sensor-readings/sensor/1
```

### Main Endpoints

**Buildings**
- `GET /api/buildings` - List all buildings
- `POST /api/buildings` - Create a new building
- `GET /api/buildings/{id}` - Get building details
- `PUT /api/buildings/{id}` - Update building
- `DELETE /api/buildings/{id}` - Delete building

**Sensors**
- `GET /api/sensors` - List all sensors
- `GET /api/sensors/room/{roomId}` - Get sensors in a room
- `POST /api/sensors` - Create a new sensor
- `PUT /api/sensors/{id}` - Update sensor

**Evacuation**
- `GET /api/evac/route` - Calculate evacuation route
- `GET /api/evac/health` - Service health check

For complete API reference, see [API Documentation](docs/API_EXAMPLES.md) and explore the interactive [Swagger UI](http://localhost:8080/swagger-ui.html).

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# Run quality checks (Checkstyle, PMD, SpotBugs)
./gradlew check

# Format code
./gradlew spotlessApply
```

## 🏗️ Architecture

```
sensorbite/
├── src/main/java/com/sensorbite/
│   ├── controller/          # REST endpoints
│   ├── service/             # Business logic
│   ├── repository/          # Data access layer
│   ├── entity/              # JPA entities
│   ├── dto/                 # Data transfer objects
│   ├── evacuation/          # Emergency routing system
│   │   ├── service/         # Route calculation, hazard detection
│   │   ├── domain/          # Graph structures, road network
│   │   └── config/          # Resilience, caching, WebClient
│   └── config/              # Spring configuration, metrics, logging
└── src/main/resources/
    ├── application.yml      # Configuration
    ├── data/                # Sample GeoJSON road networks
    └── logback-spring.xml   # Logging configuration
```

## 🔧 Configuration

Key configuration in `application.yml`:

```yaml
# Sentinel Hub API (optional, uses mock by default)
sentinel-hub:
  client-id: your-client-id
  client-secret: your-secret
  mock-mode: true

# Cache settings
spring:
  cache:
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=60m

# Metrics
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

## 🐳 Docker Deployment

The project includes a complete Docker setup with monitoring:

```bash
# Build Docker image
docker build -t sensorbite:latest .

# Run with Docker Compose (includes Prometheus + Grafana)
docker-compose up -d
```

Services:
- **Application**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

See [Docker Guide](docs/DOCKER_GUIDE.md) for more details.

## 📖 Documentation

- [📋 Complete Project Summary](docs/COMPLETE_PROJECT_SUMMARY.md)
- [🗺️ Evacuation API Guide](docs/EVACUATION_API.md)
- [🐳 Docker Deployment](docs/DOCKER_GUIDE.md)
- [🛰️ Sentinel Hub Integration](docs/SENTINEL_HUB_INTEGRATION.md)
- [📝 Changelog](CHANGELOG.md)
- [🤝 Contributing Guidelines](CONTRIBUTING.md)

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: H2 (in-memory), Spring Data JPA
- **API Documentation**: Swagger/OpenAPI 3.0
- **Testing**: JUnit 5, Mockito, REST Assured
- **Code Quality**: Spotless, PMD, SpotBugs, Checkstyle
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **Caching**: Caffeine
- **Monitoring**: Micrometer, Prometheus, Grafana
- **GIS**: GeoTools, JTS Topology Suite
- **Build**: Gradle 8.5
- **CI/CD**: GitHub Actions

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on:
- Code of conduct
- Development workflow
- Pull request guidelines
- Code quality standards

## 📄 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [GeoTools](https://geotools.org/) - GIS functionality
- [JTS](https://locationtech.github.io/jts/) - Geometry processing
- [Resilience4j](https://resilience4j.readme.io/) - Resilience patterns
- [Sentinel Hub](https://www.sentinel-hub.com/) - Satellite imagery API

## 💬 Support

- 🐛 [Report Issues](https://github.com/yourusername/sensorbite/issues)
- 💡 [Request Features](https://github.com/yourusername/sensorbite/issues/new?template=feature_request.md)
- 📚 [Documentation](docs/)

---

**Made with ❤️ using Spring Boot** | If you find this useful, please ⭐ this repository!
