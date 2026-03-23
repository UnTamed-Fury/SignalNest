# Setup Guide

Complete instructions for deploying SignalNest from scratch.

---

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Node.js | 18+ | `pkg install nodejs` (Termux) |
| Java | 17 | `pkg install openjdk-17` |
| Android SDK | API 34 | via `sdkmanager` |
| aapt2 | any | `pkg install aapt2` |

---

## Server setup

### Option A — Render (recommended, free tier)

1. Push repo to GitHub
2. [render.com](https://render.com) → **New → Web Service**
3. Connect repo, set:
   - **Root Directory:** `signalnest-server`
   - **Build Command:** `npm install`
   - **Start Command:** `npm start`
4. **Environment Variables:**
   ```
   PASSWORD=your_secure_password_here
   NODE_ENV=production
   ```
5. Deploy — note your URL.

### Option B — Self-hosted

```bash
cd signalnest-server && npm install
export PASSWORD="secure_password"
export JWT_SECRET="$(openssl rand -hex 32)"
export NODE_ENV=production
npm start
```

### Option C — Local dev

```bash
cd signalnest-server
cp .env.example .env   # set PASSWORD=
npm install && npm start
```

---

## Android app build

### Termux (ARM64)

```bash
pkg install openjdk-17 aapt2 android-tools
sdkmanager "platforms;android-34" "build-tools;34.0.0"
source ~/.profile
bash build.sh debug    # → Downloads/signalnest-debug.apk
bash build.sh release  # signed when KEYSTORE_FILE is set
```

### Linux / macOS

```bash
cd signalnest-app && chmod +x gradlew
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Signing release builds

```bash
keytool -genkey -v -keystore ~/signalnest.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias signalnest

export KEYSTORE_FILE=~/signalnest.jks KEYSTORE_PASSWORD=pass
export KEY_ALIAS=signalnest KEY_PASSWORD=pass
bash build.sh release
```

---

## App onboarding

1. Install APK → **Get Started**
2. Server URL: `https://your-app.onrender.com`
3. Password: the `PASSWORD` env var you set
4. **Connect** — done

---

## GitHub Actions release

```bash
git tag v1.0.0 && git push --tags
```

Produces 5 APK splits and a **draft** release. Publish manually.

Required repo secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `aapt2` not found | `pkg install aapt2` |
| Gradle OOM | `org.gradle.jvmargs=-Xmx900m` in `gradle.properties` |
| WS reconnects loop | Use `https://` — app auto-converts to `wss://` |
| Render goes to sleep | Ping `/health` every 14 min (Uptime Kuma etc.) |
