import jwt from 'jsonwebtoken';
import { config } from '../config.js';

export function signToken() {
  return jwt.sign({ app: 'signalnest' }, config.jwtSecret, { expiresIn: '365d' });
}

export function verifyToken(token) {
  try { return jwt.verify(token, config.jwtSecret); }
  catch { return null; }
}

export function authMiddleware(req, res, next) {
  const h = req.headers.authorization;
  if (!h?.startsWith('Bearer ')) return res.status(401).json({ error: 'No token' });
  if (!verifyToken(h.slice(7))) return res.status(401).json({ error: 'Invalid token' });
  next();
}
