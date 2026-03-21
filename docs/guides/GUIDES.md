# SignalNest Guides

## Quick Setup (5 minutes)

### 1. Install & Run Server

```bash
git clone https://github.com/yourusername/signalnest-monorepo.git
cd signalnest-monorepo
pnpm install
pnpm run server:dev
```

Server: `http://localhost:3000`

### 2. Build App

**Desktop:**
```bash
cd signalnest-app
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Termux:** See [BUILD_STATUS.md](../BUILD_STATUS.md)

### 3. Configure App

1. Open SignalNest
2. Settings → Server URL
3. Enter: `http://10.0.2.2:3000/` (emulator) or `http://192.168.x.x:3000/` (device)
4. Save

---

## Server Deployment

### Render (Recommended)

1. Push to GitHub
2. [Render](https://render.com) → New + → Blueprint
3. Connect repo → Deploy

### Vercel

```bash
cd signalnest-server
vercel --prod
```

### Self-Hosted

```bash
pnpm install --prod
export JWT_SECRET="your-secret-key-min-32-chars"
export NODE_ENV=production
pnpm start
```

---

## Integrations

### GitHub Actions

```yaml
name: Notify SignalNest
on:
  workflow_run:
    workflows: ["CI"]
    types: [completed]

jobs:
  notify:
    runs-on: ubuntu-latest
    steps:
      - name: Send notification
        run: |
          curl -X POST ${{ secrets.SIGNALNEST_URL }}/api/webhook \
            -H "Authorization: Bearer ${{ secrets.SIGNALNEST_TOKEN }}" \
            -H "Content-Type: application/json" \
            -d '{
              "title": "Build ${{ github.event.workflow_run.conclusion }}",
              "body": "Build #${{ github.run_number }}",
              "sourceType": "github",
              "type": "${{ github.event.workflow_run.conclusion == 'success' && 'success' || 'error' }}"
            }'
```

### Grafana Alertmanager

```yaml
# alertmanager.yml
receivers:
  - name: 'signalnest'
    webhook_configs:
      - url: 'http://your-server.com/api/webhook'
        send_resolved: true
        http_config:
          bearer_token: 'YOUR_JWT_TOKEN'

route:
  receiver: 'signalnest'
  group_by: ['alertname']
```

### Node.js

```javascript
const fetch = require('node-fetch');

async function notify(title, body, type = 'info') {
  await fetch('https://your-server.com/api/webhook', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer YOUR_JWT_TOKEN`
    },
    body: JSON.stringify({ title, body, sourceType: 'custom', type })
  });
}

notify('Test', 'Hello from Node.js!', 'success');
```

### Python

```python
import requests

def notify(title, body, type='info'):
    requests.post(
        'https://your-server.com/api/webhook',
        headers={
            'Content-Type': 'application/json',
            'Authorization': 'Bearer YOUR_JWT_TOKEN'
        },
        json={'title': title, 'body': body, 'sourceType': 'python', 'type': type}
    )

notify('Test', 'Hello from Python!', 'success')
```

### cURL

```bash
# Get token
TOKEN=$(curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}' | jq -r .token)

# Send notification
curl -X POST http://localhost:3000/api/webhook \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Alert","body":"Something happened","type":"warning"}'
```

---

## Troubleshooting

### Server won't start

```bash
# Check Node.js version
node --version  # Must be >= 18

# Check port
lsof -i :3000

# Check logs
pnpm run server:dev 2>&1 | tee server.log
```

### App build fails

```bash
# Check Java
java -version  # Must be 17+

# Check Android SDK
echo $ANDROID_HOME
ls $ANDROID_HOME/platforms

# Clean build
cd signalnest-app
./gradlew clean
./gradlew assembleDebug
```

### WebSocket not connecting

1. Verify server is running
2. Check token is valid
3. Check firewall settings
4. Test: `ws://localhost:3000/ws?token=YOUR_TOKEN`

### Notifications not appearing

1. Grant notification permission
2. Verify server URL in Settings
3. Pull to sync manually
4. Check server logs

---

## FAQ

**Q: Can I use the app without a server?**  
A: Yes! Use "LAN Mode" for standalone operation.

**Q: How do I backup notifications?**  
A: Notifications are stored in-app. Export feature coming soon.

**Q: Can I customize themes?**  
A: 4 themes available: Light, Gray, Dark, OLED.

**Q: Is end-to-end encryption supported?**  
A: Not yet. Use HTTPS for remote connections.

---

For more help, see [CONTRIBUTING.md](../CONTRIBUTING.md) or open an issue.
