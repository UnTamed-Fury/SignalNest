// In-memory notification storage (per-user ring buffer)
export const notifications = new Map();

const MAX_NOTIFICATIONS_PER_USER = 100;

export function createNotification(userId, notificationData) {
  const notification = {
    id: crypto.randomUUID(),
    userId,
    ...notificationData,
    isRead: false,
    pinned: false,
    createdAt: Date.now(),
    updatedAt: Date.now(),
  };

  // Get or create user's notification array
  if (!notifications.has(userId)) {
    notifications.set(userId, []);
  }

  const userNotifications = notifications.get(userId);
  userNotifications.unshift(notification);

  // Enforce max limit (ring buffer)
  if (userNotifications.length > MAX_NOTIFICATIONS_PER_USER) {
    userNotifications.pop();
  }

  return notification;
}

export function getUserNotifications(userId, options = {}) {
  const userNotifications = notifications.get(userId) || [];
  const { limit = 50, offset = 0, unreadOnly = false } = options;

  let filtered = userNotifications;
  if (unreadOnly) {
    filtered = userNotifications.filter(n => !n.isRead);
  }

  return filtered.slice(offset, offset + limit);
}

export function getNotificationById(userId, notificationId) {
  const userNotifications = notifications.get(userId) || [];
  return userNotifications.find(n => n.id === notificationId);
}

export function markNotificationAsRead(userId, notificationId) {
  const userNotifications = notifications.get(userId) || [];
  const notification = userNotifications.find(n => n.id === notificationId);
  
  if (notification) {
    notification.isRead = true;
    notification.updatedAt = Date.now();
    return notification;
  }
  return null;
}

export function markNotificationAsUnread(userId, notificationId) {
  const userNotifications = notifications.get(userId) || [];
  const notification = userNotifications.find(n => n.id === notificationId);
  
  if (notification) {
    notification.isRead = false;
    notification.updatedAt = Date.now();
    return notification;
  }
  return null;
}

export function markAllNotificationsAsRead(userId) {
  const userNotifications = notifications.get(userId) || [];
  let count = 0;
  
  userNotifications.forEach(notification => {
    if (!notification.isRead) {
      notification.isRead = true;
      notification.updatedAt = Date.now();
      count++;
    }
  });
  
  return count;
}

export function getUnreadCount(userId) {
  const userNotifications = notifications.get(userId) || [];
  return userNotifications.filter(n => !n.isRead).length;
}

export function deleteNotification(userId, notificationId) {
  const userNotifications = notifications.get(userId) || [];
  const index = userNotifications.findIndex(n => n.id === notificationId);
  
  if (index !== -1) {
    userNotifications.splice(index, 1);
    return true;
  }
  return false;
}

export function togglePin(userId, notificationId) {
  const userNotifications = notifications.get(userId) || [];
  const notification = userNotifications.find(n => n.id === notificationId);
  
  if (notification) {
    notification.pinned = !notification.pinned;
    notification.updatedAt = Date.now();
    return notification;
  }
  return null;
}

export function clearAllNotifications(userId) {
  notifications.set(userId, []);
}
