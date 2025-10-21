# IoT Microservices Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Deployment](#docker-deployment)
4. [Production Deployment](#production-deployment)
5. [Monitoring and Logging](#monitoring-and-logging)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements
- **OS**: Linux, macOS, or Windows with WSL2
- **RAM**: Minimum 8GB, Recommended 16GB
- **CPU**: Minimum 4 cores, Recommended 8 cores
- **Storage**: Minimum 50GB free space
- **Network**: Ports 8080-8084, 8761, 1883, 5432, 8086, 9092, 6379

### Software Requirements
- **Java**: JDK 25 or higher
- **Maven**: 3.8+ 
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Git**: Latest version

### Optional Tools
- **mosquitto-clients**: For MQTT testing
- **wscat**: For WebSocket testing
- **curl**: For API testing
- **jq**: For JSON processing

## Local Development Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd IOT
```

### 2. Environment Configuration
```bash
# Copy environment template
cp env.example .env

# Edit configuration
nano .env
```

### 3. Build Project
```bash
# Build all modules
mvn clean install

# Build specific service
mvn clean install -pl device-management-service
```

### 4. Start Infrastructure
```bash
# Start only infrastructure services
docker-compose up -d eureka-server zookeeper kafka mosquitto postgres influxdb redis

# Wait for services to be ready
sleep 30
```

### 5. Start Microservices
```bash
# Start all services
docker-compose up -d

# Or start individual services
docker-compose up -d api-gateway
docker-compose up -d device-management-service
```

### 6. Verify Deployment
```bash
# Run health checks
./scripts/test-system.sh

# Check service status
docker-compose ps
```

## Docker Deployment

### 1. Build Docker Images
```bash
# Build all images
docker-compose build

# Build specific service
docker-compose build api-gateway
```

### 2. Start Services
```bash
# Start all services
docker-compose up -d

# Start with specific configuration
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 3. Scale Services
```bash
# Scale device processor service
docker-compose up -d --scale device-processor-service=3

# Scale notification service
docker-compose up -d --scale notification-service=2
```

### 4. Update Services
```bash
# Pull latest images
docker-compose pull

# Recreate services
docker-compose up -d --force-recreate
```

## Production Deployment

### 1. Production Configuration

Create `docker-compose.prod.yml`:
```yaml
version: '3.8'

services:
  api-gateway:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=${JWT_SECRET}
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'

  device-management-service:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 1G
          cpus: '0.5'

  device-processor-service:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MQTT_BROKER_URL=${MQTT_BROKER_URL}
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 2G
          cpus: '1.0'

  postgres:
    environment:
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_prod_data:/var/lib/postgresql/data
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'

  influxdb:
    environment:
      - DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=${INFLUXDB_TOKEN}
    volumes:
      - influxdb_prod_data:/var/lib/influxdb2
    deploy:
      resources:
        limits:
          memory: 4G
          cpus: '2.0'

volumes:
  postgres_prod_data:
  influxdb_prod_data:
```

### 2. Environment Variables
```bash
# Production environment file
cat > .env.prod << EOF
# Database
DB_PASSWORD=secure_password_here

# InfluxDB
INFLUXDB_TOKEN=secure_token_here

# MQTT
MQTT_BROKER_URL=tcp://mqtt-broker.company.com:1883

# JWT
JWT_SECRET=very_secure_jwt_secret_key_here

# Email
MAIL_USERNAME=alerts@company.com
MAIL_PASSWORD=secure_email_password

# SMS
SMS_API_KEY=secure_sms_api_key
EOF
```

### 3. Security Configuration

#### MQTT Security
```bash
# Create MQTT user
docker-compose exec mosquitto mosquitto_passwd -c /mosquitto/config/passwd iot_user

# Update mosquitto.conf
cat >> mosquitto/config/mosquitto.conf << EOF
# Security
allow_anonymous false
password_file /mosquitto/config/passwd
acl_file /mosquitto/config/acl
EOF
```

#### Database Security
```bash
# Create database user with limited privileges
docker-compose exec postgres psql -U postgres -c "
CREATE USER iot_app_user WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE iot_devices TO iot_app_user;
GRANT USAGE ON SCHEMA public TO iot_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO iot_app_user;
"
```

### 4. Load Balancer Configuration

#### Nginx Configuration
```nginx
upstream api_gateway {
    server api-gateway-1:8080;
    server api-gateway-2:8080;
}

upstream device_management {
    server device-management-service-1:8081;
    server device-management-service-2:8081;
}

server {
    listen 80;
    server_name iot.company.com;

    location /api/ {
        proxy_pass http://api_gateway;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws/ {
        proxy_pass http://api_gateway;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 5. SSL/TLS Configuration
```bash
# Generate SSL certificates
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/ssl/private/iot.key \
  -out /etc/ssl/certs/iot.crt

# Update docker-compose for HTTPS
cat >> docker-compose.prod.yml << EOF
  nginx:
    image: nginx:alpine
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - /etc/ssl/certs/iot.crt:/etc/ssl/certs/iot.crt
      - /etc/ssl/private/iot.key:/etc/ssl/private/iot.key
    depends_on:
      - api-gateway
EOF
```

## Monitoring and Logging

### 1. Application Monitoring

#### Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'iot-services'
    static_configs:
      - targets: ['api-gateway:8080', 'device-management-service:8081', 'device-processor-service:8082']
    metrics_path: '/actuator/prometheus'
```

#### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "IoT Microservices Dashboard",
    "panels": [
      {
        "title": "Service Health",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"iot-services\"}"
          }
        ]
      },
      {
        "title": "Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total[5m])"
          }
        ]
      }
    ]
  }
}
```

### 2. Log Aggregation

#### ELK Stack Configuration
```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:8.8.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:8.8.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
```

### 3. Health Checks
```bash
# Custom health check script
#!/bin/bash
# health-check.sh

