# SignalNest - Complete System Blueprint

**Created:** 2026-02-22  
**Status:** Architecture Complete  
**Model:** GPT-5.2 | Mode: Think longer

---

## 🧠 SYSTEM IDENTITY

**SignalNest = Configurable Event OS**

- **Server** → Execution + Storage
- **App** → Control + DSL authoring + Local mirror
- **Language** → Human-readable rule engine

---

## 🗺️ GLOBAL ARCHITECTURE

```
┌────────────────────┐
│   External Apps    │
│ GitHub Discord ... │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│     Server         │
│  Raw Event Store   │
│  DSL Engine        │
│  Canonical Tree DB │
│  Sync Engine       │
└─────────┬──────────┘
          ↓
┌────────────────────┐
│     Android App    │
│  Control Center    │
│  Local Mirror DB   │
│  Notes / Todos     │
│  Pin System        │
└────────────────────┘
```

---

## 🟦 APP SYSTEM DESIGN

### App = Control Plane + Local Mirror

### 1️⃣ APP DATA MODEL

Local Room DB mirrors server but extends it.

**Core Tables:**
- users
- sources
- repositories
- commits
- issues
- pull_requests
- stars
- forks
- notifications
- notes
- todos
- pins
- sync_meta
- schemas

**Notification Extended Model:**
```
notifications
- id
- entity_type
- entity_id
- title
- body
- type (error|warn|log|info)
- priority (low|medium|high|critical)
- pinned (bool)
- note_id (nullable)
- todo_id (nullable)
- is_read
- deleted_at
- sync_version
```

**Notes Table:**
```
notes
- id
- notification_id
- content
- created_at
- updated_at
```

**Todos Table:**
```
todos
- id
- notification_id
- title
- completed
- due_date
- priority
```

**Pin System:**
```
pins
- notification_id
- pinned_at
```

### 2️⃣ APP RESPONSIBILITIES

**The app will:**
- Authenticate user
- Manage sources
- Author DSL schemas
- Upload schemas to server
- Manage notes & todos
- Manage pinning
- Trigger manual sync
- Visualize logs
- Mirror canonical tree
- Resolve conflicts
- Export/import schemas

**App does NOT:**
- Parse GitHub payload
- Store raw events
- Maintain global truth

### 3️⃣ APP FLOW

```
First Launch
   ↓
Register/Login
   ↓
Add Source (GitHub/Discord)
   ↓
Enable Preconfigured Schema
   ↓
Server starts receiving events
   ↓
App Sync
   ↓
Events appear
   ↓
User pins / adds note / converts to todo
```

### 4️⃣ APP NAVIGATION DIAGRAM

```
Navigation Drawer
│
├── Feed
├── Pinned
├── Todos
├── Sources
├── Schemas
├── Sync Monitor
├── Logs
├── Settings
└── Account
```

### 5️⃣ FEED STRUCTURE

```
┌──────────────────────────────┐
│ ERROR  PR Merge Failed       │
│ Repo: api-server             │
│ [Pin] [Note] [Todo]          │
└──────────────────────────────┘
```

Pinned items appear at top.

---

## 🟧 SERVER SYSTEM DESIGN

### Server = Always-on execution + storage engine

### 1️⃣ SERVER RESPONSIBILITIES

**Server will:**
- Receive webhooks
- Validate signatures
- Store raw events
- Execute DSL
- Build canonical entities
- Maintain tree structure
- Maintain sync_version
- Serve sync API
- Maintain idempotency
- Store user schemas
- Execute notification rules

**Server does NOT:**
- Manage UI
- Author DSL
- Store notes content logic
- Control presentation

### 2️⃣ SERVER FILE SYSTEM STRUCTURE

```
server/
│
├── core/
│   ├── server
│   ├── logger
│   ├── config
│
├── db/
│   ├── schema.sql
│   ├── migrations/
│   ├── connection
│
├── modules/
│   ├── auth/
│   ├── sources/
│   ├── repositories/
│   ├── commits/
│   ├── issues/
│   ├── pull_requests/
│   ├── stars/
│   ├── forks/
│   ├── notifications/
│   ├── sync/
│   └── raw_events/
│
├── dsl/
│   ├── lexer
│   ├── parser
│   ├── validator
│   ├── compiler
│   └── executor
│
├── adapters/
│   ├── github/
│   ├── discord/
│   └── custom/
│
└── routes/
```

### 3️⃣ SERVER TREE STRUCTURE (Supabase)

