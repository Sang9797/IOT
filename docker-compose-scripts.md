# Run docker compose up system
```bash
docker compose -f docker-compose-system-tls-v2.yml up -d
```

# Run docker compose up pre-setup
```bash
docker compose -f docker-compose-pre-setup.yml up -d
```

# Run docker compose up backend service
```bash
docker compose -f docker-compose-be.yml up -d
```

# Run docker compose down system
```bash
docker compose -f docker-compose-system-tls-v2.yml down -v
```

# Run docker compose down pre-setup
```bash
docker compose -f docker-compose-pre-setup.yml down -v
```

# Run docker compose down backend service
```bash
docker compose -f docker-compose-be.yml down -v
```