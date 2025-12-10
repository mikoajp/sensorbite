# 🚀 Deployment Guide

This guide covers deploying Sensorbite to GitHub and various cloud platforms.

## 📦 GitHub Deployment

### 1. Create GitHub Repository

```bash
# Create a new repository on GitHub (via web interface)
# Then push your local repository:

git remote add origin https://github.com/yourusername/sensorbite.git
git branch -M main
git push -u origin main
```

### 2. Configure Repository Settings

#### Enable GitHub Actions
- Go to **Settings** → **Actions** → **General**
- Enable "Allow all actions and reusable workflows"

#### Enable GitHub Packages (Docker Registry)
- Go to **Settings** → **Packages**
- Make package public (optional)

#### Configure Secrets (if using Sentinel Hub)
- Go to **Settings** → **Secrets and variables** → **Actions**
- Add secrets:
  - `SENTINEL_CLIENT_ID`
  - `SENTINEL_CLIENT_SECRET`

### 3. Create a Release

```bash
# Tag your release
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This will automatically:
- Build the project
- Run all tests
- Build and push Docker image to GitHub Container Registry
- Create a GitHub release

### 4. Update README Badges

Replace `yourusername` in `README.md` with your actual GitHub username:

```markdown
[![CI](https://github.com/yourusername/sensorbite/workflows/CI/badge.svg)](https://github.com/yourusername/sensorbite/actions)
```

## 🐳 Docker Hub Deployment

### 1. Build and Tag Image

```bash
docker build -t yourusername/sensorbite:latest .
docker tag yourusername/sensorbite:latest yourusername/sensorbite:1.0.0
```

### 2. Push to Docker Hub

```bash
docker login
docker push yourusername/sensorbite:latest
docker push yourusername/sensorbite:1.0.0
```

### 3. Run from Docker Hub

```bash
docker run -p 8080:8080 yourusername/sensorbite:latest
```

## ☁️ Cloud Deployments

### AWS Elastic Beanstalk

1. **Create `Dockerrun.aws.json`**:
```json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "yourusername/sensorbite:latest",
    "Update": "true"
  },
  "Ports": [
    {
      "ContainerPort": 8080,
      "HostPort": 8080
    }
  ]
}
```

2. **Deploy**:
```bash
eb init -p docker sensorbite
eb create sensorbite-env
eb deploy
```

### Google Cloud Run

```bash
# Build and push to Google Container Registry
gcloud builds submit --tag gcr.io/PROJECT_ID/sensorbite

# Deploy to Cloud Run
gcloud run deploy sensorbite \
  --image gcr.io/PROJECT_ID/sensorbite \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

### Azure Container Instances

```bash
# Login to Azure
az login

# Create resource group
az group create --name sensorbite-rg --location eastus

# Create container
az container create \
  --resource-group sensorbite-rg \
  --name sensorbite \
  --image yourusername/sensorbite:latest \
  --dns-name-label sensorbite \
  --ports 8080
```

### Heroku

1. **Create `heroku.yml`**:
```yaml
build:
  docker:
    web: Dockerfile
run:
  web: java -jar /app/app.jar
```

2. **Deploy**:
```bash
heroku create sensorbite-app
heroku stack:set container
git push heroku main
```

## 🔧 Environment Variables

For production deployments, set these environment variables:

```bash
# Database (if not using H2)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sensorbite
SPRING_DATASOURCE_USERNAME=sensorbite
SPRING_DATASOURCE_PASSWORD=your-password

# Sentinel Hub API
SENTINEL_HUB_CLIENT_ID=your-client-id
SENTINEL_HUB_CLIENT_SECRET=your-secret
SENTINEL_HUB_MOCK_MODE=false

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_SENSORBITE=DEBUG

# Server
SERVER_PORT=8080
```

## 📊 Monitoring Setup

### Prometheus + Grafana (Docker Compose)

Already configured in `docker-compose.yml`. Just run:

```bash
docker-compose up -d
```

Access:
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

### Cloud Monitoring

#### AWS CloudWatch
Add to your application properties:
```yaml
management:
  metrics:
    export:
      cloudwatch:
        namespace: Sensorbite
        enabled: true
```

#### Google Cloud Monitoring
Add dependency and configure:
```yaml
management:
  metrics:
    export:
      stackdriver:
        project-id: your-project-id
        enabled: true
```

## 🔒 Security Checklist

Before deploying to production:

- [ ] Change default passwords (H2 console, Grafana, etc.)
- [ ] Use external database (PostgreSQL, MySQL)
- [ ] Configure HTTPS/SSL
- [ ] Set up firewall rules
- [ ] Enable authentication for API endpoints
- [ ] Configure CORS properly
- [ ] Use secrets management (AWS Secrets Manager, Azure Key Vault, etc.)
- [ ] Enable security headers
- [ ] Set up rate limiting
- [ ] Configure backup strategy

## 🧪 Pre-Deployment Testing

```bash
# Run all tests
./gradlew test

# Run quality checks
./gradlew check

# Build Docker image
docker build -t sensorbite:test .

# Test Docker image locally
docker run -p 8080:8080 sensorbite:test

# Verify health endpoint
curl http://localhost:8080/actuator/health
```

## 📝 Deployment Checklist

- [ ] All tests passing
- [ ] Code quality checks passing
- [ ] Docker image builds successfully
- [ ] Environment variables configured
- [ ] Database migrations prepared
- [ ] Monitoring configured
- [ ] Backup strategy in place
- [ ] Documentation updated
- [ ] Security review completed
- [ ] Load testing performed

## 🆘 Troubleshooting

### Build Fails
```bash
# Clean build
./gradlew clean build --refresh-dependencies
```

### Docker Image Issues
```bash
# Check logs
docker logs container-id

# Inspect image
docker inspect sensorbite:latest
```

### Connection Issues
- Check firewall rules
- Verify security group settings
- Check environment variables
- Review application logs

## 📞 Support

- 📖 [Documentation](docs/)
- 🐛 [Issue Tracker](https://github.com/yourusername/sensorbite/issues)
- 💬 [Discussions](https://github.com/yourusername/sensorbite/discussions)

---

**Ready to deploy! 🚀**