```
platform
 └── source
      └── repository
           ├── commits
           ├── issues
           ├── pull_requests
           ├── stars
           └── forks
```

Each entity has:
- sync_version
- deleted_at
- user_id

### 4️⃣ SERVER EVENT FLOW

```
Webhook
   ↓
Validate
   ↓
Insert raw_event
   ↓
Detect Schema
   ↓
Execute DSL
   ↓
Normalize entity
   ↓
Upsert canonical tree
   ↓
Increment sync_version
   ↓
Ready for sync
```

Push notification optional. Sync is primary transport.

---

## 🟨 PRECONFIGURED SCHEMAS

### 1️⃣ GITHUB SCHEMA

```snrl
schema "GitHubDefault" {
  source: github
}

map {
  repository.name        -> entity.repo_name
  repository.full_name   -> entity.repo_full
  sender.login           -> metadata.actor
}

rules {
  when event == "push" {
    set notification.type = log
    set notification.title = "New commits pushed"
    set priority = medium
  }

  when event == "issues" and action == "opened" {
    set notification.type = warn
    set notification.title = "New issue opened"
    set priority = high
  }

  when event == "pull_request" and action == "closed" {
    set notification.type = info
    set notification.title = "Pull request closed"
  }
}

notify {
  title: "{notification.title}"
  body:  "Repo: {entity.repo_name}\nBy: {metadata.actor}"
  group: "{entity.repo_full}"
}
```

### 2️⃣ DISCORD SCHEMA

```snrl
schema "DiscordMessages" {
  source: discord
}

map {
  author.username  -> metadata.user
  content          -> entity.message
  channel.name     -> metadata.channel
}

rules {
  when entity.message contains "@everyone" {
    set notification.type = critical
    set priority = high
    set auto_pin = true
  }
}

notify {
  title: "Discord: {metadata.channel}"
  body:  "{metadata.user}: {entity.message}"
  group: "{metadata.channel}"
}
```

### 3️⃣ CUSTOM WEBHOOK SCHEMA

```snrl
schema "GenericWebhook" {
  source: webhook
}

map {
  level     -> notification.type
  message   -> notification.title
  service   -> metadata.service
}

rules {
  when notification.type == "error" {
    set priority = critical
    set auto_pin = true
  }
}

notify {
  title: "{notification.title}"
  body:  "Service: {metadata.service}"
  group: "{metadata.service}"
}
```

---

## 🟩 LANGUAGE SPECIFICATION (SNRL)

### Language Structure

```snrl
schema "Name" {
  source: platform
  match: optional_condition
}

map {
  json.path -> target.field
}

rules {
  when condition {
    set field = value
    set field += value
  }
}

notify {
  title: "{field}"
  body:  "{field}"
  group: "{field}"
}
```

### Allowed Operators

```
==  !=  >  <  >=  <=
contains
in
and
or
not
```

**No loops. No functions. No JS.**

Safe declarative language.

---

## 📐 GLOBAL DESIGN SYSTEM (Material 3)

### 1️⃣ Color System

**Use Material You dynamic color if Android 12+**

**Fallback custom palette:**
- Primary: Deep Indigo
- Secondary: Slate Blue
- Tertiary: Teal Accent

**Semantic Color Mapping:**
| Type | Color |
|------|-------|
| Error | Red 600 |
| Warn | Amber 600 |
| Log | Blue 600 |
| Info | Teal 600 |
| Critical | Red 800 |

Priority overlay indicator = left border accent.

### 2️⃣ Typography

Material 3 scale:
- DisplayLarge
- HeadlineMedium
- TitleLarge
- TitleMedium
- BodyLarge
- BodyMedium
- LabelLarge
- LabelSmall

**Usage:**
- Feed title → TitleMedium
- Body → BodyMedium
- Metadata → LabelSmall
- Section headers → TitleLarge

### 3️⃣ Shape System

- Cards → 16dp rounded
- Dialog → 24dp rounded
- Chips → 50% rounded
- Buttons → 12dp rounded

### 4️⃣ Elevation

- Feed cards → 1dp tonal
- Pinned cards → 3dp tonal
- Dialogs → 6dp

Avoid heavy shadow stacking.

---

## 🧭 NAVIGATION ARCHITECTURE

**Use:**
- ModalNavigationDrawer (main navigation)
- Scaffold
- LargeTopAppBar

### NAVIGATION STRUCTURE

```
Navigation Drawer
│
├── Feed
├── Pinned
├── Todos
├── Sources
├── Schemas
├── Sync Monitor
├── Logs
├── Settings
└── Account
```

