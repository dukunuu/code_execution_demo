SERVICES = \
    apigateway \
    code_execution_service \
    code_management_service \
    eureka_server \
    ollama-problem-generator

.PHONY: build-all $(SERVICES) up down clean run

build-all:
	@echo "--- Building all services ---"
	@for service_dir in $(SERVICES); do \
		echo "Building $$service_dir..."; \
		if (cd ./$$service_dir && ./mvnw clean package); then \
			echo "Successfully built $$service_dir."; \
		else \
			echo "ERROR: Failed to build $$service_dir."; \
			exit 1; \
		fi; \
	done
	@echo "--- All services built successfully ---"

up:
	docker compose up --build -d

down:
	docker compose down

clean:
	docker compose down -v
	docker system prune -f

run:
	@if [ -z "$(service)" ]; then \
		echo "Please specify a service: make run service=<service-name>"; \
	else \
		docker compose up -d $(service); \
	fi
