/**
 * SNRL Validator
 * Validates a rule string and returns { ok, ast, error }.
 * Also checks that field names and mutation targets are known.
 */
import { parse } from './parser.js';

const KNOWN_FIELDS    = new Set(['title','body','source','group','category','channel','rawPayload','id']);
const MUTABLE_FIELDS  = new Set(['title','body','group','category','source']);
const MAX_RULE_LENGTH = 2000;

export function validate(ruleText) {
  if (!ruleText || typeof ruleText !== 'string')
    return { ok: false, error: 'Rule must be a non-empty string' };
  if (ruleText.length > MAX_RULE_LENGTH)
    return { ok: false, error: `Rule too long (max ${MAX_RULE_LENGTH} chars)` };

  let ast;
  try {
    ast = parse(ruleText);
  } catch (e) {
    return { ok: false, error: e.message };
  }

  // Validate condition field names (warn but don't reject unknown — allows custom event fields)
  const warnings = [];
  checkCondFields(ast.when, warnings);

  // Validate mutation targets
  for (const { field } of ast.then) {
    if (!MUTABLE_FIELDS.has(field)) {
      return { ok: false, error: `Cannot mutate field "${field}". Allowed: ${[...MUTABLE_FIELDS].join(', ')}` };
    }
  }

  return { ok: true, ast, warnings };
}

function checkCondFields(node, warnings) {
  if (!node) return;
  if (node.type === 'PRED') {
    if (!KNOWN_FIELDS.has(node.field))
      warnings.push(`Unknown condition field "${node.field}" — may always be empty`);
    return;
  }
  checkCondFields(node.left,  warnings);
  checkCondFields(node.right, warnings);
}
