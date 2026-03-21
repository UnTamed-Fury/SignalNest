import { Router } from 'express';
import { signToken } from './auth.js';
import { config } from '../config.js';
import { createLogger } from '../utils/logger.js';

const router = Router();
const log    = createLogger('auth');

/** POST /app/connect — exchange PASSWORD for WS token */
router.post('/app/connect', (req, res) => {
  const { password } = req.body ?? {};
  if (!password || password !== config.password) {
    log.warn('Bad password attempt');
    return res.status(401).json({ error: 'Invalid password' });
  }
  const token = signToken();
  log.info('App authenticated');
  res.json({ token, ok: true });
});

export { router as authRouter };