---

## 🏠 1️⃣ FEED SCREEN

**Purpose:** Main event timeline.

```
┌──────────────────────────────────┐
│ LargeTopAppBar                   │
│ SignalNest      [Search] [⋮]     │
├──────────────────────────────────┤
│ Filter Chips                     │
│ [All][Error][Warn][GitHub]       │
├──────────────────────────────────┤
│                                  │
│ ┌───────────────┐                │
│ │ ERROR         │ ← left accent  │
│ │ PR Failed     │                │
│ │ Repo: api     │                │
│ │ 2m ago        │                │
│ │ [Pin][Note][✓Todo]             │
│ └───────────────┘                │
│                                  │
│ LazyColumn (scrollable)          │
└──────────────────────────────────┘
```

**Feed Card Anatomy:**
- Left colored stripe (type indicator)
- Title
- Body preview (max 3 lines)
- Metadata row
- Action row

**Swipe gestures:**
- Swipe right → Pin
- Swipe left → Delete

---

## 📌 2️⃣ PINNED SCREEN

Pinned items only.

```
┌──────────────────────────┐
│ TopAppBar: Pinned        │
├──────────────────────────┤
│ Grid or list layout      │
│ Elevated cards (3dp)     │
└──────────────────────────┘
```

Pinned items sorted by `pinned_at desc`.

---

## ✅ 3️⃣ TODOS SCREEN

Integrated task manager.

```
┌──────────────────────────┐
│ TopAppBar: Todos         │
├──────────────────────────┤
│ [Open][Completed] Tabs   │
├──────────────────────────┤
│ ☐ Fix Payment API        │
│ Due: Tomorrow            │
│ Linked: PR #45           │
└──────────────────────────┘
```

FAB → Add manual Todo.

Each todo links to notification.

---

## 🌐 4️⃣ SOURCES SCREEN

```
┌──────────────────────────┐
│ TopAppBar: Sources       │
├──────────────────────────┤
│ + Add Source (FAB)       │
│                          │
│ GitHub (john-doe)        │
│ Status: Active           │
│ Last Sync: 2m ago        │
│ [Edit][Disable]          │
└──────────────────────────┘
```

**Add Source Modal:**
- GitHub
- Discord
- Custom Webhook

OAuth handled externally.

---

## 🧠 5️⃣ SCHEMAS SCREEN

List of DSL configs.

```
┌──────────────────────────┐
│ TopAppBar: Schemas       │
├──────────────────────────┤
│ + Create Schema (FAB)    │
│                          │
│ GitHubDefault            │
│ DiscordMessages          │
│ CustomWebhook            │
└──────────────────────────┘
```

---

## ✍ DSL EDITOR SCREEN

Two-pane responsive layout.

**Phone:**
- Editor → Preview via bottom sheet.

**Tablet:**
```
┌──────────────────────────────────┐
│ Editor         | Live Preview   │
│                |                 │
│ schema {...}   | ┌───────────┐   │
│ map {...}      | │ ERROR ... │   │
│ rules {...}    | └───────────┘   │
└──────────────────────────────────┘
```

**Components:**
- Monospace text editor
- Syntax highlighting
- Error underline
- Preview card
- Test JSON input modal

---

## 🔄 6️⃣ SYNC MONITOR

```
┌──────────────────────────┐
│ TopAppBar: Sync          │
├──────────────────────────┤
│ Current Version: 5432    │
│ Last Sync: 2m ago        │
│ Status: Healthy          │
│                          │
│ Timeline of updates      │
└──────────────────────────┘
```

---

## 📜 7️⃣ LOGS SCREEN

Server logs + DSL execution logs.

```
┌──────────────────────────┐
│ TopAppBar: Logs          │
├──────────────────────────┤
│ Filter Chips             │
│ [Errors][Warnings]       │
│                          │
│ ERROR – Mapping failed   │
│ INFO – Push processed    │
└──────────────────────────┘
```

---

## ⚙ 8️⃣ SETTINGS

```
┌──────────────────────────┐
│ TopAppBar: Settings      │
├──────────────────────────┤
│ Theme (System/Light/Dark)│
│ Notification Preferences │
│ Sync Interval            │
│ Export Schemas           │
│ Import Schemas           │
│ Clear Local DB           │
│ About                    │
└──────────────────────────┘
```

---

## 🔐 9️⃣ AUTH SCREENS

**Login / Register**

```
┌──────────────────────────┐
│ SignalNest               │
│                          │
│ Email                    │
│ Password                 │
│                          │
│ [Login]                  │
│ [Register]               │
└──────────────────────────┘
```

