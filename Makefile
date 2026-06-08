SHELL := /bin/bash

.DEFAULT_GOAL := help

COMPOSE := docker compose
APP_COMPOSE := $(COMPOSE) -f docker-compose.yml
SYSTEM_COMPOSE := $(COMPOSE) -f docker-compose-system.yml
SYSTEM_TLS_COMPOSE := $(COMPOSE) -f docker-compose-system-tls.yml
SYSTEM_TLS_V2_COMPOSE := $(COMPOSE) -f docker-compose-system-tls-v2.yml
PRE_COMPOSE := $(COMPOSE) -f docker-compose-pre-setup.yml
BACKEND_COMPOSE := $(COMPOSE) -f docker-compose-be.yml

MVN ?= mvn
MVN_REPO_LOCAL ?= /tmp/codex-m2
MVN_SETTINGS ?= .mvn/settings.xml
MVN_CMD := $(MVN) --settings $(MVN_SETTINGS) -Dmaven.repo.local=$(MVN_REPO_LOCAL)
NPM ?= npm
FRONTEND_DIR := frontend
CONNECTOR_DIR := kafka-connect/connectors
CERT_DIR := mosquitto/certs
KAFKA_CONNECT_DIR := kafka-connect

DEVICE_ID ?= iot_device_01
CONNECT_URL ?= http://localhost:8083
KEYSTORE_PASSWORD ?= kafka1234
ROOT_CA_SUBJ ?= /C=US/ST=State/L=City/O=Organization/CN=IoT Root CA
SERVER_CA_SUBJ ?= /C=US/ST=State/L=City/O=Organization/CN=IoT Server CA
DEVICE_CA_SUBJ ?= /C=US/ST=State/L=City/O=Organization/CN=IoT Device CA
SERVER_SUBJ ?= /C=US/ST=State/L=City/O=Organization/CN=mosquitto
HEALTHCHECK_SUBJ ?= /C=US/ST=State/L=City/O=Organization/CN=healthcheck
KAFKA_BRIDGE_SUBJ ?= /C=US/ST=State/L=City/O=Organization/CN=kafka_bridge
DEVICE_SUBJ_PREFIX ?= /C=US/ST=State/L=City/O=Organization/CN=

APP_HEALTH_SERVICES := \
	api-gateway:8080 \
	device-management-service:8081 \
	device-processor-service:8082 \
	analysis-report-service:8083 \
	notification-service:8084

.PHONY: \
	help \
	check \
	dirs \
	build \
	build-backend \
	build-frontend \
	package-backend \
	test \
	test-backend \
	test-frontend \
	test-service \
	frontend-setup \
	frontend-dev \
	frontend-build \
	frontend-test \
	up \
	run \
	up-system \
	up-app \
	up-backend \
	up-pre \
	up-tls \
	up-tls-v2 \
	down \
	down-app \
	down-system \
	down-pre \
	down-tls \
	down-tls-v2 \
	down-clean \
	status \
	logs \
	health \
	smoke \
	connector-deploy \
	certs \
	cert-root-ca \
	cert-server-ca \
	cert-server \
	cert-device-ca \
	cert-healthcheck \
	cert-kafka-bridge \
	cert-device \
	cert-default-device \
	cert-bundles \
	kafka-stores

help: ## Show available targets.
	@awk 'BEGIN {FS = ":.*## "}; /^[a-zA-Z0-9_.-]+:.*## / {printf "%-22s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

check: ## Check core local tools used by the project.
	@set -euo pipefail; \
	for tool in docker curl $(MVN) node $(NPM) openssl keytool; do \
		if ! command -v $$tool >/dev/null 2>&1; then \
			echo "Missing required tool: $$tool"; \
			exit 1; \
		fi; \
	done; \
	echo "Core tools are available."

dirs: ## Create local directories used by Docker and generated assets.
	@mkdir -p $(CERT_DIR) mosquitto/data mosquitto/log $(KAFKA_CONNECT_DIR)

build: build-backend build-frontend ## Build backend modules and the frontend bundle.

build-backend: ## Build all Maven modules.
	$(MVN_CMD) clean install

package-backend: ## Package backend modules without running tests.
	$(MVN_CMD) clean install -DskipTests

build-frontend: frontend-setup ## Build the React frontend bundle.
	cd $(FRONTEND_DIR) && $(NPM) run build

test: test-backend test-frontend ## Run backend and frontend test suites.

test-backend: ## Run all backend tests.
	$(MVN_CMD) test

test-frontend: frontend-setup ## Run frontend tests once.
	cd $(FRONTEND_DIR) && CI=true $(NPM) test -- --watch=false

