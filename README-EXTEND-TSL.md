# Generate Certificates
### Create certificates directory
```bash
mkdir -p mosquitto/certs
cd mosquitto/certs
```

### Generate CA (Certificate Authority)
openssl genrsa -out ca.key 2048
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt \
-subj "/C=US/ST=State/L=City/O=Organization/CN=Mosquitto-CA"

### Create extension file for server
```bash
cat > server.ext <<EOF
subjectAltName = DNS:localhost,IP:127.0.0.1
extendedKeyUsage = serverAuth
keyUsage = digitalSignature, keyEncipherment
EOF
```
### Generate server certificate
```bash
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
-subj "/C=US/ST=State/L=City/O=Organization/CN=mosquitto"
```
### Sign server certificate with CA
```bash
openssl x509 -req \
-in server.csr \
-CA ca.crt -CAkey ca.key \
-CAcreateserial \
-out server.crt \
-days 365 \
-sha256 \
-extfile server.ext
```
```bash
chmod 640 server.key
chmod 644 server.crt
chown 1883:1883 server.*
```

### Create extension for client cert
```bash
cat > client.ext <<EOF
extendedKeyUsage = clientAuth
keyUsage = digitalSignature
EOF
```

### Generate healthcheck certificate
```bash
openssl genrsa -out healthcheck.key 2048
openssl req -new -key healthcheck.key -out healthcheck.csr \
-subj "/C=US/ST=State/L=City/O=Organization/CN=healthcheck"
```

### Sign healthcheck certificate with CA
```bash
openssl x509 -req \
-in healthcheck.csr \
-CA ca.crt -CAkey ca.key \
-CAcreateserial \
-out healthcheck.crt \
-days 3650 \
-sha256 \
-extfile client.ext
```
```bash
chmod 640 healthcheck.key
chmod 644 healthcheck.crt
chown 1883:1883 healthcheck.*
```

### Verify with OpenSSL
```bash
openssl s_client \
  -connect localhost:8883 \
  -cert healthcheck.crt \
  -key healthcheck.key \
  -CAfile ca.crt
```