Minimal. Clean.

---

## 🧩 COMPONENT INVENTORY

**Use:**
- Scaffold
- ModalNavigationDrawer
- LargeTopAppBar
- MediumTopAppBar
- LazyColumn
- ElevatedCard
- AssistChip
- FilterChip
- ExtendedFAB
- FloatingActionButton
- ModalBottomSheet
- AlertDialog
- SnackbarHost
- PullRefreshIndicator
- TabRow

---

## 🧱 STATE MANAGEMENT MODEL

```
App Layers:

UI Layer (Compose)
   ↓
ViewModel
   ↓
Repository
   ↓
Room DB
   ↓
Sync Worker
   ↓
Server API
```

Offline-first.

---

## 📊 DATA FLOW

```
Server updates
    ↓
Sync Worker
    ↓
Room update
    ↓
StateFlow
    ↓
UI recomposes
```

---

## 📐 RESPONSIVE DESIGN

- **Phone:** Single column layout.
- **Tablet:** Two-pane (Feed + Detail, Editor + Preview)
- **Desktop future:** Three-pane (Explorer style).

---

## 🎯 UX PRINCIPLES

- Everything editable.
- DSL visible.
- Live preview always available.
- Clear status indicators.
- No hidden behavior.
- Minimal visual noise.

---

## 🧠 FINAL APP STRUCTURE

```
SignalNest Android App
│
├── Feed System
├── Pin System
├── Todo System
├── Notes System
├── DSL Editor
├── Source Manager
├── Sync Monitor
├── Logs
├── Auth
└── Settings
```

Material 3 compliant. Composable. Scalable. Professional.

---

## 🎨 THEME SYSTEM

### Theme Modes

1. **Light**
2. **Gray**
3. **Dark**
4. **OLED**

User selects manually in Settings. Persist locally. Applied globally.

---

### 🟡 1️⃣ LIGHT THEME

**Clean productivity mode.**

| Property | Color |
|----------|-------|
| Background | #FFFFFF |
| Surface | #F5F5F5 |
| Surface Variant | #EAEAEA |
| Primary | #3F51B5 |
| Secondary | #607D8B |
| Outline | #D0D0D0 |
| Primary Text | #111111 |
| Secondary Text | #555555 |
| Disabled | #9E9E9E |

**Notification Accent Mapping:**
| Type | Color |
|------|-------|
| Error | #D32F2F |
| Warn | #F57C00 |
| Log | #1976D2 |
| Info | #00897B |
| Critical | #B71C1C |

**Usage:**
- Feed background → white
- Cards → Surface
- TopAppBar → Primary
- Chips → SurfaceVariant

---

### 🩶 2️⃣ GRAY THEME

**Low glare. Neutral. Developer mode.**

| Property | Color |
|----------|-------|
| Background | #E6E6E6 |
| Surface | #DADADA |
| Surface Variant | #CFCFCF |
| Primary | #424242 |
| Secondary | #616161 |
| Outline | #B0B0B0 |
| Primary Text | #1A1A1A |
| Secondary Text | #4F4F4F |
| Disabled | #8C8C8C |

Accent colors remain same as Light for clarity.

**Purpose:**
- Long monitoring sessions
- Reduced brightness fatigue
- Neutral UI tone

---

### 🌑 3️⃣ DARK THEME

**Standard dark mode.**

| Property | Color |
|----------|-------|
| Background | #121212 |
| Surface | #1E1E1E |
| Surface Variant | #2A2A2A |
| Primary | #7986CB |
| Secondary | #90A4AE |
| Outline | #3A3A3A |
| Primary Text | #FFFFFF |
| Secondary Text | #B0B0B0 |
| Disabled | #666666 |

**Accent Mapping:**
| Type | Color |
|------|-------|
| Error | #EF5350 |
| Warn | #FFB74D |
| Log | #64B5F6 |
| Info | #4DB6AC |
| Critical | #FF1744 |

Cards use subtle tonal elevation.

---

### 🖤 4️⃣ OLED THEME

**True black. Maximum battery savings.**

| Property | Color |
|----------|-------|
| Background | #000000 |
| Surface | #000000 |
| Surface Variant | #0A0A0A |
| Primary | #BB86FC |
| Secondary | #03DAC6 |
| Outline | #1A1A1A |
| Primary Text | #FFFFFF |
| Secondary Text | #A0A0A0 |
| Disabled | #505050 |

**Cards:**
- No elevation shadow
- Thin outline border only

