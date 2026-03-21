# SignalNest Documentation

> Personal event aggregation and notification intelligence system.

---

## 📚 Documentation Sections

| Section | Description |
|---------|-------------|
| [Changelog](changelog/CHANGELOG.md) | Version history and changes |
| [Roadmap](roadmap/ROADMAP.md) | Development phases and timeline |
| [API Reference](api/API.md) | Backend API documentation |
| [Guides](guides/) | How-to guides and tutorials |

---

## 🚀 Quick Start

### Backend (Vercel)

```bash
# Deploy
cd mr_notifier-server
vercel --prod

# Test
curl https://signalnest-api-personal.vercel.app/health
```

### Android App

```bash
# Build
cd forked-version
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📋 System Architecture

```
[ External Sources ]
        ↓
[ SignalNest Server ]
    - FastAPI → Node.js/Express
    - JWT Auth
    - Notification Queue
        ↓
[ Firebase FCM ]
        ↓
[ Android App ]
    - SQLite (offline-first)
    - Material 3 UI
    - Notes + Todos
```

---

## 🔧 Environment Setup

### Backend Variables
```bash
SECRET_KEY=your-secret-key
FIREBASE_CREDENTIALS=./firebase-credentials.json
DATABASE_URL=file:./dev.db
```

### Android Setup
1. Add `google-services.json` to `app/`
2. Update `ApiClient.kt` with backend URL
3. Build and run

---

## 📱 Features

### Current (v0.1.0)
- ✅ User registration/login
- ✅ Push notifications via FCM
- ✅ Notification delivery tracking
- ✅ ACK-based confirmation
- ✅ Sync endpoint

### Coming Soon
- 🔄 Login UI
- 📋 Pin/delete notifications
- 📋 Notes and Todos
- 📋 Search and filtering
- 📋 Settings page

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## 📄 License

ISC License

---

**Last Updated:** 2026-02-22
