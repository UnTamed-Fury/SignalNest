# Changelog

All notable changes documented here. Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [2.0.0] — 2026-03-23

### Added — Phase 2: DSL Engine + App improvements

**SNRL (SignalNest Rule Language)**
- Full rule language: lexer, parser (AST), execution engine, validator
- 6 operators: `=` `!=` `CONTAINS` `STARTSWITH` `ENDSWITH` `MATCHES` (regex)
- `AND` / `OR` condition combining
- `{{field}}` template interpolation in mutations
- Mutable fields: `title`, `body`, `group`, `category`, `source`
- REST API: `GET/POST/PATCH/DELETE /app/rules` + `POST /app/rules/validate`
- Rules applied to every inbound webhook before storage and push
- Rules UI in app: list, create, edit, live validate, enable/disable toggle

**Search**
- Full-text search across event title, body, source, and group
- Live results with 200 ms debounce
- Search accessible from Feed top bar

**Export / Import**
- JSON backup of all events, notes, todos, and SNRL rules
- SAF file picker (system file chooser) for save/open
- Merge-import strategy — existing items not deleted
- Accessible from Settings → Backup & Restore

**Server**
- `/webhook` now runs SNRL rule pipeline before broadcasting
- Server version bumped to `2.0.0`
- `DELETE /app/events` endpoint to clear buffer

**App**
- `SnrlRule` Room entity + DAO
- `AppDatabase` bumped to version 2
- `@Serializable` added to `Event`, `Note`, `Todo`, `SnrlRule`
- Bottom nav hides with slide animation on Phase 2 screens
- Settings: new "Automation" (Rules) and "Data" (Backup) sections

---

## [1.0.0] — 2026-03-21

### Added — Phase 1: Core system

- Android app: Jetpack Compose + Material 3, dark/AMOLED/light/dynamic themes
- Server: Node.js + Express + WebSocket on Render
- Password-based auth (no user accounts)
- Real-time event push via WebSocket with auto-reconnect
- LAN mode: NanoHTTPD embedded server receives webhooks on the local network
- Feed: events with grouping, filtering, pin, mark read, copy payload
- Notes: staggered grid, colour chips, Markdown content
- Todos: priority, due dates, cron expressions, WorkManager alarms
- RSS: configurable feed polling, silent/normal notifications
- Boot receiver: service restarts on device reboot
- GitHub webhook auto-parser: push, PR, issues, releases, workflow_run, star, fork, etc.
- Room database: Event, Note, Todo, RssFeed entities
- DataStore preferences: server URL, token, theme, notification settings
- ProGuard rules for release builds
- ABI splits: arm64-v8a, armeabi-v7a, x86_64, x86, universal
- GitHub Actions CI + release workflow (draft releases with SHA-256 checksums)
- Termux ARM64 build script with aapt2 override
