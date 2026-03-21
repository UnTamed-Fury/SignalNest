# Webhook Examples

## Basic JSON

```bash
curl -X POST https://your-app.onrender.com/webhook \
  -H "Content-Type: application/json" \
  -d '{"title":"Hello","message":"World"}'
```

## With grouping and category

```json
{
  "title": "Server alert",
  "message": "CPU at 95%",
  "source": "monitoring",
  "group": "alerts",
  "category": "normal"
}
```

Set `"silent": true` for no sound/vibration.

## GitHub — Connect any repo

1. Repo → Settings → Webhooks → Add webhook
2. Payload URL: `https://your-app.onrender.com/webhook`
3. Content type: `application/json`
4. Select events: push, pull requests, issues, releases, workflow runs

## Uptime Kuma

- Notification type: Webhook
- URL: `https://your-app.onrender.com/webhook`
- Body:
```json
{
  "title": "{{name}} is {{status}}",
  "message": "{{msg}}",
  "group": "uptime"
}
```

## Grafana

- Contact point type: Webhook
- URL: `https://your-app.onrender.com/webhook`
- Message template:
```json
{
  "title": "[{{.Status}}] {{.GroupLabels.alertname}}",
  "message": "{{range .Alerts}}{{.Annotations.summary}}{{end}}",
  "group": "grafana"
}
```

## Python

```python
import requests
requests.post('https://your-app.onrender.com/webhook',
    json={'title': 'Build done', 'message': 'All tests passed'})
```

## RSS (in-app)

Add any RSS/Atom feed URL in the RSS tab.
SignalNest polls it on your configured interval and pushes new items as events.
