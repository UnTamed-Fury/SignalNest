# Contributing to SignalNest

Thanks for contributing! Here's how to help.

## Quick Start

```bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/signalnest-monorepo.git
cd signalnest-monorepo

# Install dependencies
pnpm install

# Create branch
git checkout -b feature/your-feature

# Make changes, then test
./build.sh build

# Commit (conventional commits)
git commit -m "feat: add new feature"

# Push and create PR
git push origin feature/your-feature
```

---

## Code of Conduct

Be respectful and inclusive. See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

---

## What We Need

### High Priority

- [ ] DSL engine implementation
- [ ] GitHub/Discord adapters
- [ ] iOS app (SwiftUI)
- [ ] Web app (React)
- [ ] Unit tests

### Always Welcome

- Bug fixes
- Documentation improvements
- Feature suggestions
- Bug reports with reproduction steps

---

## Development Setup

### Prerequisites

- Node.js >= 18
- pnpm >= 8
- Java 17+
- Android SDK (for app)

### Install

```bash
pnpm install
```

### Build

```bash
# Everything
./build.sh build

# Server only
./build.sh build:server

# App only
./build.sh build:app
```

### Test

```bash
# Server
cd signalnest-server
pnpm test

# App
cd signalnest-app
./gradlew test
```

---

## Coding Standards

### Kotlin

- Follow [Kotlin conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names
- Keep functions small
- Add KDoc for public APIs

```kotlin
// Good
fun getUserById(userId: String): User? {
    return database.findUser(userId)
}

// Bad
fun f(id: String): Any? = db.find(id)
```

### JavaScript

- ES6+ features
- `const` over `let`
- async/await for async
- JSDoc for public functions

```javascript
// Good
async function createUser(userData) {
    const user = await db.insert(userData);
    return user;
}
```

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `style` | Formatting |
| `refactor` | Code restructuring |
| `test` | Tests |
| `chore` | Build/config |

### Examples

```
feat(app): add 4-theme system

Implemented Light, Gray, Dark, OLED themes.
Closes #123
```

```
fix(server): resolve WebSocket reconnection

Fixed race condition in WebSocket client.
```

---

## Pull Request Checklist

- [ ] Code follows style guidelines
- [ ] Tests pass (if applicable)
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
- [ ] Commit messages are clear

---

## Bug Reports

### Template

```markdown
**Describe the bug**
Clear description of the bug.

**To Reproduce**
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected.

**Environment:**
- OS/Device: [e.g., Pixel 7, Android 14]
- App Version: [e.g., 1.0.0]
- Server Version: [e.g., 1.0.0]

**Screenshots**
If applicable.

**Additional context**
Any other info.
```

---

## Feature Requests

### Template

```markdown
**Is your feature request related to a problem?**
Description of the problem.

**Describe the solution**
What you want to happen.

**Alternatives considered**
Other solutions you've considered.

**Use case**
Who will use this?
```

---

## Questions?

- Open an issue with `question` label
- Use [GitHub Discussions](https://github.com/yourusername/signalnest-monorepo/discussions)

---

## License

By contributing, you agree to license under ISC License.
