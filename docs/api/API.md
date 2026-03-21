# SignalNest API Reference

## Base URL

```
Development: http://localhost:3000
Production:  https://your-server.com
```

## Authentication

Most endpoints require JWT authentication:

```
Authorization: Bearer <your-jwt-token>
```

---

## Endpoints

### Health

#### `GET /`

Server info.

**Response:**
```json
{
  "name": "SignalNest Server",
  "version": "1.0.0",
  "status": "running",
  "timestamp": "2026-03-21T12:00:00.000Z"
}
```

#### `GET /health`

Health check.

**Response:**
```json
{
  "status": "healthy",
  "uptime": 12345.67,
  "memory": { "rss": 123456789 }
}
```

---

### Auth

#### `POST /auth/register`

Register new user.

**Request:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**Response (201):**
```json
{
  "user": {
    "id": "uuid-here",
    "username": "testuser",
    "email": "test@example.com",
    "createdAt": "2026-03-21T12:00:00.000Z"
  }
}
```

#### `POST /auth/login`

Login.

**Request:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid-here",
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

#### `GET /auth/me`

Get current user.

**Headers:** `Authorization: Bearer <token>`

**Response (200):**
```json
{
  "id": "uuid-here",
  "username": "testuser",
  "email": "test@example.com"
}
```

---

### Notifications

#### `GET /api/notifications`

List notifications.

**Headers:** `Authorization: Bearer <token>`

**Query Params:**
- `limit` (default: 50)
- `offset` (default: 0)
- `unreadOnly` (default: false)

**Response:**
```json
{
  "notifications": [
    {
      "id": "uuid",
      "title": "Build Failed",
      "body": "CI build #123 failed",
      "sourceType": "github",
      "type": "error",
      "isRead": false,
      "pinned": false,
      "createdAt": 1234567890000,
      "updatedAt": 1234567890000
    }
  ],
  "total": 1
}
```

#### `POST /api/notifications`

Create notification.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "title": "New Notification",
  "body": "Notification body",
  "sourceType": "api",
  "type": "info",
  "metadata": { "key": "value" }
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "title": "New Notification",
  "body": "Notification body",
  "sourceType": "api",
  "type": "info",
  "isRead": false,
  "pinned": false,
  "createdAt": 1234567890000
}
```

#### `GET /api/notifications/unread/count`

Get unread count.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{ "count": 5 }
```

#### `PATCH /api/notifications/:id/read`

Mark as read.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "notification": {
    "id": "uuid",
    "title": "Notification",
    "isRead": true,
    "updatedAt": 1234567890000
  }
}
```

#### `PATCH /api/notifications/:id/unread`

Mark as unread.

**Headers:** `Authorization: Bearer <token>`

#### `PATCH /api/notifications/mark-all-read`

Mark all as read.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{ "markedCount": 5 }
```

#### `DELETE /api/notifications/:id`

Delete notification.

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

#### `PATCH /api/notifications/:id/pin`

Toggle pin.

**Headers:** `Authorization: Bearer <token>`

---

### Webhook

#### `POST /api/webhook`

Create notification via webhook.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "title": "Build Failed",
  "body": "CI build #123 failed",
  "sourceType": "github",
  "type": "error",
  "metadata": {}
}
```

**Response (201):**
```json
{
  "notification": {
    "id": "uuid",
    "title": "Build Failed",
    "body": "CI build #123 failed",
    "sourceType": "github",
    "type": "error",
    "isRead": false,
    "createdAt": 1234567890000
  }
}
```

---

### WebSocket

Connect to: `ws://localhost:3000/ws?token=JWT_TOKEN`

#### Client → Server Messages

**Ping:**
```json
{ "type": "ping" }
```

**Get Notifications:**
```json
{ "type": "get_notifications" }
```

**Get Unread Count:**
```json
{ "type": "get_unread_count" }
```

#### Server → Client Messages

**Connected:**
```json
{
  "type": "connected",
  "userId": "uuid",
  "username": "testuser",
  "timestamp": 1234567890000
}
```

**Pong:**
```json
{ "type": "pong", "timestamp": 1234567890000 }
```

**New Notification:**
```json
{
  "type": "new_notification",
  "notification": { ... }
}
```

**Notification Updated:**
```json
{
  "type": "notification_updated",
  "notification": { ... }
}
```

**All Read:**
```json
{ "type": "all_read", "count": 5 }
```

---

## Errors

### 400 Bad Request
```json
{ "error": "Title and body are required" }
```

### 401 Unauthorized
```json
{ "error": "No token provided" }
```

### 404 Not Found
```json
{ "error": "Not Found", "path": "/api/unknown" }
```

### 429 Too Many Requests
```json
{ "error": "Too many requests, please try again later" }
```

### 500 Internal Server Error
```json
{ "error": "Internal server error" }
```

---

## Rate Limiting

**Default:** 100 requests per 15 minutes per IP

**Headers:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1234567890
```

---

## Quick Test

```bash
# Register
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123"}'

# Login (save token)
TOKEN=$(curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}' | jq -r .token)

# Create notification
curl -X POST http://localhost:3000/api/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Test","body":"Hello!"}'

# Get notifications
curl -X GET http://localhost:3000/api/notifications \
  -H "Authorization: Bearer $TOKEN"

# Mark all as read
curl -X PATCH http://localhost:3000/api/notifications/mark-all-read \
  -H "Authorization: Bearer $TOKEN"
```