test-service: ## Run Maven tests for one module. Usage: make test-service SERVICE=device-management-service
	@test -n "$(SERVICE)" || { echo "SERVICE is required. Example: make test-service SERVICE=device-management-service"; exit 1; }
	$(MVN_CMD) -pl $(SERVICE) test

frontend-setup: ## Install frontend dependencies and create frontend/.env when missing.
	@set -euo pipefail; \
	cd $(FRONTEND_DIR); \
	$(NPM) install; \
	if [ ! -f .env ]; then \
		cp env.example .env; \
		echo "Created $(FRONTEND_DIR)/.env from env.example"; \
	fi

frontend-dev: frontend-setup ## Start the React dev server.
	cd $(FRONTEND_DIR) && $(NPM) start

frontend-build: build-frontend ## Alias for build-frontend.

frontend-test: test-frontend ## Alias for test-frontend.

up: up-system up-app ## Start the default local stack.

run: up ## Alias for up.

up-system: dirs ## Start Kafka, Mosquitto, and Kafka Connect.
	$(SYSTEM_COMPOSE) up -d

up-app: dirs ## Start the application stack and build images when needed.
	$(APP_COMPOSE) up -d --build

up-backend: dirs ## Start only backend services from the backend-only compose file.
	$(BACKEND_COMPOSE) up -d --build

up-pre: dirs ## Start the pre-setup infrastructure stack.
	$(PRE_COMPOSE) up -d

up-tls: dirs ## Start the TLS system stack.
	$(SYSTEM_TLS_COMPOSE) up -d

up-tls-v2: certs ## Start the TLS v2 stack and deploy Kafka Connect connectors.
	$(SYSTEM_TLS_V2_COMPOSE) up -d
	$(MAKE) connector-deploy CONNECT_URL=$(CONNECT_URL)

down: down-app down-system ## Stop the default local stack.

down-app: ## Stop the application compose stack.
	$(APP_COMPOSE) down

down-system: ## Stop the system compose stack.
	$(SYSTEM_COMPOSE) down

down-pre: ## Stop the pre-setup compose stack.
	$(PRE_COMPOSE) down

down-tls: ## Stop the TLS system compose stack.
	$(SYSTEM_TLS_COMPOSE) down

down-tls-v2: ## Stop the TLS v2 system compose stack.
	$(SYSTEM_TLS_V2_COMPOSE) down

down-clean: ## Stop app and system stacks and remove named volumes.
	$(APP_COMPOSE) down -v
	$(SYSTEM_COMPOSE) down -v

status: ## Show status for the default app and system compose stacks.
	$(SYSTEM_COMPOSE) ps
	$(APP_COMPOSE) ps

logs: ## Stream logs for a compose service. Usage: make logs SERVICE=api-gateway
	@test -n "$(SERVICE)" || { echo "SERVICE is required. Example: make logs SERVICE=api-gateway"; exit 1; }
	$(APP_COMPOSE) logs -f $(SERVICE)

health: ## Check actuator health endpoints for app services.
	@set -euo pipefail; \
	for service in $(APP_HEALTH_SERVICES); do \
		name="$${service%%:*}"; \
		port="$${service##*:}"; \
		if curl -fsS "http://localhost:$$port/actuator/health" | grep -q '"status"[[:space:]]*:[[:space:]]*"UP"'; then \
			echo "$$name is healthy on $$port"; \
		else \
			echo "$$name health check failed on $$port"; \
			exit 1; \
		fi; \
	done

smoke: health ## Run a basic end-to-end smoke test against the running stack.
	@set -euo pipefail; \
	device_response=$$(curl -fsS -X POST http://localhost:8080/api/devices \
		-H "Content-Type: application/json" \
		-d '{"name":"Test Device","address":"192.168.1.200","type":"SENSOR","status":"ONLINE","factoryId":"test-factory","location":"Test Location"}'); \
	if echo "$$device_response" | grep -q '"id"'; then \
		echo "Device creation succeeded"; \
	else \
		echo "Device creation failed: $$device_response"; \
		exit 1; \
	fi; \
	if command -v mosquitto_pub >/dev/null 2>&1; then \
		mosquitto_pub -h localhost -t "devices/test-device/data" -m '{"temperature":25.5,"pressure":2.1,"vibration":0.5}'; \
		echo "Published MQTT smoke message"; \
	else \
		echo "mosquitto_pub not installed; skipped MQTT smoke message"; \
	fi

