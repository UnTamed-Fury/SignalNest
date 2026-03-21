# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial monorepo structure
- Android app with Jetpack Compose
- Node.js server with Express + WebSocket
- 4-theme system (Light, Gray, Dark, OLED)
- LAN mode for standalone operation
- WebSocket real-time notifications
- JWT authentication
- Webhook endpoint for integrations

### Changed
- Migrated from mr_notifier to signalnest-monorepo
- Updated to Kotlin 2.0.0
- Updated to AGP 8.6.0
- Updated to Gradle 9.4.0

### Fixed
- Initial release

---

## [1.0.0] - 2026-03-21

### Added
- Initial release of SignalNest Monorepo
- Android app with Material 3 design
- Server with REST API and WebSocket
- Real-time notification delivery
- Unread/read notification management
- Pull-to-sync functionality
- Settings screen for configuration
- Build script for unified building
- pnpm workspace configuration
- GitHub Actions CI workflow
- Comprehensive documentation

### Security
- JWT authentication with bcrypt password hashing
- Rate limiting (100 req/15min)
- Helmet.js security headers
- CORS configuration
- Input validation

---

## Previous Versions (mr_notifier)

### [2.4.0] - 2026-02-23
- 4-Theme System
- Sync upsert logic
- No login required

### [2.3.0] - 2026-02-22
- Unread/Read notification system
- Badge count
- Mark all as read

### [2.2.0] - 2026-02-21
- No login required
- Immediate home screen
- Configurable server URL

### [2.1.0] - 2026-02-20
- FCM push notifications
- Sync endpoint
- In-memory storage

### [2.0.0] - 2026-02-19
- Jetpack Compose UI
- Material 3 design
- Modern architecture

### [1.0.0] - 2026-02-18
- Initial release
- Basic notification display
- Simple server setup

---

**Legend:**
- 🚀 Added - New features
- 🔧 Changed - Changes in existing functionality
- 🐛 Fixed - Bug fixes
- ⚡ Security - Security improvements
