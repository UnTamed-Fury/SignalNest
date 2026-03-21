import { Router } from 'express';
import { registerUser, loginUser, getCurrentUser, verifyToken } from './auth.service.js';
import { createLogger } from '../utils/logger.js';

const router = Router();
const logger = createLogger('auth');

// Register
router.post('/register', async (req, res) => {
  try {
    const { username, email, password } = req.body;
    const user = await registerUser(username, email, password);
    logger.info(`User registered: ${username}`);
    res.status(201).json({ user });
  } catch (error) {
    logger.error(`Registration failed: ${error.message}`);
    res.status(400).json({ error: error.message });
  }
});

// Login
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    const result = await loginUser(username, password);
    logger.info(`User logged in: ${username}`);
    res.json(result);
  } catch (error) {
    logger.error(`Login failed: ${error.message}`);
    res.status(401).json({ error: error.message });
  }
});

// Get current user
router.get('/me', (req, res) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'No token provided' });
    }

    const token = authHeader.substring(7);
    const decoded = verifyToken(token);
    
    if (!decoded) {
      return res.status(401).json({ error: 'Invalid token' });
    }

    const user = getCurrentUser(decoded.userId);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json(user);
  } catch (error) {
    logger.error(`Get user failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

export { router as authRoutes };
