// In-memory user storage (replace with database in production)
export const users = new Map();

export function createUser(userData) {
  const user = {
    id: crypto.randomUUID(),
    ...userData,
    createdAt: new Date().toISOString(),
  };
  users.set(user.id, user);
  return user;
}

export function findUserByUsername(username) {
  for (const user of users.values()) {
    if (user.username === username) {
      return user;
    }
  }
  return null;
}

export function findUserById(id) {
  return users.get(id);
}

export function updateUser(id, updates) {
  const user = users.get(id);
  if (user) {
    const updated = { ...user, ...updates };
    users.set(id, updated);
    return updated;
  }
  return null;
}
