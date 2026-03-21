import { Router } from 'express';
import { 
  getUserNotifications, 
  getNotificationById,
  markNotificationAsRead,
  markNotificationAsUnread,
  markAllNotificationsAsRead,
  getUnreadCount,
  deleteNotification,
  createNotification,
  togglePin 
} from '../events/notifications.js';
import { broadcastToUser } from '../ws/ws.server.js';
import { verifyToken } from '../auth/auth.service.js';
import { createLogger } from '../utils/logger.js';

const router = Router();
const logger = createLogger('notifications');

// Middleware to verify token
function authMiddleware(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'No token provided' });
  }

  const token = authHeader.substring(7);
  const decoded = verifyToken(token);
  
  if (!decoded) {
    return res.status(401).json({ error: 'Invalid token' });
  }

  req.userId = decoded.userId;
  req.username = decoded.username;
  next();
}

// Get all notifications
router.get('/', authMiddleware, (req, res) => {
  try {
    const { limit = 50, offset = 0, unreadOnly = false } = req.query;
    const notifications = getUserNotifications(req.userId, {
      limit: parseInt(limit),
      offset: parseInt(offset),
      unreadOnly: unreadOnly === 'true',
    });
    
    res.json({
      notifications,
      total: notifications.length,
    });
  } catch (error) {
    logger.error(`Get notifications failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Get unread count
router.get('/unread/count', authMiddleware, (req, res) => {
  try {
    const count = getUnreadCount(req.userId);
    res.json({ count });
  } catch (error) {
    logger.error(`Get unread count failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Mark as read
router.patch('/:id/read', authMiddleware, (req, res) => {
  try {
    const notification = markNotificationAsRead(req.userId, req.params.id);
    if (!notification) {
      return res.status(404).json({ error: 'Notification not found' });
    }

    // Broadcast update
    broadcastToUser(req.userId, {
      type: 'notification_updated',
      notification,
    });

    res.json({ notification });
  } catch (error) {
    logger.error(`Mark as read failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Mark as unread
router.patch('/:id/unread', authMiddleware, (req, res) => {
  try {
    const notification = markNotificationAsUnread(req.userId, req.params.id);
    if (!notification) {
      return res.status(404).json({ error: 'Notification not found' });
    }

    // Broadcast update
    broadcastToUser(req.userId, {
      type: 'notification_updated',
      notification,
    });

    res.json({ notification });
  } catch (error) {
    logger.error(`Mark as unread failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Mark all as read
router.patch('/mark-all-read', authMiddleware, (req, res) => {
  try {
    const count = markAllNotificationsAsRead(req.userId);
    
    // Broadcast update
    broadcastToUser(req.userId, {
      type: 'all_read',
      count,
    });

    res.json({ markedCount: count });
  } catch (error) {
    logger.error(`Mark all as read failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Delete notification
router.delete('/:id', authMiddleware, (req, res) => {
  try {
    const deleted = deleteNotification(req.userId, req.params.id);
    if (!deleted) {
      return res.status(404).json({ error: 'Notification not found' });
    }

    // Broadcast update
    broadcastToUser(req.userId, {
      type: 'notification_deleted',
      notificationId: req.params.id,
    });

    res.status(204).send();
  } catch (error) {
    logger.error(`Delete notification failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Toggle pin
router.patch('/:id/pin', authMiddleware, (req, res) => {
  try {
    const notification = togglePin(req.userId, req.params.id);
    if (!notification) {
      return res.status(404).json({ error: 'Notification not found' });
    }

    // Broadcast update
    broadcastToUser(req.userId, {
      type: 'notification_updated',
      notification,
    });

    res.json({ notification });
  } catch (error) {
    logger.error(`Toggle pin failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

// Create notification (for testing)
router.post('/', authMiddleware, (req, res) => {
  try {
    const { title, body, sourceType = 'api', type = 'info', metadata = {} } = req.body;

    if (!title || !body) {
      return res.status(400).json({ error: 'Title and body are required' });
    }

    const notification = createNotification(req.userId, {
      title,
      body,
      sourceType,
      type,
      metadata,
    });

    // Broadcast to connected clients
    broadcastToUser(req.userId, {
      type: 'new_notification',
      notification,
    });

    logger.info(`Notification created for user ${req.userId}`);
    res.status(201).json({ notification });
  } catch (error) {
    logger.error(`Create notification failed: ${error.message}`);
    res.status(500).json({ error: error.message });
  }
});

export { router as notificationRoutes };