Accent colors slightly brighter than dark theme.

---

### 🧱 COMPONENT THEMING RULES

**TopAppBar:**
| Theme | Background | Text |
|-------|------------|------|
| Light | Primary | White |
| Gray | SurfaceVariant | - |
| Dark | Surface | - |
| OLED | Black | - |

**Cards (Feed Items):**
| Theme | Surface | Elevation |
|-------|---------|-----------|
| Light | Surface | 1dp tonal |
| Gray | SurfaceVariant | No heavy shadow |
| Dark | Surface | Tonal elevation only |
| OLED | Background black | 1px outline border |

**Chips:**
- Selected: Primary color
- Unselected: SurfaceVariant

**Buttons:**
- Primary Button: Filled with Primary
- Secondary Button: Outlined
- Danger: Error color

---

### 📐 SPACING SYSTEM (All Themes)

| Value | Name |
|-------|------|
| 4dp | micro |
| 8dp | small |
| 12dp | compact |
| 16dp | standard |
| 24dp | large |
| 32dp | section separation |

**Cards:**
- Padding: 16dp
- Vertical spacing between cards: 12dp

---

### 🌊 ANIMATION RULES

Keep minimal.

| Animation | Duration |
|-----------|----------|
| Fade in | 150ms |
| Card expand | 200ms |
| Bottom sheet | standard Material timing |
| Theme change | crossfade 200ms |

No flashy transitions.

---

### ⚙ SETTINGS UI FOR THEME

```
┌────────────────────────┐
│ Theme                  │
├────────────────────────┤
│ ○ Light                │
│ ○ Gray                 │
│ ○ Dark                 │
│ ○ OLED                 │
└────────────────────────┘
```

Live preview before confirm.

---

### 🧠 DESIGN PHILOSOPHY

| Theme | Purpose |
|-------|---------|
| Light | clean daily use |
| Gray | long focus sessions |
| Dark | balanced night use |
| OLED | extreme minimal + battery |

No extra theme. No dynamic wallpaper-based color. No complexity.

---

### FINAL STRUCTURE

```
ThemeEngine
│
├── LightPalette
├── GrayPalette
├── DarkPalette
└── OLEDPalette
```

Single source of truth. Applied via composition root.

---

## 🔵 SERVER DATA — MINIMUM SET

### 1️⃣ USERS

```sql
users
- id (uuid)
- email
- password_hash
- created_at
- last_login
- status (active|disabled)
```

**Purpose:** Authentication and ownership.

**Min Constraints:**
- Unique email
- JWT auth only

---

### 2️⃣ SOURCES

```sql
sources
- id
- user_id
- platform (github|discord|webhook)
- external_account_id
- access_token (encrypted)
- webhook_secret
- created_at
- status
```

**Min:**
- At least 1 source per user
- Status must be tracked

---

### 3️⃣ RAW EVENTS (CRITICAL)

Append-only.

```sql
raw_events
- id
- user_id
- source_id
- platform
- event_type
- external_event_id
- payload (jsonb)
- received_at
- processed (bool)
- retry_count
- error
```

**Min Guarantees:**
- Never deleted
- Idempotent (platform + external_event_id unique)

---

### 4️⃣ CANONICAL TREE

**Repositories:**
```sql
repositories
- id
- user_id
- source_id
- external_id
- name
- owner
- url
- visibility
- sync_version
- deleted_at
```

**Commits:**
```sql
commits
- id
- repository_id
- sha
- message
- author
- committed_at
- sync_version
```
Unique: (repository_id, sha)

**Issues:**
```sql
issues
- id
- repository_id
- number
- title
- body
- state
- author
- sync_version
```

**Pull Requests:**
```sql
pull_requests
- id
- repository_id
- number
- title
- body
- state
- sync_version
```

**Stars:**
```sql
stars
- id
- repository_id
- user_login
- starred_at
- sync_version
```

**Forks:**
```sql
forks
- id
- repository_id
- fork_id
- fork_url
- forked_at
- sync_version
```

---

### 5️⃣ NOTIFICATIONS

Derived entity.

```sql
notifications
- id
- user_id
- entity_type
- entity_id
- type (error|warn|log|info)
- title
- body
- priority
- auto_pin
- sync_version
- deleted_at
```

---

### 6️⃣ SYNC META

```sql
sync_meta
- user_id
- current_version bigint
- last_compaction
```

Version-based sync only.

---

### 7️⃣ DSL SCHEMAS

