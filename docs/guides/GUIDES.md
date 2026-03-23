# Guides

---

## Sending your first webhook

```bash
curl -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","message":"SignalNest works!"}'
```

Open the app — the event appears in the Feed instantly.

---

## Setting up GitHub webhooks

1. Go to your repo → **Settings → Webhooks → Add webhook**
2. **Payload URL:** `https://your-app.onrender.com/webhook`
3. **Content type:** `application/json`
4. **Events:** send me everything (or select specific ones)
5. Save — a `ping` event arrives in your Feed immediately

SignalNest auto-formats GitHub events. A failed workflow run looks like:
> `❌ CI failure — myorg/myrepo`  
> `Branch: main · Triggered by username`

---

## Creating SNRL rules

Rules transform events before they reach your phone.

### In the app

Settings → **SNRL Rules** → `+`

Example — silence all "up" pings from Uptime Kuma:
```
WHEN source CONTAINS "uptime" AND title CONTAINS " up"
THEN category = "silent"
```

Tap **Validate** to check syntax, then **Create**.

### Via API

```bash
TOKEN="your_ws_token"  # from /app/connect response

curl -X POST https://your-app.onrender.com/app/rules \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Label GitHub CI",
    "text": "WHEN source CONTAINS \"/\" AND title CONTAINS \"workflow\"\nTHEN group = \"ci\", title = \"⚙️ {{title}}\""
  }'
```

See [SNRL.md](../snrl/SNRL.md) for the full language reference.

---

## Exporting and restoring your data

1. Settings → **Backup & Restore** → **Choose save location**
2. Pick a folder — creates `signalnest-backup-YYYYMMDD-HHmm.json`

The backup includes events, notes, todos, and SNRL rules. To restore on a new device:
1. Copy the JSON file to the new device
2. Settings → **Backup & Restore** → **Choose backup file**
3. Tap the JSON file — items are merged in

---

## Building from source on Termux

```bash
# 1. Install deps (once)
pkg install openjdk-17 aapt2 android-tools git

# 2. Clone
git clone https://github.com/yourusername/signalnest-monorepo
cd signalnest-monorepo

# 3. Build
source ~/.profile
bash build.sh debug

# APK lands in ~/storage/downloads/signalnest-debug.apk
```

---

## Keeping Render's free tier awake

Render free web services sleep after 15 minutes. Set up Uptime Kuma (or any HTTP monitor) to ping your health endpoint every 14 minutes:

- **URL:** `https://your-app.onrender.com/health`
- **Interval:** 14 minutes
- **Expected status:** 200

This keeps the server warm so webhooks arrive instantly.

---

## Integrating with Python scripts

```python
import requests, os

WEBHOOK = os.getenv("SIGNALNEST_WEBHOOK", "https://your-app.onrender.com/webhook")

def notify(title, body="", group="scripts", silent=False):
    try:
        requests.post(WEBHOOK, json={
            "title": title,
            "message": body,
            "group": group,
            "silent": silent,
        }, timeout=5)
    except Exception as e:
        print(f"SignalNest notify failed: {e}")

# In your script:
notify("Backup completed", f"3.2 GB backed up at {__import__('datetime').datetime.now()}")
```

---

## Integrating with GitHub Actions

```yaml
- name: Notify SignalNest
  if: always()
  run: |
    STATUS="${{ job.status }}"
    EMOJI=$([[ "$STATUS" == "success" ]] && echo "✅" || echo "❌")
    curl -sf -X POST ${{ secrets.SIGNALNEST_WEBHOOK }} \
      -H "Content-Type: application/json" \
      -d "{
        \"title\": \"$EMOJI Build $STATUS\",
        \"message\": \"${{ github.repository }} #${{ github.run_number }}\",
        \"group\": \"ci\"
      }"
```

Add `SIGNALNEST_WEBHOOK` as a repo secret set to `https://your-app.onrender.com/webhook`.
