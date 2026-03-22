# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-03-22

### Added
- **Android App** - Jetpack Compose UI with Material 3 design
- **Server** - Node.js + Express webhook relay server
- **Push Notifications** - Real-time notifications via WebSocket
- **RSS Feed** - Built-in RSS reader for following updates
- **Todo Lists** - Task management with alarm support
- **Notes** - Quick note-taking functionality
- **Settings** - App configuration and preferences
- **LAN Server** - Local network server for device communication
- **GitHub Actions CI/CD** - Automated builds and releases
- **ABI Split APKs** - 5 architecture-specific builds for smaller downloads:
  - `arm64-v8a` (recommended for modern phones)
  - `armeabi-v7a` (older phones)
  - `x86_64` (emulator / Intel phones)
  - `x86` (old emulator / Intel phones)
  - `universal` (all architectures)

### Changed
- Initial stable release
- Production-ready build with R8 minification and ProGuard rules

### Technical
- **Frontend Stack:**
  - Kotlin 1.9.20
  - Jetpack Compose with Material 3
  - Room Database for local storage
  - DataStore for preferences
  - OkHttp for networking
  - Kotlinx Serialization for JSON
  - WorkManager for background tasks

- **Backend Stack:**
  - Node.js 20+
  - Express.js for HTTP server
  - WebSocket for real-time push
  - JWT for authentication
  - Helmet for security headers
  - CORS enabled for cross-origin requests

- **Build System:**
  - Gradle 8.4
  - AGP 8.x
  - pnpm workspaces
  - GitHub Actions for CI/CD

### Security
- Password-based authentication for app connection
- JWT token-based API authentication
- Rate limiting on API endpoints
- HTTPS support (when deployed with SSL)

---

## [Unreleased]

### Planned
- [ ] Signed release builds (keystore configuration pending)
- [ ] Play Store release preparation
- [ ] Additional notification customization options
- [ ] Dark/Light theme toggle in settings
- [ ] Backup and restore functionality
- [ ] Multi-account support
- [ ] Widget support for home screen

---

## Version History

| Version | Date       | Status  |
|---------|------------|---------|
| 1.0.0   | 2026-03-22 | Released |

---

## Release Notes

### v1.0.0 - Initial Release

This is the first stable release of SignalNest, featuring:

- **Complete Android app** with modern Material You design
- **Webhook relay server** for receiving GitHub notifications
- **Real-time push** via WebSocket connection
- **RSS feed reader** for following updates
- **Todo management** with alarm support
- **Notes** for quick reminders
- **LAN server** for local network communication

**Download:** Visit the [Releases page](https://github.com/UnTamed-Fury/SignalNest/releases) to download the APK for your device architecture.

**Installation:**
1. Download the APK for your architecture (recommended: `arm64-v8a`)
2. Enable "Install from unknown sources" in Android settings
3. Open the APK and install
4. Launch the app and complete onboarding
5. Connect to your SignalNest server using the password

**Server Setup:**
1. Clone this repository
2. Install dependencies: `pnpm install`
3. Set environment variables: `PASSWORD=your_password PORT=8080`
4. Start the server: `pnpm start`
5. Configure webhooks to point to your server URL

---

*For more information, see the [README.md](README.md) and [docs/](docs/) folder.*
