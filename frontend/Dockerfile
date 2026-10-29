# --- Build Stage ---
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package manifests first for efficient layer caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy source code and build Vite production bundle
COPY . .
ARG VITE_API_BASE_URL=""
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# --- Production Runtime Stage (Nginx) ---
FROM nginx:alpine AS runner

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
