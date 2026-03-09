# Multi-stage build for React Frontend with Nginx
# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production=false

# Copy source code
COPY . .

# Build arguments for environment variables (injected at build time)
ARG VITE_API_BASE_URL=/api/v1
ARG VITE_SOCKET_URL=
ARG VITE_OAUTH_URL=/oauth2/authorization/google

# Set environment variables for build
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_SOCKET_URL=$VITE_SOCKET_URL
ENV VITE_OAUTH_URL=$VITE_OAUTH_URL

# Build the application (skip TypeScript checking for faster Docker builds)
RUN npm run build:docker

# Stage 2: Production with Nginx
FROM nginx:alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf
RUN rm /etc/nginx/nginx.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Create non-root user for security
RUN addgroup -S nginx-user && adduser -S nginx-user -G nginx-user

# Set proper permissions
RUN chown -R nginx-user:nginx-user /usr/share/nginx/html && \
    chown -R nginx-user:nginx-user /var/cache/nginx && \
    chown -R nginx-user:nginx-user /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx-user:nginx-user /var/run/nginx.pid

# Note: Running as root for nginx to bind to port 80
# For rootless, would need to change to port 8080

EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/nginx-health || exit 1

CMD ["nginx", "-g", "daemon off;"]