```sql
schemas
- id
- user_id
- name
- source
- raw_dsl_text
- compiled_ast
- created_at
- updated_at
- enabled
```

---

## 🔴 SERVER DATA — MAXIMUM SET

### 1️⃣ DELIVERY LOGS

```sql
delivery_logs
- id
- notification_id
- device_id
- status (pending|sent|failed|delivered)
- retry_count
- last_attempt
- error
```

### 2️⃣ DEVICES

```sql
devices
- id
- user_id
- fcm_token
- device_name
- last_seen
- active
```

### 3️⃣ FAILED EVENTS (Dead Letter Queue)

```sql
failed_events
- raw_event_id
- reason
- failed_at
```

### 4️⃣ METRICS

```sql
metrics
- event_type
- processing_time_ms
- success
- timestamp
```

### 5️⃣ RATE LIMIT TRACKING

```sql
rate_limits
- user_id
- window_start
- request_count
```

### 6️⃣ AUDIT LOGS

```sql
audit_logs
- user_id
- action
- target
- timestamp
```

### 7️⃣ SOFT DELETE SUPPORT

Every entity includes:
- `deleted_at` timestamp nullable

### 8️⃣ SCHEMA VERSIONING

```sql
schema_versions
- schema_id
- version
- dsl_text
- created_at
```

---

## 📊 DATA LIMITS (MIN/MAX RULES)

### Per User Limits

**Minimum enforced:**
| Resource | Limit |
|----------|-------|
| Max Sources | 10 |
| Max Schemas | 20 |
| Max Rules per Schema | 50 |
| Max Events per Minute | 200 |
| Max DSL size | 50KB |

**Maximum (scalable):**
- Unlimited with paid tier

---

## 🧠 SERVER BEHAVIOR RULES

### It MUST:
- Be stateless
- Use DB as source of truth
- Validate webhook signature
- Enforce idempotency
- Increment sync_version on change
- Reject malformed DSL
- Log every processing error

### It MUST NOT:
- Execute arbitrary code
- Allow direct SQL from DSL
- Allow recursion in DSL
- Store presentation layout logic
- Depend on push delivery for correctness

---

## 🔁 SERVER PROCESS FLOW

```
Webhook Received
   ↓
Validate Signature
   ↓
Insert raw_event
   ↓
Match Schema
   ↓
Execute DSL
   ↓
Normalize Entity
   ↓
Upsert Canonical Tree
   ↓
Increment sync_version
   ↓
Insert Notification
   ↓
(Optionally Send Push)
```

---

## 📁 FILE SYSTEM STRUCTURE

```
server/
│
├── core/
├── db/
│   ├── schema.sql
│   ├── migrations/
│
├── modules/
│   ├── auth/
│   ├── sources/
│   ├── raw_events/
│   ├── canonical/
│   ├── notifications/
│   ├── sync/
│   ├── dsl/
│   └── delivery/
│
├── adapters/
│   ├── github/
│   ├── discord/
│   └── webhook/
│
└── routes/
```

---

## 🧱 STORAGE STRATEGY

**Database:** Supabase PostgreSQL

**Indexes required:**
- user_id on all tables
- (repository_id, sha)
- (repository_id, number)
- (platform, external_event_id)
- sync_version index

---

## 🛡 SECURITY MINIMUM

- JWT auth
- HMAC webhook validation
- Rate limiting
- Encrypted tokens
- Input validation
- Row ownership check

---

## 🧠 FINAL SERVER MODEL

```
Server =
  Auth Layer
  +
  Ingestion Layer
  +
  Raw Event Store
  +
  DSL Engine
  +
  Canonical Tree Model
  +
  Versioned Sync Engine
  +
  Optional Delivery Engine
```

---

## 🧠 LANGUAGE SPECIFICATION (SNRL)

### Language Name

**SNRL — SignalNest Rule Language**

**Purpose:**
- Normalize incoming event data
- Apply logic rules
- Generate notifications
- Control grouping & priority
- Remain human-readable
- Be safe (no arbitrary execution)

**Declarative. Deterministic. Sandboxed.**

---

### 1️⃣ CORE PHILOSOPHY

**SNRL is:**
- Declarative (describe intent, not process)
- Non-Turing complete
- Side-effect limited
- Predictable execution
- Versionable
- Portable
- Compilable to internal AST

**It is NOT:**
- A scripting language
- JavaScript
- SQL
- Imperative code

---

### 2️⃣ FILE STRUCTURE

A schema file contains exactly 5 sections:

1. schema
2. map
3. transform (optional)
4. rules
5. notify

Order is fixed.

