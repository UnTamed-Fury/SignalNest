import { Router } from 'express';
import { addEvent } from '../events/store.js';
import { broadcast } from '../ws/ws.js';
import { createLogger } from '../utils/logger.js';
import { parseGitHub } from '../github/github.js';

const router = Router();
const log    = createLogger('webhook');

/**
 * POST /webhook
 * Public endpoint — no auth.  Any JSON body is accepted.
 * Field mapping (checked in order):
 *   title:   title | summary | subject | alertname | ref | action
 *   body:    message | body | description | text | msg | commits[0].message
 *   source:  source | service | repository.full_name | sender.login
 *   group:   group | category | repository.name
 *   silent:  silent === true  →  category = "silent"
 */
router.post('/webhook', (req, res) => {
  try {
    const payload = req.body;
    const isGH    = !!req.headers['x-github-event'];
    const event   = isGH ? parseGitHub(req) : parseGeneric(payload);

    const saved = addEvent(event);
    broadcast({ type: 'event', data: saved });
    log.info(`Event: [${event.source}] ${event.title}`);
    res.status(202).json({ ok: true, id: saved.id });
  } catch (e) {
    log.error(e.message);
    res.status(500).json({ error: e.message });
  }
});

function str(obj, ...keys) {
  for (const k of keys) {
    const v = obj?.[k];
    if (typeof v === 'string' && v.trim()) return v.trim();
  }
  return null;
}

function parseGeneric(p) {
  const silent = p?.silent === true || p?.category === 'silent';
  return {
    title:    str(p, 'title','summary','subject','alertname','action') ?? 'Webhook',
    body:     str(p, 'message','body','description','text','msg')      ?? JSON.stringify(p).slice(0, 400),
    source:   str(p, 'source','service','host','from')                 ?? 'custom',
    group:    str(p, 'group','category','namespace')                   ?? 'default',
    category: silent ? 'silent' : 'normal',
    rawPayload: JSON.stringify(p),
    channel:  'remote',
  };
}

export { router as webhookRouter };