connector-deploy: ## Deploy connector JSON files to Kafka Connect. Requires curl and jq.
	@set -euo pipefail; \
	command -v jq >/dev/null 2>&1 || { echo "jq is required for connector deployment"; exit 1; }; \
	ready=0; \
	for attempt in $$(seq 1 30); do \
		status=$$(curl -s -o /dev/null -w "%{http_code}" "$(CONNECT_URL)/connectors" || true); \
		if [ "$$status" = "200" ]; then \
			ready=1; \
			break; \
		fi; \
		sleep 2; \
	done; \
	if [ "$$ready" -ne 1 ]; then \
		echo "Kafka Connect at $(CONNECT_URL) did not become ready in time"; \
		exit 1; \
	fi; \
	found=0; \
	for connector in $(CONNECTOR_DIR)/*.json; do \
		[ -f "$$connector" ] || continue; \
		found=1; \
		name=$$(jq -r '.name' "$$connector"); \
		status=$$(curl -s -o /dev/null -w "%{http_code}" "$(CONNECT_URL)/connectors/$$name" || true); \
		echo "Deploying $$name"; \
		if [ "$$status" = "200" ]; then \
			config=$$(jq -c '.config' "$$connector"); \
			curl -fsS -X PUT -H "Content-Type: application/json" -d "$$config" "$(CONNECT_URL)/connectors/$$name/config" >/dev/null; \
		else \
			curl -fsS -X POST -H "Content-Type: application/json" -d "@$$connector" "$(CONNECT_URL)/connectors" >/dev/null; \
		fi; \
		status_ready=0; \
		for attempt in $$(seq 1 15); do \
			status_code=$$(curl -s -o /tmp/codex-connector-status.json -w "%{http_code}" "$(CONNECT_URL)/connectors/$$name/status" || true); \
			if [ "$$status_code" = "200" ]; then \
				status_ready=1; \
				break; \
			fi; \
			sleep 1; \
		done; \
		if [ "$$status_ready" -ne 1 ]; then \
			echo "Connector $$name did not become ready in time"; \
			exit 1; \
		fi; \
		jq '.' /tmp/codex-connector-status.json; \
	done; \
	if [ "$$found" -ne 1 ]; then \
		echo "No connector files found in $(CONNECTOR_DIR)"; \
		exit 1; \
	fi

certs: ## Generate local TLS certificates, a default device cert, and Kafka Connect stores.
	@$(MAKE) cert-root-ca
	@$(MAKE) cert-server-ca
	@$(MAKE) cert-server
	@$(MAKE) cert-device-ca
	@$(MAKE) cert-healthcheck
	@$(MAKE) cert-kafka-bridge
	@$(MAKE) cert-default-device
	@$(MAKE) cert-bundles
	@$(MAKE) kafka-stores

cert-root-ca: dirs ## Generate the root CA certificate and key.
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/rootCA.crt ] && [ -f $(CERT_DIR)/rootCA.key ]; then \
		echo "Root CA already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/rootCA.key 4096; \
		openssl req -x509 -new -nodes -key $(CERT_DIR)/rootCA.key -sha256 -days 3650 -subj "$(ROOT_CA_SUBJ)" -out $(CERT_DIR)/rootCA.crt; \
		chmod 640 $(CERT_DIR)/rootCA.key; \
		chmod 644 $(CERT_DIR)/rootCA.crt; \
	fi

cert-server-ca: cert-root-ca ## Generate the intermediate CA used to sign the broker certificate.
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/serverCA.crt ] && [ -f $(CERT_DIR)/serverCA.key ]; then \
		echo "Server CA already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/serverCA.key 4096; \
		openssl req -new -key $(CERT_DIR)/serverCA.key -subj "$(SERVER_CA_SUBJ)" -out $(CERT_DIR)/serverCA.csr; \
		printf '%s\n' \
			'basicConstraints = CA:TRUE,pathlen:0' \
			'keyUsage = critical, keyCertSign, cRLSign' \
			'subjectKeyIdentifier = hash' \
			'authorityKeyIdentifier = keyid:always,issuer' > $(CERT_DIR)/serverCA.ext; \
		openssl x509 -req -in $(CERT_DIR)/serverCA.csr -CA $(CERT_DIR)/rootCA.crt -CAkey $(CERT_DIR)/rootCA.key -CAcreateserial -out $(CERT_DIR)/serverCA.crt -days 3650 -sha256 -extfile $(CERT_DIR)/serverCA.ext; \
		chmod 640 $(CERT_DIR)/serverCA.key; \
		chmod 644 $(CERT_DIR)/serverCA.crt; \
	fi

cert-server: cert-server-ca ## Generate the Mosquitto server certificate and key.
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/server.crt ] && [ -f $(CERT_DIR)/server.key ]; then \
		echo "Broker server certificate already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/server.key 2048; \
		openssl req -new -key $(CERT_DIR)/server.key -out $(CERT_DIR)/server.csr -subj "$(SERVER_SUBJ)"; \
		printf '%s\n' \
			'basicConstraints = CA:FALSE' \
			'keyUsage = digitalSignature, keyEncipherment' \
			'extendedKeyUsage = serverAuth' \
			'subjectAltName = @alt_names' \
			'' \
			'[alt_names]' \
			'DNS.1 = mosquitto' \
			'DNS.2 = localhost' \
			'IP.1 = 127.0.0.1' > $(CERT_DIR)/server.ext; \
		openssl x509 -req -in $(CERT_DIR)/server.csr -CA $(CERT_DIR)/serverCA.crt -CAkey $(CERT_DIR)/serverCA.key -CAcreateserial -out $(CERT_DIR)/server.crt -days 365 -sha256 -extfile $(CERT_DIR)/server.ext; \
		chmod 640 $(CERT_DIR)/server.key; \
		chmod 644 $(CERT_DIR)/server.crt; \
	fi

cert-device-ca: cert-root-ca ## Generate the CA used to sign MQTT client certificates.
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/deviceCA.crt ] && [ -f $(CERT_DIR)/deviceCA.key ]; then \
		echo "Device CA already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/deviceCA.key 4096; \
		openssl req -new -key $(CERT_DIR)/deviceCA.key -subj "$(DEVICE_CA_SUBJ)" -out $(CERT_DIR)/deviceCA.csr; \
		printf '%s\n' \
			'basicConstraints = CA:TRUE,pathlen:0' \
			'keyUsage = critical, keyCertSign, cRLSign' \
			'subjectKeyIdentifier = hash' \
			'authorityKeyIdentifier = keyid:always,issuer' > $(CERT_DIR)/deviceCA.ext; \
		openssl x509 -req -in $(CERT_DIR)/deviceCA.csr -CA $(CERT_DIR)/rootCA.crt -CAkey $(CERT_DIR)/rootCA.key -CAcreateserial -out $(CERT_DIR)/deviceCA.crt -days 3650 -sha256 -extfile $(CERT_DIR)/deviceCA.ext; \
		chmod 640 $(CERT_DIR)/deviceCA.key; \
		chmod 644 $(CERT_DIR)/deviceCA.crt; \
	fi; \
	if [ ! -f $(CERT_DIR)/device.ext ]; then \
		printf '%s\n' \
			'basicConstraints = CA:FALSE' \
			'keyUsage = digitalSignature' \
			'extendedKeyUsage = clientAuth' > $(CERT_DIR)/device.ext; \
	fi

cert-healthcheck: cert-device-ca ## Generate the healthcheck client certificate.
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/healthcheck.crt ] && [ -f $(CERT_DIR)/healthcheck.key ]; then \
		echo "Healthcheck certificate already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/healthcheck.key 2048; \
		openssl req -new -key $(CERT_DIR)/healthcheck.key -out $(CERT_DIR)/healthcheck.csr -subj "$(HEALTHCHECK_SUBJ)"; \
		openssl x509 -req -in $(CERT_DIR)/healthcheck.csr -CA $(CERT_DIR)/deviceCA.crt -CAkey $(CERT_DIR)/deviceCA.key -CAcreateserial -out $(CERT_DIR)/healthcheck.crt -days 180 -sha256 -extfile $(CERT_DIR)/device.ext; \
		chmod 640 $(CERT_DIR)/healthcheck.key; \
		chmod 644 $(CERT_DIR)/healthcheck.crt; \
	fi

cert-kafka-bridge: cert-device-ca ## Generate the Kafka Connect MQTT bridge client certificate.
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/kafka_bridge.crt ] && [ -f $(CERT_DIR)/kafka_bridge.key ]; then \
		echo "Kafka bridge certificate already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/kafka_bridge.key 2048; \
		openssl req -new -key $(CERT_DIR)/kafka_bridge.key -out $(CERT_DIR)/kafka_bridge.csr -subj "$(KAFKA_BRIDGE_SUBJ)"; \
		openssl x509 -req -in $(CERT_DIR)/kafka_bridge.csr -CA $(CERT_DIR)/deviceCA.crt -CAkey $(CERT_DIR)/deviceCA.key -CAcreateserial -out $(CERT_DIR)/kafka_bridge.crt -days 180 -sha256 -extfile $(CERT_DIR)/device.ext; \
		chmod 644 $(CERT_DIR)/kafka_bridge.key; \
		chmod 644 $(CERT_DIR)/kafka_bridge.crt; \
	fi; \
	chmod 644 $(CERT_DIR)/kafka_bridge.key $(CERT_DIR)/kafka_bridge.crt

cert-device: cert-device-ca ## Generate one MQTT device certificate. Usage: make cert-device DEVICE_ID=device-001
	@set -euo pipefail; \
	if [ -f $(CERT_DIR)/$(DEVICE_ID).crt ] && [ -f $(CERT_DIR)/$(DEVICE_ID).key ]; then \
		echo "Device certificate $(DEVICE_ID) already exists"; \
	else \
		openssl genrsa -out $(CERT_DIR)/$(DEVICE_ID).key 2048; \
		openssl req -new -key $(CERT_DIR)/$(DEVICE_ID).key -out $(CERT_DIR)/$(DEVICE_ID).csr -subj "$(DEVICE_SUBJ_PREFIX)$(DEVICE_ID)"; \
		openssl x509 -req -in $(CERT_DIR)/$(DEVICE_ID).csr -CA $(CERT_DIR)/deviceCA.crt -CAkey $(CERT_DIR)/deviceCA.key -CAcreateserial -out $(CERT_DIR)/$(DEVICE_ID).crt -days 180 -sha256 -extfile $(CERT_DIR)/device.ext; \
		chmod 644 $(CERT_DIR)/$(DEVICE_ID).key; \
		chmod 644 $(CERT_DIR)/$(DEVICE_ID).crt; \
	fi; \
	chmod 644 $(CERT_DIR)/$(DEVICE_ID).key $(CERT_DIR)/$(DEVICE_ID).crt

cert-default-device: ## Generate the default local device certificate.
	@$(MAKE) cert-device DEVICE_ID=$(DEVICE_ID)

cert-bundles: cert-root-ca cert-server-ca cert-device-ca ## Generate certificate bundle files for clients and Mosquitto.
	@set -euo pipefail; \
	if [ ! -f $(CERT_DIR)/ca-bundle.crt ]; then \
		cat $(CERT_DIR)/rootCA.crt $(CERT_DIR)/serverCA.crt > $(CERT_DIR)/ca-bundle.crt; \
		chmod 644 $(CERT_DIR)/ca-bundle.crt; \
	else \
		echo "CA bundle already exists"; \
	fi; \
	if [ ! -f $(CERT_DIR)/device-ca-bundle.crt ]; then \
		cat $(CERT_DIR)/rootCA.crt $(CERT_DIR)/deviceCA.crt > $(CERT_DIR)/device-ca-bundle.crt; \
		chmod 644 $(CERT_DIR)/device-ca-bundle.crt; \
	else \
		echo "Device CA bundle already exists"; \
	fi

kafka-stores: cert-kafka-bridge cert-root-ca cert-server-ca ## Generate the Kafka Connect keystore and truststore.
	@set -euo pipefail; \
	if [ ! -f $(KAFKA_CONNECT_DIR)/kafka_bridge.p12 ]; then \
		openssl pkcs12 -export -in $(CERT_DIR)/kafka_bridge.crt -inkey $(CERT_DIR)/kafka_bridge.key -out $(KAFKA_CONNECT_DIR)/kafka_bridge.p12 -name kafka_bridge -passout pass:$(KEYSTORE_PASSWORD); \
	fi; \
	if [ ! -f $(KAFKA_CONNECT_DIR)/truststore.jks ]; then \
		keytool -importcert -file $(CERT_DIR)/rootCA.crt -alias root-ca -keystore $(KAFKA_CONNECT_DIR)/truststore.jks -storepass $(KEYSTORE_PASSWORD) -noprompt; \
		keytool -importcert -file $(CERT_DIR)/serverCA.crt -alias server-ca -keystore $(KAFKA_CONNECT_DIR)/truststore.jks -storepass $(KEYSTORE_PASSWORD) -noprompt; \
	fi; \
	if [ ! -f $(KAFKA_CONNECT_DIR)/keystore.jks ]; then \
		keytool -importkeystore -srckeystore $(KAFKA_CONNECT_DIR)/kafka_bridge.p12 -srcstoretype PKCS12 -srcstorepass $(KEYSTORE_PASSWORD) -destkeystore $(KAFKA_CONNECT_DIR)/keystore.jks -deststorepass $(KEYSTORE_PASSWORD) -noprompt; \
	fi
