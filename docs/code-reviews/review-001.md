# Code Review - 2026-02-22 Session 1

**Reviewer:** Gemini CLI Agent
**Date:** 2026-02-22 17:00:00
**Status:** 🔴 CRITICAL - APP CANNOT COMPILE

---

## 🚨 Critical Blockers (Must Fix Immediately)

### 1. App Compilation Failure (Data Model Mismatch)
The app code in `HomeScreen.kt` is trying to access fields that no longer exist in `LocalData.kt`.
- `notification.message` -> should be `notification.body`
- `notification.sourceName` -> should be `notification.sourceType`
- `notification.category` -> should be `notification.type`

**Action:** Update `HomeScreen.kt` to use the new field names.

### 2. Server Mock Data Isolation
The server's `notifications` route and `sync` route are using different in-memory maps.
- `POST /api/notifications` saves to a local map.
- `POST /api/sync` reads from a shared `mock-store.ts` map.

**Action:** Update `notifications.routes.ts` to import `mockNotifications` from `../../core/db/mock-store.js`.

---

## ⚠️ Major Roadmap Deviations

### 1. Missing Security Layer
The `notifications` and `sync` endpoints are currently public.
- **Roadmap Requirement:** JWT Authentication is mandatory for the "Canonical Tree" architecture.
- **Current State:** No token verification middleware is applied.

### 2. Missing "Local Mirror" Logic
The `HomeViewModel` uses `_notifications.value = notifications` which overwrites the entire list on every sync.
- **Roadmap Requirement:** The app should "Upsert" (Update or Insert) new items into the local database, preserving existing state (like read status).
- **Current State:** Simple list replacement.

---

## 💡 Suggestions & Improvements

1.  **Biometric Lock:** Add `BiometricPrompt` to `MainActivity` on `onResume` to secure the app.
2.  **Server Control:** Add a "Server Status" screen in the app that polls `/health` and allows restarting the server (if running via PM2/Docker).
3.  **Notification Channels:** Create separate channels for "High Priority" and "Low Priority" notifications in `SignalNestApp`.

---
**Next Review Scheduled:** +15 mins
