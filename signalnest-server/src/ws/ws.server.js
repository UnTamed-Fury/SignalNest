import WebSocket, { WebSocketServer } from 'ws';
import { createLogger } from '../utils/logger.js';
import { verifyToken } from '../auth/auth.service.js';
import { getUserNotifications, getUnreadCount } from '../events/notifications.js';

const logger = createLogger('ws');

// WebSocket server instance
export let wss = null;

// Connected clients per user
export const connectedClients = new Map();

// Ping interval (30 seconds)
const PING_INTERVAL = 30000;

export function initializeWebSocket(server) {
  wss = new WebSocketServer({
    server,
    path: '/ws',
  });

  wss.on('connection', (ws, req) => {
    logger.info('New WebSocket connection');
    
    // Extract token from URL query params
    const url = new URL(req.url, `http://${req.headers.host}`);
    const token = url.searchParams.get('token');
    
    if (!token) {
      ws.close(4001, 'No token provided');
      return;
    }

    // Verify token
    const decoded = verifyToken(token);
    if (!decoded) {
      ws.close(4002, 'Invalid token');
      return;
    }

    const userId = decoded.userId;
    const username = decoded.username;

    // Store client connection
    if (!connectedClients.has(userId)) {
      connectedClients.set(userId, []);
    }
    connectedClients.get(userId).push(ws);

    logger.info(`User ${username} connected via WebSocket`);

    // Send welcome message
    ws.send(JSON.stringify({
      type: 'connected',
      userId,
      username,
      timestamp: Date.now(),
    }));

    // Setup ping/pong keepalive
    const pingInterval = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.ping();
      }
    }, PING_INTERVAL);

    // Handle messages
    ws.on('message', (message) => {
      try {
        const data = JSON.parse(message.toString());
        logger.debug(`Message from ${username}:`, data);
        
        // Handle different message types
        switch (data.type) {
          case 'ping':
            ws.send(JSON.stringify({ type: 'pong', timestamp: Date.now() }));
            break;
          case 'get_notifications':
            sendNotifications(ws, userId);
            break;
          case 'get_unread_count':
            sendUnreadCount(ws, userId);
            break;
          default:
            logger.warn(`Unknown message type: ${data.type}`);
        }
      } catch (error) {
        logger.error(`Error processing message: ${error.message}`);
      }
    });

    // Handle disconnection
    ws.on('close', () => {
      clearInterval(pingInterval);
      const clients = connectedClients.get(userId) || [];
      const index = clients.indexOf(ws);
      if (index !== -1) {
        clients.splice(index, 1);
      }
      logger.info(`User ${username} disconnected`);
    });

    // Handle errors
    ws.on('error', (error) => {
      logger.error(`WebSocket error for ${username}:`, error);
    });
  });

  logger.info('WebSocket server initialized');
}

function sendNotifications(ws, userId) {
  const userNotifications = getUserNotifications(userId);
  ws.send(JSON.stringify({
    type: 'notifications',
    notifications: userNotifications,
    timestamp: Date.now(),
  }));
}

function sendUnreadCount(ws, userId) {
  const count = getUnreadCount(userId);
  ws.send(JSON.stringify({
    type: 'unread_count',
    count,
    timestamp: Date.now(),
  }));
}

// Broadcast notification to all connected clients for a user
export function broadcastToUser(userId, data) {
  const clients = connectedClients.get(userId) || [];
  const message = JSON.stringify(data);
  
  clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

// Broadcast to all connected clients
export function broadcastToAll(data) {
  const message = JSON.stringify(data);
  
  wss?.clients.forEach(client => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

export function closeWebSocket() {
  wss?.close();
  logger.info('WebSocket server closed');
}
