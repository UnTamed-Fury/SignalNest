/**
 * SNRL Rule Store
 * In-memory storage for rules with order-preserving semantics.
 * Rules are applied in ascending `order` value.
 */
import { validate } from './validator.js';

// Map<id, rule>
const rules = new Map();

/**
 * @typedef {object} Rule
 * @prop {string}  id
 * @prop {string}  name
 * @prop {string}  text      — raw SNRL rule text
 * @prop {object}  ast       — parsed AST (cached)
 * @prop {boolean} enabled
 * @prop {number}  order     — execution order (lower = first)
 * @prop {string}  createdAt
 * @prop {string}  updatedAt
 */

export function createRule(name, text) {
  const { ok, ast, error } = validate(text);
  if (!ok) throw new Error(error);

  const rule = {
    id:        crypto.randomUUID(),
    name:      name || 'Unnamed rule',
    text,
    ast,
    enabled:   true,
    order:     rules.size,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
  rules.set(rule.id, rule);
  return sanitize(rule);
}

export function updateRule(id, { name, text, enabled, order }) {
  const rule = rules.get(id);
  if (!rule) throw new Error(`Rule ${id} not found`);

  if (text && text !== rule.text) {
    const { ok, ast, error } = validate(text);
    if (!ok) throw new Error(error);
    rule.text = text;
    rule.ast  = ast;
  }
  if (name     !== undefined) rule.name    = name;
  if (enabled  !== undefined) rule.enabled = enabled;
  if (order    !== undefined) rule.order   = order;
  rule.updatedAt = new Date().toISOString();
  return sanitize(rule);
}

export function deleteRule(id) {
  if (!rules.has(id)) throw new Error(`Rule ${id} not found`);
  rules.delete(id);
}

export function getRules() {
  return [...rules.values()]
    .sort((a, b) => a.order - b.order)
    .map(sanitize);
}

/** Returns raw rules with ASTs for the engine (enabled only, sorted). */
export function getActiveRuleAsts() {
  return [...rules.values()]
    .filter(r => r.enabled)
    .sort((a, b) => a.order - b.order)
    .map(r => r.ast);
}

// Strip the internal ast from API responses
function sanitize({ ast: _, ...rest }) { return rest; }
