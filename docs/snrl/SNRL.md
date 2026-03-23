# SNRL — SignalNest Rule Language

SNRL lets you transform incoming events **on the server** before they reach your phone. Rules run in order, each one seeing the output of the previous.

---

## Syntax

```
WHEN <condition>
THEN <mutation> [, <mutation> ...]
```

Everything is **case-insensitive** for keywords and operators. String values are **case-insensitive** in comparisons.

---

## Conditions

### Fields you can check

| Field | Type | Example values |
|-------|------|----------------|
| `title` | string | `"Push to main"`, `"Build failed"` |
| `body` | string | commit message, alert text |
| `source` | string | `"myorg/myrepo"`, `"grafana"`, custom |
| `group` | string | `"default"`, `"ci"`, `"uptime"` |
| `category` | string | `"normal"`, `"silent"` |
| `channel` | string | `"remote"`, `"lan"` |

### Operators

| Operator | Example | Meaning |
|----------|---------|---------|
| `=` | `source = "github"` | Exact match (case-insensitive) |
| `!=` | `category != "silent"` | Not equal |
| `CONTAINS` | `title CONTAINS "fail"` | Substring |
| `STARTSWITH` | `source STARTSWITH "myorg"` | Prefix |
| `ENDSWITH` | `title ENDSWITH ".apk"` | Suffix |
| `MATCHES` | `title MATCHES "^Build #\d+"` | Regex (JS-style, `i` flag) |

### Combining

Use `AND` / `OR` to combine predicates. Evaluated **left-to-right** (no parentheses yet):

```
WHEN source = "github" AND title CONTAINS "push" AND group != "bot"
```

---

## Mutations

### Fields you can set

| Field | Effect |
|-------|--------|
| `title` | Override event title shown in feed + notification |
| `body` | Override event body |
| `group` | Change which group the event appears in |
| `category` | Set `"normal"` or `"silent"` (silent = no sound/vibration) |
| `source` | Override the source label |

### Template interpolation

Use `{{fieldname}}` in mutation values to reference the **original** event fields:

```
THEN title = "🔀 {{title}} — {{source}}"
```

Available template vars: `title`, `body`, `source`, `group`, `category`, `channel`

---

## Examples

### Silence Uptime Kuma "up" pings

```
WHEN source CONTAINS "uptime" AND title CONTAINS "up"
THEN category = "silent"
```

### Label all GitHub CI events

```
WHEN source CONTAINS "/" AND title CONTAINS "workflow"
THEN group = "ci", title = "⚙️ {{title}}"
```

### Rename a noisy group

```
WHEN group = "default" AND source = "my-script"
THEN group = "scripts"
```

### Urgent alerts from Grafana

```
WHEN source = "grafana" AND title CONTAINS "FIRING"
THEN category = "normal", group = "alerts", title = "🚨 {{title}}"
```

### Mark everything from a bot as silent

```
WHEN source ENDSWITH "-bot"
THEN category = "silent"
```

### Regex — catch version tags

```
WHEN title MATCHES "v\d+\.\d+\.\d+"
THEN group = "releases", title = "🚀 {{title}}"
```

---

## Managing rules

### Via the Android app

Settings → **SNRL Rules** → tap `+`

- Write your rule in the text field
- Tap **Validate** to check syntax before saving
- Toggle the switch to enable/disable without deleting

### Via the API

```bash
TOKEN="eyJ..."  # from /app/connect

# Create
curl -X POST https://your-app.onrender.com/app/rules \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Label GitHub CI",
    "text": "WHEN source CONTAINS \"/\" AND title CONTAINS \"workflow\"\nTHEN group = \"ci\""
  }'

# List
curl https://your-app.onrender.com/app/rules \
  -H "Authorization: Bearer $TOKEN"

# Validate without saving
curl -X POST https://your-app.onrender.com/app/rules/validate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text": "WHEN source = \"x\" THEN group = \"y\""}'

# Disable a rule
curl -X PATCH https://your-app.onrender.com/app/rules/<id> \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"enabled": false}'

# Delete
curl -X DELETE https://your-app.onrender.com/app/rules/<id> \
  -H "Authorization: Bearer $TOKEN"
```

---

## Rule execution order

Rules are applied in ascending `order` value. Each rule sees the **output** of the previous one — so later rules can further transform what earlier rules set.

To change order, PATCH the rule with a new `order` integer.

---

## Limitations (v2)

- No parentheses in conditions — compound logic is left-to-right
- No `DROP` mutation yet (dropping events) — set `category = "silent"` as a workaround
- Rules live in server memory — they reset on server restart (Render free tier). Persistent storage is on the roadmap.
