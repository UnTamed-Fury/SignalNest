# SignalNest API Reference v2.0

## Base URL

```
Local:       http://localhost:3000
Production:  https://your-app.onrender.com
```

## Authentication

The server uses a single `PASSWORD` env var. The app exchanges it for a JWT token once at setup. All protected endpoints require:

```
Authorization: Bearer <token>
```

---

## App endpoints (require token)

### `POST /app/connect`

Exchange password for a WS token. Called once during onboarding.

**Request:**
```json
{ "password": "your_server_password" }
```

**Response 200:**
```json
{ "token": "eyJ...", "ok": true }
```

**Response 401:**
```json
{ "error": "Invalid password" }
```

---

### `GET /app/events`

Pull buffered events (last 200 by default). Used to catch up on missed events.

**Query params:** `?limit=200` (max 500)

**Response:**
```json
{
  "events": [
    {
      "id": "uuid",
      "title": "Push to main",
      "body": "3 commits by user",
      "source": "myorg/myrepo",
      "group": "myorg",
      "category": "normal",
      "channel": "remote",
      "rawPayload": "{...}",
      "ts": 1234567890000
    }
  ]
}
```

---

### `DELETE /app/events`

Clear the server-side event buffer.

**Response 200:** `{ "ok": true }`

---

### `GET /app/rules`

List all SNRL rules.

**Response:**
```json
{
  "rules": [
    {
      "id": "uuid",
      "name": "Label GitHub CI",
      "text": "WHEN source = \"github\" AND title CONTAINS \"workflow\"\nTHEN group = \"ci\", title = \"⚙️ {{title}}\"",
      "enabled": true,
      "order": 0,
      "createdAt": "2026-03-23T00:00:00.000Z",
      "updatedAt": "2026-03-23T00:00:00.000Z"
    }
  ]
}
```

---

### `POST /app/rules`

Create a new SNRL rule.

**Request:**
```json
{
  "name": "Silent monitoring",
  "text": "WHEN group = \"uptime\" THEN category = \"silent\""
}
```

**Response 201:** `{ "rule": { ... } }`

**Response 400:** `{ "error": "SNRL parse error: expected THEN, got EOF" }`

---

### `GET /app/rules/:id`

Get one rule by ID.

---

### `PATCH /app/rules/:id`

Update rule. All fields optional.

**Request:**
```json
{
  "name": "New name",
  "text": "WHEN source = \"grafana\" THEN group = \"alerts\"",
  "enabled": false,
  "order": 2
}
```

**Response 200:** `{ "rule": { ... } }`

---

### `DELETE /app/rules/:id`

Delete a rule. **Response 204.**

---

### `POST /app/rules/validate`

Validate rule syntax without saving.

**Request:** `{ "text": "WHEN source = \"x\" THEN group = \"y\"" }`

**Response 200 (valid):**
```json
{ "ok": true, "warnings": [] }
```

**Response 200 (invalid):**
```json
{ "ok": false, "error": "SNRL parse error: expected THEN, got EOF" }
```

---

## Webhook endpoint (public, no auth)

### `POST /webhook`

The main inbound endpoint. Any JSON body. No authentication required.

**GitHub webhooks** are auto-detected via the `x-github-event` header and parsed into structured events.

**Field mapping for generic JSON:**

| Priority | Field name(s) | Maps to |
|----------|--------------|---------|
| 1st | `title`, `summary`, `subject`, `alertname` | `event.title` |
| 2nd | `message`, `body`, `description`, `text`, `msg` | `event.body` |
| 3rd | `source`, `service`, `host`, `from` | `event.source` |
| 4th | `group`, `category`, `namespace` | `event.group` |
| — | `silent: true` | `category = "silent"` (no sound) |

**Response 202:**
```json
{ "ok": true, "id": "uuid" }
```

**SNRL rules are applied** to the event before it is stored and pushed to the app.

---

## WebSocket

Connect: `wss://your-app.onrender.com/ws?token=<token>`

The server sends existing buffered events immediately on connect (as `"events"`), then pushes new ones as `"event"` in real time.

### Server → Client

| Type | Payload | When |
|------|---------|------|
| `events` | `{ data: Event[] }` | On connect — catch-up batch |
| `event`  | `{ data: Event }`   | New event arrives |
| `pong`   | `{ ts: number }`    | In response to `ping` |

### Client → Server

| Type | When to send |
|------|-------------|
| `ping` | Keepalive (client-initiated, optional) |

---

## Other endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Server info + version |
| `GET` | `/health` | `{ ok: true, uptime: N }` |

---

## Errors

All errors return JSON:
```json
{ "error": "Human-readable message" }
```

| Code | Meaning |
|------|---------|
| 400 | Bad request / validation failed |
| 401 | Missing or invalid token |
| 404 | Route not found |
| 429 | Rate limit exceeded (120 req/min) |
| 500 | Server error |
