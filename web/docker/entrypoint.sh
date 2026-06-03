#!/bin/sh
set -e

APP_DIR=/var/www/html

# Write Firebase credentials from env var if provided
if [ -n "$FIREBASE_CREDENTIALS_JSON" ]; then
    echo "$FIREBASE_CREDENTIALS_JSON" > "$APP_DIR/firebase-service-account.json"
fi

# Ensure storage and cache dirs are writable
chown -R www-data:www-data "$APP_DIR/storage" "$APP_DIR/bootstrap/cache"
chmod -R 775 "$APP_DIR/storage" "$APP_DIR/bootstrap/cache"

cd "$APP_DIR"

# Bootstrap Laravel
php artisan key:generate --no-interaction --force 2>/dev/null || true
php artisan config:cache
php artisan route:cache
php artisan view:cache
php artisan migrate --force --no-interaction 2>/dev/null || true

exec /usr/bin/supervisord -c /etc/supervisord.conf
