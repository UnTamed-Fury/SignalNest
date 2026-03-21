# SignalNest Setup Guide

Complete setup instructions for SignalNest development.

---

## Prerequisites

- Node.js 18+
- Android Studio / Termux with Gradle
- Firebase account
- Vercel account (for deployment)

---

## 1. Firebase Setup

### Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click **"Add project"**
3. Name: `SignalNest`
4. Disable Google Analytics (optional)

### Add Android App
1. Click **Android icon**
2. Package name: `com.signalnest.app`
3. Download `google-services.json`
4. Place in: `forked-version/app/google-services.json`

### Generate Service Account Key
1. Go to **Project Settings** ⚙️ → **Service accounts**
2. Click **"Generate new private key"**
3. Save as `firebase-credentials.json`
4. Place in: `mr_notifier-server/firebase-credentials.json`

---

## 2. Backend Setup

### Install Dependencies
```bash
cd mr_notifier-server
npm install
```

### Configure Environment
```bash
cp .env.example .env
# Edit .env with your settings
```

### Run Locally
```bash
npm run dev
# Server runs at http://localhost:3000
```

### Deploy to Vercel
```bash
npm install -g vercel
vercel login
vercel --prod
```

### Set Environment Variables (Vercel)
In Vercel Dashboard → Settings → Environment Variables:
- `SECRET_KEY` = your-random-secret-key
- `FIREBASE_CREDENTIALS` = `./firebase-credentials.json`

---

## 3. Android App Setup

### Update API URL
Edit `forked-version/app/src/main/java/com/signalnest/app/data/ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://YOUR-VERCEL-URL.vercel.app/"
```

### Build Debug APK
```bash
cd forked-version
./gradlew assembleDebug
```

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 4. Test the System

### Register User
```bash
curl -X POST https://YOUR-URL.vercel.app/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"testpass123"}'
```

### Login
```bash
curl -X POST https://YOUR-URL.vercel.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"testpass123"}'
```

### Create Notification
```bash
curl -X POST https://YOUR-URL.vercel.app/api/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "source_type": "test",
    "source_name": "manual",
    "title": "Hello!",
    "message": "Test notification"
  }'
```

---

## 5. Troubleshooting

### Backend won't start
- Check Node.js version: `node --version` (needs 18+)
- Check Firebase credentials file exists
- Check port 3000 is not in use

### Android build fails
- Ensure `google-services.json` is in `app/` folder
- Sync Gradle files
- Clean build: `./gradlew clean`

### Push notifications not working
- Verify FCM token is registered
- Check Firebase Console → Cloud Messaging
- Verify `firebase-credentials.json` is correct

---

## 6. Development Workflow

1. Make changes to backend or Android
2. Test locally
3. Commit to Git
4. Deploy backend: `vercel --prod`
5. Build Android: `./gradlew assembleDebug`
6. Test on device

---

**Last Updated:** 2026-02-22
