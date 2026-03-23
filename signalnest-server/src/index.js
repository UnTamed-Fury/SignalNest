import 'dotenv/config';
import express from 'express';
import http from 'http';
import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';
import { config } from './config.js';
import { authRouter }    from './auth/routes.js';
import { eventsRouter }  from './events/routes.js';
import { webhookRouter } from './webhook/webhook.js';
import { snrlRoutes }    from './snrl/routes.js';
import { initWs, closeWss } from './ws/ws.js';
import { createLogger } from './utils/logger.js';

const log = createLogger('server');
const app = express();
const server = http.createServer(app);

initWs(server);

app.use(helmet());
app.use(cors({ origin: '*' }));
app.use(rateLimit({ windowMs: 60_000, max: 120, message: { error: 'Rate limit exceeded' } }));
app.use(express.json({ limit: '2mb' }));

// ── Routes ────────────────────────────────────────────────────────────────────
app.use(authRouter);
app.use(eventsRouter);
app.use(webhookRouter);
app.use(snrlRoutes);

app.get('/health', (_, res) => res.json({ ok: true, uptime: process.uptime() }));
app.get('/',       (_, res) => res.json({ app: 'SignalNest', version: '2.0.0' }));

// 404
app.use((req, res) => res.status(404).json({ error: 'Not found', path: req.path }));

// Error handler
app.use((err, req, res, _next) => {
  log.error(err.message);
  res.status(500).json({ error: config.nodeEnv === 'production' ? 'Internal error' : err.message });
});

// ── Start ─────────────────────────────────────────────────────────────────────
server.listen(config.port, config.host, () => {
  log.info('');
  log.info('🚀 SignalNest Server v2.0.0');
  log.info(`🌐 http://${config.host}:${config.port}`);
  log.info('');
  log.info('Endpoints:');
  log.info('  POST /app/connect         — App auth (PASSWORD → token)');
  log.info('  GET  /app/events          — Pull buffered events');
  log.info('  WS   /ws?token=...        — Live push to app');
  log.info('  POST /webhook             — Inbound webhooks (public)');
  log.info('  GET  /app/rules           — List SNRL rules');
  log.info('  POST /app/rules           — Create SNRL rule');
  log.info('  PATCH /app/rules/:id      — Update SNRL rule');
  log.info('  DELETE /app/rules/:id     — Delete SNRL rule');
  log.info('  POST /app/rules/validate  — Validate rule syntax');
  log.info('  GET  /health              — Health check');
  log.info('');
  if (!config.password) log.warn('⚠️  PASSWORD env var not set!');
});

const graceful = () => { server.close(() => { closeWss(); process.exit(0); }); };
process.on('SIGTERM', graceful);
process.on('SIGINT',  graceful);
