import 'dotenv/config';

if (!process.env.PASSWORD) {
  console.warn('⚠️  WARNING: PASSWORD env var not set. Server will reject all connections.');
}

export const config = {
  port:     parseInt(process.env.PORT  || '3000'),
  host:     process.env.HOST           || '0.0.0.0',
  password: process.env.PASSWORD       || '',
  jwtSecret:process.env.JWT_SECRET     || ('sn_' + Math.random().toString(36)),
  nodeEnv:  process.env.NODE_ENV       || 'development',
  maxEvents:parseInt(process.env.MAX_EVENTS || '500'),
};
