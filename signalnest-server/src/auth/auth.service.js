import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import { config } from '../config.js';
import { createUser, findUserByUsername, findUserById } from './users.js';

export async function registerUser(username, email, password) {
  // Validate input
  if (!username || !email || !password) {
    throw new Error('Username, email, and password are required');
  }

  if (password.length < 6) {
    throw new Error('Password must be at least 6 characters');
  }

  // Check if user already exists
  const existingUser = findUserByUsername(username);
  if (existingUser) {
    throw new Error('Username already exists');
  }

  // Hash password
  const hashedPassword = await bcrypt.hash(password, 10);

  // Create user
  const user = createUser({
    username,
    email,
    password: hashedPassword,
  });

  // Remove password from response
  const { password: _, ...userWithoutPassword } = user;
  return userWithoutPassword;
}

export async function loginUser(username, password) {
  // Find user
  const user = findUserByUsername(username);
  if (!user) {
    throw new Error('Invalid username or password');
  }

  // Verify password
  const isValid = await bcrypt.compare(password, user.password);
  if (!isValid) {
    throw new Error('Invalid username or password');
  }

  // Generate JWT token
  const token = jwt.sign(
    { userId: user.id, username: user.username },
    config.jwtSecret,
    { expiresIn: config.jwtExpiresIn }
  );

  // Remove password from response
  const { password: _, ...userWithoutPassword } = user;
  return {
    token,
    user: userWithoutPassword,
  };
}

export function verifyToken(token) {
  try {
    return jwt.verify(token, config.jwtSecret);
  } catch (error) {
    return null;
  }
}

export function getCurrentUser(userId) {
  const user = findUserById(userId);
  if (user) {
    const { password: _, ...userWithoutPassword } = user;
    return userWithoutPassword;
  }
  return null;
}
