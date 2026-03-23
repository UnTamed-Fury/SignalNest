# Roadmap

## Vision

SignalNest is a personal webhook notification hub. No cloud accounts, no Firebase, no subscriptions. Deploy a server, install the app, get notified.

---

## Q1 2026 — Phase 1 ✅ Complete

- [x] Core server (Node.js + Express + WebSocket)
- [x] Android app (Kotlin + Jetpack Compose + Material 3)
- [x] Password-based auth — no user accounts
- [x] Real-time WebSocket push + auto-reconnect
- [x] LAN mode (NanoHTTPD embedded server)
- [x] GitHub webhook auto-parser (10+ event types)
- [x] Feed with grouping, pinning, silent/normal categories
- [x] Markdown notes, todo list with alarms, RSS reader
- [x] 4 themes (system / light / dark / AMOLED)
- [x] ABI splits + GitHub Actions release workflow
- [x] Termux ARM64 build support

---

## Q2 2026 — Phase 2 ✅ Complete

- [x] SNRL lexer + parser + AST
- [x] Rule execution engine
- [x] Rule validator with field checks
- [x] In-memory rule store (CRUD)
- [x] REST API for rule management
- [x] Rules applied to webhook pipeline
- [x] Rules UI in app (create / edit / validate / toggle)
- [x] Full-text event search
- [x] Export / Import (JSON backup of events + notes + todos + rules)

---

## Q3 2026 — Phase 3: Adapters & persistence 🚧

### Server
- [ ] PostgreSQL / SQLite persistent storage (rules survive restarts)
- [ ] SNRL: `DROP` mutation to discard events
- [ ] SNRL: parentheses in conditions
- [ ] SNRL: `NOTIFY` action override (custom notification title/body separate from event)
- [ ] Slack app integration
- [ ] Discord webhook adapter
- [ ] Email digest (SMTP)

### App
- [ ] Widget (recent events on home screen)
- [ ] Do Not Disturb schedule (quiet hours)
- [ ] Per-group notification channel settings
- [ ] Swipe-to-dismiss on feed cards
- [ ] Bulk event actions (select + delete/pin/mark)

---

## Q4 2026 — Phase 4: Platform expansion 📋

- [ ] iOS app (SwiftUI)
- [ ] Web dashboard (React / SvelteKit)
- [ ] Desktop app (Tauri)
- [ ] Multi-device sync (one server → multiple phones)

---

## 2027+ — Long-term 📋

- [ ] AI event prioritisation
- [ ] Plugin / extension system
- [ ] Self-hostable cloud edition
- [ ] Watch app (Wear OS)

---

## Status

| Component | Phase | Status |
|-----------|-------|--------|
| Server core | 1 | ✅ Complete |
| Android app | 1 | ✅ Complete |
| WebSocket real-time | 1 | ✅ Complete |
| GitHub adapter | 1 | ✅ Complete |
| RSS reader | 1 | ✅ Complete |
| SNRL engine | 2 | ✅ Complete |
| Event search | 2 | ✅ Complete |
| Export / Import | 2 | ✅ Complete |
| Persistent rule storage | 3 | 🚧 Planned |
| Slack / Discord adapters | 3 | 🚧 Planned |
| iOS app | 4 | 📋 Future |
| Web dashboard | 4 | 📋 Future |
