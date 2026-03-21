# Environment Variables

## Server (.env)

```bash
# Server Configuration
PORT=3000
HOST=0.0.0.0
NODE_ENV=development

# JWT Configuration
# IMPORTANT: Change this in production!
JWT_SECRET=your-super-secret-jwt-key-min-32-chars-long
JWT_EXPIRES_IN=24h

# LAN Server (for Android app embedded server)
LAN_PORT=8080
```

## Android App (local.properties)

```properties
# Android SDK Location (Termux)
sdk.dir=/data/data/com.termux/files/usr/opt/Android/sdk

# Or on standard Linux/Mac:
# sdk.dir=/home/user/Android/sdk
# sdk.dir=/Users/user/Library/Android/sdk
```

## Build Configuration (gradle.properties)

```properties
# Gradle settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true

# Suppress compileSdk warning (if needed)
android.suppressUnsupportedCompileSdk=36
```

## Environment-Specific Settings

### Development

```bash
NODE_ENV=development
PORT=3000
JWT_SECRET=dev-secret-key-change-in-production
```

### Production

```bash
NODE_ENV=production
PORT=3000
JWT_SECRET=<generate-strong-random-key>
# Use environment variable injection from your hosting provider
```

### Testing

```bash
NODE_ENV=test
PORT=3001
JWT_SECRET=test-secret-key
```

## Security Notes

⚠️ **IMPORTANT:**

1. **Never commit `.env` files** - They're in `.gitignore`
2. **Use strong secrets** - Minimum 32 characters
3. **Rotate secrets regularly** - Especially after team changes
4. **Use environment injection** - From Render/Vercel/Heroku
5. **Different secrets per environment** - Dev ≠ Staging ≠ Production

## Generating Secure Secrets

```bash
# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# Using OpenSSL
openssl rand -hex 32

# Using pwgen
pwgen -s 32 1
```

## Example .env.complete

```bash
# Server
PORT=3000
HOST=0.0.0.0
NODE_ENV=production

# JWT
JWT_SECRET=super-secret-key-generated-with-openssl-min-32-chars
JWT_EXPIRES_IN=24h

# Optional: Database (future)
DATABASE_URL=postgresql://user:pass@localhost:5432/signalnest

# Optional: Redis (future)
REDIS_URL=redis://localhost:6379

# Optional: Logging
LOG_LEVEL=info
LOG_FILE=/var/log/signalnest/server.log

# Optional: Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100

# Optional: CORS
CORS_ORIGIN=https://your-domain.com

# LAN Server
LAN_PORT=8080
```

## Validation

The server validates environment variables on startup using Zod schema.

Required variables:
- `PORT` (default: 3000)
- `NODE_ENV` (default: development)
- `JWT_SECRET` (default: dev key - change in production!)

Optional variables have sensible defaults.

---

**See also:** [SECURITY.md](../SECURITY.md)
