# SignalNest Documentation

> Webhook notifications — no cloud, no Firebase, no account required.

---

## 📚 Sections

| File | What's in it |
|------|-------------|
| [SETUP.md](guides/SETUP.md) | Deploy server + build app from scratch |
| [API.md](api/API.md) | Full REST + WebSocket API reference |
| [SNRL.md](snrl/SNRL.md) | SignalNest Rule Language guide |
| [WEBHOOK_EXAMPLES.md](../docs/WEBHOOK_EXAMPLES.md) | Copy-paste examples for GitHub, Grafana, Python, etc. |
| [ENV.md](ENV.md) | Environment variables reference |
| [ROADMAP.md](roadmap/ROADMAP.md) | Development timeline |
| [CHANGELOG.md](changelog/CHANGELOG.md) | Version history |

---

## Architecture

```
[Any service / script / GitHub]
         │
         │  POST /webhook  (public, no auth)
         ▼
 ┌─────────────────────────┐
 │  signalnest-server      │  Node.js + Express + WebSocket
 │  Render / self-hosted   │  SNRL rule engine
 │  env: PASSWORD=...      │
 └────────────┬────────────┘
              │  WebSocket push  (/ws?token=...)
              ▼
 ┌─────────────────────────┐
 │  SignalNest Android App │  Kotlin + Jetpack Compose
 │  Feed · Notes · Todos   │  Room DB · DataStore
 │  RSS · SNRL Rules       │  WorkManager alarms
 └─────────────────────────┘
```

No Firebase. No Google services. Events go server → WebSocket → app instantly.

---

## Quick start (5 minutes)

### 1. Deploy the server on Render

1. Push this repo to GitHub
2. [Render](https://render.com) → New Web Service → connect repo
3. Root directory: `signalnest-server`
4. Build: `npm install`  Start: `npm start`
5. Add env var `PASSWORD=your_password`
6. Deploy → note your URL (`https://your-app.onrender.com`)

### 2. Build the app (Termux)

```bash
source ~/.profile          # load ANDROID_HOME, JAVA_HOME
bash build.sh debug
# APK lands in Downloads/signalnest-debug.apk
```

### 3. Connect

1. Install APK → tap **Get Started**
2. Enter your Render URL + password → **Connect**
3. Done. Events appear instantly.

### 4. Send your first event

```bash
curl -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello SignalNest","message":"It works!"}'
```

---

## Project layout

```
signalnest-monorepo/
├── signalnest-server/        Node.js Express server
│   ├── src/
│   │   ├── snrl/             SNRL rule engine (lexer/parser/engine)
│   │   ├── webhook/          Inbound webhook handler
│   │   ├── ws/               WebSocket server
│   │   ├── auth/             Password → JWT token
│   │   └── events/           In-memory event ring buffer
│   └── render.yaml
├── signalnest-app/           Android app (Kotlin + Compose)
│   └── app/src/main/java/com/signalnest/app/
│       ├── data/             Room DB, DataStore, ExportManager
│       ├── network/          OkHttp API client
│       ├── server/           WsClient, ConnectionService, LanServer
│       ├── ui/               Screens, ViewModels, Theme
│       └── worker/           WorkManager todo alarms
├── build.sh                  Termux build script
└── docs/                     This documentation
```