---

### 3️⃣ GRAMMAR (FORMAL STRUCTURE)

```ebnf
config         ::= schema_block map_block transform_block? rules_block notify_block

schema_block   ::= "schema" STRING "{" schema_body "}"
schema_body    ::= ("source:" IDENTIFIER)
                   ("match:" expression)?

map_block      ::= "map" "{" mapping+ "}"
mapping        ::= path "->" target

transform_block ::= "transform" "{" transform_stmt+ "}"
transform_stmt  ::= path "using" transform_type

rules_block    ::= "rules" "{" rule+ "}"
rule           ::= "when" expression "{" action+ "}"
action         ::= "set" field assign_operator value

notify_block   ::= "notify" "{" notify_stmt+ "}"
notify_stmt    ::= IDENTIFIER ":" template_string
```

---

### 4️⃣ DATA CONTEXT

When executed, the engine has:

| Scope | Purpose |
|-------|---------|
| event | raw JSON |
| entity | normalized data |
| notification | output object |
| metadata | computed values |

All fields live in these scopes.

---

### 5️⃣ SCHEMA BLOCK

Defines ownership and matching.

```snrl
schema "GitHubDefault" {
  source: github
  match: event.type == "push"
}
```

**Fields:**
- source (required)
- match (optional)

`match` filters raw event.

---

### 6️⃣ MAP BLOCK

Maps raw JSON paths to normalized fields.

**Syntax:**
```snrl
json.path -> target.field
```

**Allowed targets:**
- entity.*
- notification.*
- metadata.*

**Example:**
```snrl
map {
  repository.name      -> entity.repo
  sender.login         -> metadata.actor
  commits[0].message   -> notification.title
}
```

**Rules:**
- Left side must exist or evaluate to null.
- No dynamic creation outside allowed namespaces.

---

### 7️⃣ TRANSFORM BLOCK (OPTIONAL)

Apply built-in transformations.

**Supported transforms:**
- string
- number
- boolean
- date
- lowercase
- uppercase
- trim
- length

**Example:**
```snrl
transform {
  metadata.actor using lowercase
}
```

No custom functions. No user-defined transforms.

---

### 8️⃣ RULES BLOCK

Conditional logic.

**Syntax:**
```snrl
rules {
  when expression {
    set field = value
  }
}
```

**Expression Syntax:**

Supported operators:
```
==  !=  >  <  >=  <=
contains
in
and
or
not
```

**Example:**
```snrl
when metadata.actor == "admin" and notification.type == "error"
```

No nested blocks. No loops. No recursion.

**Actions:**
```snrl
set field = value
set field += value
```

---

### 9️⃣ NOTIFY BLOCK

Controls output formatting.

**Supported keys:**
- title:
- body:
- group:
- icon:
- sound:
- visibility:

**Templates use interpolation:**
```snrl
"{field.path}"
```

**Example:**
```snrl
notify {
  title: "{notification.title}"
  body:  "Repo: {entity.repo}"
  group: "{entity.repo}"
}
```

---

### 🔟 EXECUTION MODEL

**Processing order:**
1. Match schema
2. Apply map
3. Apply transform
4. Evaluate rules (top to bottom)
5. Build notification
6. Return normalized entity + notification

Rules override previous assignments.

Execution is single-pass.

---

### 1️⃣1️⃣ TYPE SYSTEM

**Primitive Types:**
- string
- number
- boolean
- date
- array
- null

**Implicit casting:**
- number to string allowed
- string to number forbidden unless transform used

---

### 1️⃣2️⃣ VARIABLE RESOLUTION

**Resolution priority:**
1. notification.*
2. entity.*
3. metadata.*
4. event.*

Dot notation only.

**Arrays:**
```snrl
commits[0].message
```

No wildcards. No deep globbing.

---

### 1️⃣3️⃣ SECURITY GUARANTEES

**Language cannot:**
- Execute system calls
- Run JS
- Access database directly
- Perform network calls
- Create loops
- Create infinite recursion

**Max constraints:**
| Constraint | Limit |
|------------|-------|
| Max rules per schema | 50 |
| Max mapping entries | 100 |
| Max DSL size | 50KB |
| Max execution time per event | 10ms |

---

### 1️⃣4️⃣ ERROR HANDLING

**Compile-time errors:**
- Invalid syntax
- Unknown target field
- Invalid operator
- Type mismatch

**Runtime errors:**
- Missing path → null
- Type mismatch in comparison → false

Runtime never crashes engine.

---

### 1️⃣5️⃣ VERSIONING

