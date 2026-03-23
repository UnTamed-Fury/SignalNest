# Environment Variables

## Server (`signalnest-server/.env`)

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `PASSWORD` | **Yes** | — | App authentication password. Set this or the server rejects all connections. |
| `PORT` | No | `3000` | HTTP/WS listen port |
| `HOST` | No | `0.0.0.0` | Listen address |
| `NODE_ENV` | No | `development` | `production` suppresses stack traces in errors |
| `JWT_SECRET` | No | random | Secret for signing tokens. Set explicitly in production — if unset, tokens invalidate on restart. |
| `MAX_EVENTS` | No | `500` | Max events held in the server ring buffer |

### Example `.env`

```bash
PASSWORD=my_super_secure_password
NODE_ENV=production
JWT_SECRET=generate_with_openssl_rand_hex_32
MAX_EVENTS=500
```

Generate a strong secret:
```bash
openssl rand -hex 32
```

---

## Android app (`signalnest-app/local.properties`)

> `local.properties` is **gitignored** — never commit it.

```properties
# Android SDK location
sdk.dir=/data/data/com.termux/files/usr/opt/Android/sdk

# Termux ARM64 only: use native aapt2 instead of Maven's x86_64 binary
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```

Standard Linux/macOS:
```properties
sdk.dir=/home/user/Android/sdk
# (no aapt2 override needed)
```

---

## Android release signing (environment variables)

Set before running `bash build.sh release` or in GitHub Actions secrets:

| Variable | Description |
|----------|-------------|
| `KEYSTORE_FILE` | Absolute path to `.jks` file |
| `KEYSTORE_PASSWORD` | Keystore store password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

---

## Gradle performance (`signalnest-app/gradle.properties`)

The file is **committed** (no secrets). Key settings:

```properties
# CI: 4 GB heap, 2 workers (GitHub Actions free tier = 2 vCPU, 7 GB RAM)
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m

# Termux: override via GRADLE_OPTS before building
# GRADLE_OPTS="-Xmx900m" bash build.sh debug

org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

> **Termux users:** if you hit OOM, run: `export GRADLE_OPTS="-Xmx900m -XX:MaxMetaspaceSize=256m"` before building.
