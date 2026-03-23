/**
 * SNRL Lexer — tokenises a rule string into a flat token list.
 *
 * Grammar overview:
 *   rule     ::= WHEN conditions THEN mutations
 *   cond     ::= field op value (AND|OR cond)*
 *   mutation ::= field = value (, mutation)*
 *
 *   field    ::= WORD                  e.g. source, title, group, category, body
 *   op       ::= = | != | CONTAINS | STARTSWITH | ENDSWITH | MATCHES
 *   value    ::= "string" | NUMBER | WORD
 *
 * Example:
 *   WHEN source = "github" AND title CONTAINS "push"
 *   THEN group = "ci", category = "normal", title = "🔀 {{title}}"
 */

export const T = {
  WHEN: 'WHEN', THEN: 'THEN',
  AND: 'AND',   OR: 'OR',
  EQ: '=',      NEQ: '!=',
  CONTAINS: 'CONTAINS', STARTSWITH: 'STARTSWITH',
  ENDSWITH: 'ENDSWITH', MATCHES: 'MATCHES',
  WORD: 'WORD', STRING: 'STRING', NUMBER: 'NUMBER',
  COMMA: ',',   EOF: 'EOF',
};

const KEYWORDS = new Set([
  'WHEN','THEN','AND','OR',
  'CONTAINS','STARTSWITH','ENDSWITH','MATCHES',
]);

export function lex(src) {
  const tokens = [];
  let i = 0;

  while (i < src.length) {
    // skip whitespace
    if (/\s/.test(src[i])) { i++; continue; }

    // !=
    if (src[i] === '!' && src[i+1] === '=') {
      tokens.push({ type: T.NEQ, value: '!=' }); i += 2; continue;
    }
    // =
    if (src[i] === '=') {
      tokens.push({ type: T.EQ, value: '=' }); i++; continue;
    }
    // ,
    if (src[i] === ',') {
      tokens.push({ type: T.COMMA, value: ',' }); i++; continue;
    }
    // string literal  "..." or '...'
    if (src[i] === '"' || src[i] === "'") {
      const q = src[i++]; let s = '';
      while (i < src.length && src[i] !== q) {
        if (src[i] === '\\') { i++; s += src[i++]; } else s += src[i++];
      }
      i++; // closing quote
      tokens.push({ type: T.STRING, value: s }); continue;
    }
    // number
    if (/\d/.test(src[i])) {
      let n = '';
      while (i < src.length && /[\d.]/.test(src[i])) n += src[i++];
      tokens.push({ type: T.NUMBER, value: Number(n) }); continue;
    }
    // word / keyword
    if (/[A-Za-z_\u0080-\uFFFF]/.test(src[i])) {
      let w = '';
      while (i < src.length && /[\w\u0080-\uFFFF]/.test(src[i])) w += src[i++];
      const up = w.toUpperCase();
      tokens.push({ type: KEYWORDS.has(up) ? up : T.WORD, value: w }); continue;
    }
    // unknown char — skip with warning
    i++;
  }

  tokens.push({ type: T.EOF, value: null });
  return tokens;
}
