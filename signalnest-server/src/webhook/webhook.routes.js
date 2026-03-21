import { Router } from 'express';
import { createNotification } from '../events/notifications.js';
import { broadcastToUser } from '../ws/ws.server.js';
import { verifyToken } from '../auth/auth.service.js';
import { createLogger } from '../utils/logger.js';

const router = Router();
const logger = createLogger('webhook');

// Webhook endpoint for external services
router.post('/api/webhook', (req, res) => {
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

    const { title, body, sourceType = 'webhook', type = 'info', metadata = {} } = req.body;

    if (!title || !body) {
      return res.status(400).json({ error: 'Title and body are required' });
    }

    // Create notification
    const notification = createNotification(decoded.userId, {
      title,
      body,
      sourceType,
      type,
      metadata,
    });

    // Broadcast to connected clients
    broadcastToUser(decoded.userId, {
      type: 'new_notification',
      notification,
    });

    logger.info(`Webhook notification created for user ${decoded.userId}`);
    res.status(201).json({ notification });
  } catch (error) {
    logger.error(`Webhook failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

export { router as webhookRoutes };
