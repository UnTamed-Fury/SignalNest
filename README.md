# SignalNest

**Webhook notifications — no cloud, no Firebase, no bullshit.**

Receive webhooks from anywhere (GitHub, Uptime Kuma, Grafana, curl, Python...)
and get instant push notifications on your Android device.

## Architecture

```
[Any Service] ──POST /webhook──▶ [signalnest-server on Render]
                                          │
                                    WebSocket push
                                          │
                                          ▼
                                  [SignalNest Android App]
```

## Server setup (5 minutes)

1. Fork this repo or push to GitHub
2. Create a new Web Service on [Render](https://render.com)
3. Set the `PASSWORD` environment variable to a secure string
4. Deploy — your webhook URL is `https://your-app.onrender.com/webhook`

## App setup

1. Install the APK (build from source or download release)
2. Open app → enter your Render URL + PASSWORD → tap Connect
3. Done. Events will appear instantly.

## Webhook usage

```bash
# Basic
curl -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d '{"title":"Deploy done","message":"prod is live"}'

# With grouping and silent notification
curl -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d '{"title":"Low priority","message":"FYI only","group":"alerts","silent":true}'
```

## GitHub webhooks

Point any repo's webhook to `https://your-app.onrender.com/webhook`.
Content type: `application/json`. No secret needed (or add one via Render env).

Supported events: push, pull_request, issues, release, workflow_run,
check_run, star, fork, issue_comment, create, delete, deployment_status, ping.

## Features

- 📬 Receive webhooks from any service
- ⚡ Instant WebSocket push to app
- 🐙 GitHub webhook auto-parsing (10+ event types)
- 📱 Feed with grouping, pinning, silent/normal categories
- 📝 Markdown notes
- ✅ Todo list with due dates, priorities, cron reminders
- 📡 RSS feed reader
- 🌙 Dark / AMOLED / Light themes
- 🔁 Auto-reconnect WebSocket
- 🚀 Starts on device boot

## License

SignalNest Source License v1.0 — see [LICENSE](LICENSE).
Personal use: free. Commercial use: contact the author.
