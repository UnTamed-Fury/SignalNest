/**
 * SNRL Execution Engine
 *
 * Takes a parsed AST and an event object.
 * Returns a (possibly mutated) copy of the event if the WHEN condition
 * matches, or null if the rule does not apply.
 *
 * Mutation values support {{field}} template interpolation from the
 * original event fields, e.g.  title = "🔀 {{title}} — {{source}}"
 */

/** Evaluate a single condition node against an event. */
function evalCond(node, event) {
  if (node.type === 'AND') return evalCond(node.left, event) && evalCond(node.right, event);
  if (node.type === 'OR')  return evalCond(node.left, event) || evalCond(node.right, event);

  // PRED
  const raw   = event[node.field];
  const field = raw != null ? String(raw).toLowerCase() : '';
  const val   = String(node.value).toLowerCase();

  switch (node.op) {
    case '=':          return field === val;
    case '!=':         return field !== val;
    case 'CONTAINS':   return field.includes(val);
    case 'STARTSWITH': return field.startsWith(val);
    case 'ENDSWITH':   return field.endsWith(val);
    case 'MATCHES':    return new RegExp(node.value, 'i').test(String(raw ?? ''));
    default:           return false;
  }
}

/** Interpolate {{field}} templates in a mutation value. */
function interpolate(template, event) {
  return String(template).replace(/\{\{(\w+)\}\}/g, (_, k) => event[k] ?? '');
}

/**
 * Apply an AST rule to an event.
 * @param {object} ast     - result of parse(ruleText)
 * @param {object} event   - the raw event object from the store
 * @returns {object|null}  - mutated event copy, or null if rule didn't match
 */
export function applyRule(ast, event) {
  if (!evalCond(ast.when, event)) return null;

  const mutated = { ...event };
  for (const { field, value } of ast.then) {
    mutated[field] = interpolate(value, event);
  }
  return mutated;
}

/**
 * Apply an ordered list of AST rules to an event.
 * Rules are applied in order; each rule sees the output of the previous.
 * @param {object[]} asts
 * @param {object}   event
 * @returns {object} - final (possibly mutated) event
 */
export function applyRules(asts, event) {
  let current = event;
  for (const ast of asts) {
    const result = applyRule(ast, current);
    if (result !== null) current = result;
  }
  return current;
}
