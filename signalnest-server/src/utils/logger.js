import { config } from '../config.js';
const ico = { error:'🔴', warn:'🟡', info:'🔵', debug:'🟢' };
export const createLogger = (mod) => ({
  error: (m, ...a) => console.error(`${ico.error} [${mod}] ${m}`, ...a),
  warn:  (m, ...a) => console.warn (`${ico.warn}  [${mod}] ${m}`, ...a),
  info:  (m, ...a) => console.info (`${ico.info}  [${mod}] ${m}`, ...a),
  debug: (m, ...a) => { if (config.nodeEnv !== 'production') console.debug(`${ico.debug} [${mod}] ${m}`, ...a); },
});
