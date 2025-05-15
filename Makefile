SERVICES = \
    apigateway \
    code_execution_service \
    code_management_service \
    eureka_server \
    ollama-problem-generator

.PHONY: build-all $(SERVICES)

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

# Optional: Individual build targets if you want to build one service
# This uses a Make loop to generate targets, or you can list them manually.
# $(foreach service,$(SERVICES), \
# $(eval .PHONY: $(service)) \
# $(eval $(service): ; @echo "Building $(service)..."; @(cd ./$(service) && ./mvnw clean package)) \
# )

