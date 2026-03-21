# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | ✅ |
| < 1.0   | ❌ |

---

## Reporting a Vulnerability

**DO NOT create public GitHub issues for security vulnerabilities.**

### Report Via

1. **GitHub Security Advisories** (Preferred)
   - Go to Security tab → Report a vulnerability

2. **Email** (if configured)
   - security@signalnest.dev

### Include

- Type of vulnerability
- Affected files (full paths)
- Reproduction steps
- Proof-of-concept (if possible)
- Potential impact

### Response Time

| Severity | Response |
|----------|----------|
| Critical | 24-48 hours |
| High | 48-72 hours |
| Medium | 1 week |
| Low | 2 weeks |

---

## Process

1. **Report** → Acknowledgment within response time
2. **Assessment** → Evaluate vulnerability
3. **Fix** → Develop patch
4. **Test** → Thorough testing
5. **Release** → Publish security update
6. **Disclosure** → Coordinated (if requested)

---

## Security Best Practices

### For Users

#### Server

1. **Change JWT_SECRET**
   ```bash
   JWT_SECRET=<random-32+-chars>
   ```

2. **Use HTTPS**
   - Never expose HTTP to internet
   - Use reverse proxy (nginx, caddy)

3. **Update dependencies**
   ```bash
   pnpm audit
   pnpm update
   ```

4. **Firewall**
   - Expose only necessary ports
   - Use fail2ban

#### App

1. **Keep updated** - Install updates promptly
2. **Strong passwords** - 8+ chars, mixed
3. **Use HTTPS** - For remote servers
4. **Review permissions** - Grant only necessary

### For Developers

#### Code Security

- [ ] No hardcoded secrets
- [ ] Input validation
- [ ] Authentication required
- [ ] Authorization checked
- [ ] No sensitive data in logs
- [ ] Error messages don't leak info

#### Dependencies

```bash
# Check regularly
pnpm audit
npm audit

# Update
pnpm update
```

---

## Current Security Measures

### Server

- ✅ JWT authentication
- ✅ Password hashing (bcrypt)
- ✅ Rate limiting (100 req/15min)
- ✅ Helmet.js headers
- ✅ CORS configuration
- ✅ Input validation

### App

- ✅ Network security config
- ✅ Secure preferences (DataStore)
- ✅ No sensitive data in logs
- ✅ Permission checks

---

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Node.js Security](https://nodejs.org/en/docs/guides/security/)
- [Android Security](https://developer.android.com/topic/security)

---

**Last Updated:** 2026-03-21
