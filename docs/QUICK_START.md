# 🚀 Quick Start Guide

This guide will help you get Sensorbite up and running in minutes.

## Prerequisites

- **Java 17+** - [Download here](https://adoptium.net/)
- **Git** - [Download here](https://git-scm.com/)
- **Docker** (optional) - [Download here](https://www.docker.com/)

## Option 1: Run Locally (5 minutes)

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/sensorbite.git
cd sensorbite
```

### Step 2: Build the Project

```bash
# On Linux/Mac
./gradlew build

# On Windows
gradlew.bat build
```

### Step 3: Run the Application

```bash
# On Linux/Mac
./gradlew bootRun

# On Windows
gradlew.bat bootRun
```

### Step 4: Access the Application

Open your browser and navigate to:

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:sensorbite`
  - Username: `sa`
  - Password: *(leave empty)*

## Option 2: Run with Docker (3 minutes)

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/sensorbite.git
cd sensorbite
```

### Step 2: Start with Docker Compose

```bash
docker-compose up -d
```

This will start:
- **Sensorbite API** on port 8080
- **Prometheus** on port 9090
- **Grafana** on port 3000

### Step 3: Access the Services

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
  - Username: `admin`
  - Password: `admin`

## First API Call

Try your first evacuation route calculation:

```bash
curl "http://localhost:8080/api/evac/route?startLat=52.0&startLon=21.0&endLat=52.04&endLon=21.04"
```

Or create your first building:

```bash
curl -X POST http://localhost:8080/api/buildings \
  -H "Content-Type: application/json" \
  -d '{"name": "My Building", "address": "123 Main St"}'
```

## What's Next?

- 📖 Read the [API Documentation](API_EXAMPLES.md)
- 🚨 Learn about [Evacuation Routing](EVACUATION_API.md)
- 🐳 Explore [Docker Deployment](DOCKER_GUIDE.md)
- 🛰️ Configure [Sentinel Hub Integration](SENTINEL_HUB_INTEGRATION.md)

## Common Issues

### Port Already in Use

If port 8080 is already in use, change it in `application.yml`:

```yaml
server:
  port: 8081
```

### Java Version Issues

Verify your Java version:

```bash
java -version
```

You need Java 17 or higher.

### Docker Issues

Make sure Docker is running:

```bash
docker --version
docker-compose --version
```

## Need Help?

- 📚 Check the [Complete Documentation](COMPLETE_PROJECT_SUMMARY.md)
- 🐛 [Report an Issue](https://github.com/yourusername/sensorbite/issues)
- 💬 [Start a Discussion](https://github.com/yourusername/sensorbite/discussions)

---

**Happy coding! 🎉**
