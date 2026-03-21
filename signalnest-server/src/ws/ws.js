import { WebSocketServer, WebSocket } from 'ws';
import { verifyToken } from '../auth/auth.js';
import { getEvents } from '../events/store.js';
import { createLogger } from '../utils/logger.js';

const log = createLogger('ws');
let wss   = null;

export function initWs(server) {
  wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', (ws, req) => {
    const token = new URL(req.url, 'http://x').searchParams.get('token');
    if (!token || !verifyToken(token)) { ws.close(4001, 'Unauthorized'); return; }

    log.info('App connected');

    // Send missed events immediately on connect
    const missed = getEvents(200);
    if (missed.length) ws.send(JSON.stringify({ type: 'events', data: missed }));

    const ping = setInterval(() => { if (ws.readyState === WebSocket.OPEN) ws.ping(); }, 30_000);

    ws.on('message', (raw) => {
      try {
        const msg = JSON.parse(raw.toString());
        if (msg.type === 'ping') ws.send(JSON.stringify({ type: 'pong', ts: Date.now() }));
      } catch { /* ignore */ }
    });

    ws.on('close', () => { clearInterval(ping); log.info('App disconnected'); });
    ws.on('error', (e) => log.error(e.message));
  });

  log.info('WebSocket ready at /ws');
}

export function broadcast(data) {
  if (!wss) return;
  const msg = JSON.stringify(data);
  wss.clients.forEach(c => { if (c.readyState === WebSocket.OPEN) c.send(msg); });
}

export function closeWss() { wss?.close(); }
