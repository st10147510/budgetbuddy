FROM php:8.3-fpm-alpine AS base

# System deps
RUN apk add --no-cache \
    nginx \
    supervisor \
    sqlite \
    sqlite-dev \
    libpq \
    postgresql-dev \
    libzip-dev \
    zip \
    unzip \
    curl \
    nodejs \
    npm \
    && docker-php-ext-install pdo pdo_sqlite pdo_pgsql zip opcache

# Composer
COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

WORKDIR /var/www/html

# ── Dependencies (cached layer) ───────────────────────────────────────────────
FROM base AS deps

COPY composer.json composer.lock ./
RUN composer install --no-dev --no-scripts --no-autoloader --prefer-dist

COPY package.json package-lock.json ./
RUN npm ci

# ── Build assets ──────────────────────────────────────────────────────────────
FROM deps AS builder

COPY . .
RUN composer dump-autoload --optimize --no-dev
RUN npm run build

# ── Production image ──────────────────────────────────────────────────────────
FROM base AS production

COPY --from=builder /var/www/html /var/www/html

# Config files
COPY docker/nginx.conf       /etc/nginx/http.d/default.conf
COPY docker/supervisord.conf /etc/supervisord.conf
COPY docker/entrypoint.sh    /entrypoint.sh
RUN chmod +x /entrypoint.sh

RUN chown -R www-data:www-data /var/www/html/storage /var/www/html/bootstrap/cache

EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