services=("api-gateway:8080" "device-management-service:8081" "device-processor-service:8082")

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    
    if curl -f -s http://localhost:$port/actuator/health > /dev/null; then
        echo "✅ $name is healthy"
    else
        echo "❌ $name is unhealthy"
        # Send alert
        curl -X POST http://localhost:8080/api/notifications \
          -H "Content-Type: application/json" \
          -d '{"type":"EMAIL","title":"Service Down","message":"'$name' is not responding"}'
    fi
done
```

## Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check logs
docker-compose logs api-gateway

# Check service dependencies
docker-compose ps

# Restart specific service
docker-compose restart api-gateway
```

#### 2. Database Connection Issues
```bash
# Check PostgreSQL
docker-compose exec postgres pg_isready -U iot_user

# Check InfluxDB
curl http://localhost:8086/health

# Reset databases
docker-compose down -v
docker-compose up -d postgres influxdb
```

#### 3. MQTT Connection Issues
```bash
# Test MQTT connection
mosquitto_pub -h localhost -t "test/topic" -m "test message"

# Check MQTT logs
docker-compose logs mosquitto

# Restart MQTT broker
docker-compose restart mosquitto
```

#### 4. Kafka Issues
```bash
# Check Kafka topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer groups
docker-compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Reset Kafka
docker-compose down -v
docker-compose up -d zookeeper kafka
```

### Performance Tuning

#### 1. JVM Tuning
```bash
# Add to docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### 2. Database Tuning
```sql
-- PostgreSQL tuning
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
```

#### 3. Kafka Tuning
```yaml
# kafka configuration
environment:
  KAFKA_NUM_NETWORK_THREADS: 8
  KAFKA_NUM_IO_THREADS: 16
  KAFKA_SOCKET_SEND_BUFFER_BYTES: 102400
  KAFKA_SOCKET_RECEIVE_BUFFER_BYTES: 102400
  KAFKA_SOCKET_REQUEST_MAX_BYTES: 104857600
```

### Backup and Recovery

#### 1. Database Backup
```bash
# PostgreSQL backup
docker-compose exec postgres pg_dump -U iot_user iot_devices > backup_$(date +%Y%m%d_%H%M%S).sql

# InfluxDB backup
docker-compose exec influxdb influx backup /backup/backup_$(date +%Y%m%d_%H%M%S)
```

#### 2. Configuration Backup
```bash
# Backup configurations
tar -czf config_backup_$(date +%Y%m%d_%H%M%S).tar.gz \
  docker-compose.yml \
  mosquitto/config/ \
  init-scripts/ \
  .env
```

#### 3. Recovery Procedures
```bash
# Restore PostgreSQL
docker-compose exec postgres psql -U iot_user iot_devices < backup_20240115_103000.sql

# Restore InfluxDB
docker-compose exec influxdb influx restore /backup/backup_20240115_103000
```

## Maintenance

### Regular Maintenance Tasks

#### 1. Log Rotation
```bash
# Setup logrotate
cat > /etc/logrotate.d/iot-services << EOF
/var/log/iot-services/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
}
EOF
```

#### 2. Database Maintenance
```sql
-- Weekly maintenance
VACUUM ANALYZE;
REINDEX DATABASE iot_devices;

-- Monthly maintenance
VACUUM FULL;
```

#### 3. System Updates
```bash
# Update system packages
apt update && apt upgrade -y

# Update Docker images
docker-compose pull
docker-compose up -d

# Clean up old images
docker image prune -f
```

This deployment guide provides comprehensive instructions for deploying the IoT microservices system in various environments, from local development to production deployment with proper monitoring, security, and maintenance procedures.
