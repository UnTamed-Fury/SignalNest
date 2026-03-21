# SignalNest Server

Express + WebSocket notification server for SignalNest app.

## Features

- 🔐 JWT Authentication (login/register)
- 🔔 In-memory notification storage (per-user)
- 🔌 WebSocket real-time updates
- 🌐 LAN mode support
- 🛡️ Rate limiting & security headers
- 📡 Webhook endpoint for external services

## Quick Start

### Development

```bash
# Install dependencies
pnpm install

# Start development server (auto-reload)
pnpm run dev

# Server runs on: http://localhost:3000
```

### Production

```bash
# Install dependencies
pnpm install --prod

# Start server
pnpm start
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login user |
| GET | `/auth/me` | Get current user |

### Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications` | List notifications |
| POST | `/api/notifications` | Create notification |
| GET | `/api/notifications/unread/count` | Get unread count |
| PATCH | `/api/notifications/:id/read` | Mark as read |
| PATCH | `/api/notifications/:id/unread` | Mark as unread |
| PATCH | `/api/notifications/mark-all-read` | Mark all as read |
| DELETE | `/api/notifications/:id` | Delete notification |
| PATCH | `/api/notifications/:id/pin` | Toggle pin |

### Webhook

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/webhook` | Create notification via webhook |

### Health

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Server info |
| GET | `/health` | Health check |

## WebSocket

Connect to `ws://localhost:3000/ws?token=YOUR_JWT_TOKEN`

### Messages

**Client → Server:**
```json
{ "type": "ping" }
{ "type": "get_notifications" }
{ "type": "get_unread_count" }
```

**Server → Client:**
```json
{ "type": "connected", "userId": "...", "username": "..." }
{ "type": "pong", "timestamp": 1234567890 }
{ "type": "notifications", "notifications": [...] }
{ "type": "unread_count", "count": 5 }
{ "type": "new_notification", "notification": {...} }
{ "type": "notification_updated", "notification": {...} }
{ "type": "all_read", "count": 5 }
```

## Usage Examples

### Register & Login

```bash
# Register
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Create Notification

```bash
curl -X POST http://localhost:3000/api/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"title":"Test","body":"This is a test notification","sourceType":"api","type":"info"}'
```

### Webhook Integration

```bash
# GitHub Actions example
curl -X POST https://your-server.com/api/webhook \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Build Failed",
    "body": "CI build #123 failed",
    "sourceType": "github",
    "type": "error"
  }'
```

### Grafana Alert

```yaml
# alertmanager.yml
receivers:
  - name: 'signalnest'
    webhook_configs:
      - url: 'http://your-server.com/api/webhook'
        send_resolved: true
        http_config:
          bearer_token: 'YOUR_JWT_TOKEN'
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| PORT | 3000 | Server port |
| HOST | 0.0.0.0 | Server host |
| NODE_ENV | development | Environment mode |
| JWT_SECRET | (auto) | JWT signing secret |
| JWT_EXPIRES_IN | 24h | Token expiration |

## Deploy to Render

1. Push code to GitHub
2. Go to [Render](https://render.com)
3. Click "New +" → "Blueprint"
4. Connect your repository
5. Deploy!

Or use the included `render.yaml`:

```bash
# Render will automatically detect and use render.yaml
```

## License

ISC
