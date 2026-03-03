.PHONY: help install up down restart logs logs-backend logs-frontend logs-postgres status health clean clean-all db-connect shell-backend test-backend build rebuild install-proxy build-proxy rebuild-proxy

# Couleurs pour l'affichage
BLUE=\033[0;34m
GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m # No Color

# Docker Compose
COMPOSE_FILES ?= -f docker-compose.yml
COMPOSE = docker-compose $(COMPOSE_FILES)

help: ## Affiche cette aide
	@echo "$(BLUE)TSP Evolutionnaire - Commandes disponibles$(NC)"
	@echo ""
	@echo "$(YELLOW)Standard:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*## .*$$' $(MAKEFILE_LIST) | grep -v proxy | sort | awk 'BEGIN {FS = ":.*## "}; {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(YELLOW)Proxy (Nexus):$(NC)"
	@grep -E '.*proxy.*## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*## "}; {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

install: ## Installation complete (premiere fois)
	@echo "$(BLUE)Installation de TSP Evolutionnaire...$(NC)"
	$(COMPOSE) build
	$(COMPOSE) up -d
	@echo "$(GREEN)Installation terminee!$(NC)"
	@echo "Frontend: http://localhost:8080"
	@echo "Backend API: http://localhost:8081/api"
	@echo "PostgreSQL: localhost:5432"

install-proxy: ## Installation avec proxy (Nexus)
	@echo "$(BLUE)Installation avec proxy (Nexus)...$(NC)"
	docker-compose -f docker-compose.yml -f docker-compose-proxy.yml build
	docker-compose -f docker-compose.yml -f docker-compose-proxy.yml up -d
	@echo "$(GREEN)Installation terminee!$(NC)"
	@echo "Frontend: http://localhost:8080"
	@echo "Backend API: http://localhost:8081/api"
	@echo "PostgreSQL: localhost:5432"

up: ## Demarre tous les services
	@echo "$(BLUE)Demarrage des services...$(NC)"
	$(COMPOSE) up -d
	@echo "$(GREEN)Services demarres!$(NC)"

down: ## Arrete et supprime les conteneurs
	@echo "$(YELLOW)Arret des services...$(NC)"
	$(COMPOSE) down -v
	@echo "$(GREEN)Services arretes$(NC)"

restart: ## Redemarrer tous les services
	@echo "$(YELLOW)Redemarrage des services...$(NC)"
	$(COMPOSE) restart
	@echo "$(GREEN)Services redemarres$(NC)"

build: ## Reconstruire les images
	@echo "$(BLUE)Reconstruction des images...$(NC)"
	$(COMPOSE) build
	@echo "$(GREEN)Images reconstruites$(NC)"

build-proxy: ## Reconstruire les images avec proxy (Nexus)
	@echo "$(BLUE)Reconstruction des images avec proxy...$(NC)"
	docker-compose -f docker-compose.yml -f docker-compose-proxy.yml build
	@echo "$(GREEN)Images reconstruites$(NC)"

rebuild: ## Reconstruire les images sans cache
	@echo "$(BLUE)Reconstruction des images (sans cache)...$(NC)"
	$(COMPOSE) build --no-cache
	@echo "$(GREEN)Images reconstruites$(NC)"

rebuild-proxy: ## Reconstruire les images sans cache avec proxy (Nexus)
	@echo "$(BLUE)Reconstruction des images sans cache avec proxy...$(NC)"
	docker-compose -f docker-compose.yml -f docker-compose-proxy.yml build --no-cache
	@echo "$(GREEN)Images reconstruites$(NC)"

logs: ## Voir les logs de tous les services
	$(COMPOSE) logs -f

logs-backend: ## Voir les logs du backend
	$(COMPOSE) logs -f backend

logs-frontend: ## Voir les logs du frontend
	$(COMPOSE) logs -f frontend

logs-postgres: ## Voir les logs de PostgreSQL
	$(COMPOSE) logs -f postgres

status: ## Afficher le statut des services
	@echo "$(BLUE)Statut des services:$(NC)"
	$(COMPOSE) ps

health: ## Verifier la sante des services
	@echo "$(BLUE)Verification de la sante des services...$(NC)"
	@echo ""
	@echo "PostgreSQL:"
	@$(COMPOSE) exec -T postgres pg_isready -U tspuser -d tspdb && echo "$(GREEN)OK$(NC)" || echo "$(RED)ERREUR$(NC)"
	@echo ""
	@echo "Backend:"
	@curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP" && echo "$(GREEN)OK$(NC)" || curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "404" && echo "$(GREEN)OK (running)$(NC)" || echo "$(RED)ERREUR$(NC)"
	@echo ""
	@echo "Frontend:"
	@curl -s -o /dev/null -w "%{http_code}" http://localhost:80 | grep -q "200" && echo "$(GREEN)OK$(NC)" || echo "$(RED)ERREUR$(NC)"

clean: ## Nettoyer les conteneurs et volumes
	@echo "$(YELLOW)Nettoyage...$(NC)"
	$(COMPOSE) down -v
	@echo "$(GREEN)Nettoyage termine$(NC)"

clean-all: ## Nettoyage complet (images incluses)
	@echo "$(RED)Nettoyage complet...$(NC)"
	$(COMPOSE) down -v --rmi all
	@echo "$(GREEN)Nettoyage complet termine$(NC)"

db-connect: ## Se connecter a PostgreSQL
	$(COMPOSE) exec postgres psql -U tspuser -d tspdb

shell-backend: ## Ouvrir un shell dans le conteneur backend
	$(COMPOSE) exec backend sh

shell-frontend: ## Ouvrir un shell dans le conteneur frontend
	$(COMPOSE) exec frontend sh

test-backend: ## Executer les tests backend
	@echo "$(BLUE)Execution des tests backend...$(NC)"
	cd backend && mvn test

dev: ## Mode developpement avec logs
	$(COMPOSE) up --build

psql: ## Connexion a la base de donnees PostgreSQL
	$(COMPOSE) exec postgres psql -U tspuser -d tspdb
