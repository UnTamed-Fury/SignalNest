# Webhook Examples

The webhook endpoint is `POST /webhook` — public, no auth.

---

## Minimal

```bash
curl -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","message":"World"}'
```

---

## All fields

```json
{
  "title":    "Server alert",
  "message":  "CPU at 95%",
  "source":   "monitoring",
  "group":    "alerts",
  "silent":   false
}
```

Set `"silent": true` for no notification sound or vibration.

---

## GitHub webhooks

1. Repo → **Settings → Webhooks → Add webhook**
2. **Payload URL:** `https://your-app.onrender.com/webhook`
3. **Content type:** `application/json`
4. **Events:** choose what you want (push, pull_request, issues, releases, workflow_run, etc.)

GitHub events are auto-detected via the `x-github-event` header and formatted beautifully:

| Event | Title example |
|-------|--------------|
| `push` | `🔀 Push to main — myorg/repo` |
| `pull_request` | `🔃 PR #42 opened — myorg/repo` |
| `issues` | `🐛 Issue #7 opened — myorg/repo` |
| `workflow_run` (success) | `✅ CI success — myorg/repo` |
| `workflow_run` (failure) | `❌ CI failure — myorg/repo` |
| `release` | `🚀 Release v1.2.0 — myorg/repo` |
| `star` | `⭐ myorg/repo starred by user` |

---

## Uptime Kuma

- **Notification type:** Webhook
- **URL:** `https://your-app.onrender.com/webhook`
- **Body:**
```json
{
  "title":   "{{name}} is {{status}}",
  "message": "{{msg}}",
  "group":   "uptime",
  "silent":  false
}
```

---

## Grafana Alertmanager

```yaml
receivers:
  - name: signalnest
    webhook_configs:
      - url: https://your-app.onrender.com/webhook
        send_resolved: true
        http_config:
          bearer_token: ""   # no auth needed for /webhook

route:
  receiver: signalnest
```

Grafana sends a JSON body with `alerts[].annotations.summary` — SNRL rules can reshape it:

```
WHEN source = "grafana" AND title CONTAINS "FIRING"
THEN group = "alerts", title = "🚨 {{title}}"
```

---

## Python

```python
import requests

def notify(title, message, group="default", silent=False):
    requests.post(
        "https://your-app.onrender.com/webhook",
        json={"title": title, "message": message, "group": group, "silent": silent},
    )

notify("Deploy done", "v1.2.3 is live", group="releases")
```

---

## Node.js

```javascript
async function notify(title, message, group = "default") {
  await fetch("https://your-app.onrender.com/webhook", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title, message, group }),
  });
}

await notify("Build passed", "All 47 tests green", "ci");
```

---

## Bash / cron job

```bash
#!/bin/bash
curl -sf -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Backup done\",\"message\":\"$(date)\",\"group\":\"cron\"}" \
  || echo "Notification failed"
```

---

## RSS feeds (in-app)

Add any RSS/Atom URL in the **RSS** tab. SignalNest polls it on your configured interval and creates events for new items. Supports RSS 2.0, Atom, and most common formats.
