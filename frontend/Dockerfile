# ===== Build stage =====
FROM node:20-alpine AS build

WORKDIR /app

# Utilisation du proxy nexus : npm
ARG USE_NEXUS_NPM=false
COPY .npmrc /tmp/.npmrc
RUN if [ "$USE_NEXUS_NPM" = "true" ]; then mv /tmp/.npmrc .npmrc; else rm /tmp/.npmrc; fi

COPY package.json .
RUN npm install
COPY . .
RUN npm run build -- --configuration=production

# ===== Run stage =====
FROM nginx:alpine
COPY --from=build /app/dist/tsp-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