Each schema stored with:
- schema_id
- version
- dsl_text
- compiled_ast
- created_at

Older versions preserved. Rollback supported.

---

### 1️⃣6️⃣ PRECONFIGURED SCHEMA TEMPLATE STRUCTURE

GitHub, Discord, Webhook default schemas follow:
- schema
- map
- rules
- notify

Preconfigured schemas are locked but editable copies can be created.

---

### 1️⃣7️⃣ FUTURE EXTENSIONS (Controlled)

Allowed possible future additions:
- grouping {}
- aggregation {}
- schedule {}

But not required for v1.

---

### 1️⃣8️⃣ SAMPLE COMPLETE SCHEMA

```snrl
schema "BuildLogs" {
  source: webhook
}

map {
  level       -> notification.type
  message     -> notification.title
  service     -> entity.service
  user.id     -> metadata.user
}

transform {
  metadata.user using lowercase
}

rules {
  when notification.type == "error" {
    set notification.priority = critical
    set notification.icon = alert
  }

  when entity.service == "payments" and notification.type == "error" {
    set notification.priority = emergency
  }
}

notify {
  title: "{notification.title}"
  body:  "Service: {entity.service}\nUser: {metadata.user}"
  group: "{entity.service}"
}
```

Readable. Safe. Deterministic.

---

### 🧠 FINAL LANGUAGE MODEL

```
SNRL =
  Schema Definition
  +
  Data Mapping
  +
  Controlled Transformation
  +
  Deterministic Rules
  +
  Template Rendering
```

It is a domain rule engine. Not a programming language.

---

## 📋 ROADMAP

### PHASE 1 – Core Server ✅ COMPLETED
- [x] Setup Supabase schema
- [x] Create canonical tree database structure
- [x] Implement raw event store
- [x] Implement sync_version system
- [x] Implement idempotent upserts
- [x] Mock database mode for local dev
- [x] Environment validation with Zod
- [x] Structured logging
- [x] JWT Authentication with password validation
- [x] Notification endpoints (POST, GET, PATCH, DELETE)
- [x] Sync endpoints (POST, GET status)
- [x] Global mock storage for development

### PHASE 2 – DSL Engine ⏳ PENDING
- [ ] Build lexer
- [ ] Build parser
- [ ] Build AST validator
- [ ] Build execution engine
- [ ] Store compiled schemas

### PHASE 3 – GitHub + Discord Adapters ⏳ PENDING
- [ ] Webhook validation
- [ ] Adapter mapping
- [ ] Raw event insertion
- [ ] FCM push delivery

### PHASE 4 – App Mirror System 🔄 IN PROGRESS
- [x] Local in-memory mirror
- [x] Configurable server URL
- [x] Settings screen
- [x] No login required
- [x] Notification permission handling
- [x] System notification display
- [x] Pull-to-sync functionality
- [ ] **Unread/Read notification system** ⭐ NEW
- [ ] Version-based sync
- [ ] Conflict rules
- [x] Pin system (repository methods ready)
- [x] Notes & Todos linking (repository methods ready)
- [ ] 4-theme system (Light, Gray, Dark, OLED)

### PHASE 5 – Intelligence Layer ⏳ PENDING
- [ ] Rule testing UI
- [ ] Live preview
- [ ] Schema export/import
- [ ] Log visualization

---

## ✅ COMPLETED (2026-02-22)

### Server v3.0.0
- Complete rebuild from scratch
- TypeScript + Hono framework
- Mock database mode for local development
- Full canonical tree schema for Supabase
- JWT authentication with password strength validation
- Structured logging
- Environment validation

**Tested Endpoints:**
- `GET /health` ✅
- `POST /auth/register` ✅ (with password validation)
- `POST /auth/login` ✅ (JWT token generation)
- `GET /auth/me` ✅

**To Run Locally:**
```bash
cd mr_notifier-server
npm install
npm run dev
```

**To Test:**
```bash
curl http://localhost:3000/health
curl -X POST http://localhost:3000/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"test@example.com","password":"SecurePass123!"}'
curl -X POST http://localhost:3000/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"test@example.com","password":"SecurePass123!"}'
```

---

## FINAL STRUCTURE

```
SignalNest =
  Server (24/7 Execution + Storage)
  +
  App (Control + Mirror + Productivity Layer)
  +
  SNRL (Human-readable event programming language)
```

---

**Last Updated:** 2026-02-22  
**Document:** `/data/data/com.termux/files/home/github/mr_notifier/docs/roadmap-test.md`
