# ===== Build stage =====
FROM node:20-alpine AS build

WORKDIR /app

# Utilisation du proxy nexus : npm
ARG USE_NEXUS_NPM=false
COPY .npmrc /tmp/.npmrc
RUN if [ "$USE_NEXUS_NPM" = "true" ]; then mv /tmp/.npmrc .npmrc; else rm /tmp/.npmrc; fi

# Copier les fichiers de configuration
COPY package.json ./
COPY tsconfig.json ./
COPY tsconfig.app.json ./
COPY angular.json ./

# Installer les dépendances
RUN npm install --legacy-peer-deps --verbose

# Copier le code source
COPY src ./src

# Build l'application
RUN npm run build -- --configuration production

# Vérifier le contenu du dossier dist
RUN ls -la /app/dist/ && ls -la /app/dist/tsp-frontend/ && ls -la /app/dist/tsp-frontend/browser || true

# ===== Run stage =====
FROM nginx:alpine

# Copier les fichiers buildés (Angular application builder -> /browser)
COPY --from=build /app/dist/tsp-frontend/browser /usr/share/nginx/html

# Copier la configuration nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
