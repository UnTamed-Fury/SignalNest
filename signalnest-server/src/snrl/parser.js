/**
 * SNRL Parser — converts token list into an AST.
 *
 * AST shape:
 * {
 *   when: CondNode,        // condition tree
 *   then: MutationNode[]   // list of field assignments
 * }
 *
 * CondNode:
 *   { type: 'AND'|'OR', left: CondNode, right: CondNode }
 *   { type: 'PRED', field, op, value }
 *
 * MutationNode:
 *   { field, value }       // value may contain {{field}} templates
 */
import { T, lex } from './lexer.js';

class ParseError extends Error {
  constructor(msg) { super(`SNRL parse error: ${msg}`); this.name = 'ParseError'; }
}

class Parser {
  constructor(tokens) {
    this.tokens = tokens;
    this.pos    = 0;
  }

  peek()     { return this.tokens[this.pos]; }
  consume()  { return this.tokens[this.pos++]; }
  expect(type) {
    const t = this.consume();
    if (t.type !== type) throw new ParseError(`expected ${type}, got ${t.type} ("${t.value}")`);
    return t;
  }

  parse() {
    this.expect(T.WHEN);
    const when = this.parseConditions();
    this.expect(T.THEN);
    const then = this.parseMutations();
    if (this.peek().type !== T.EOF)
      throw new ParseError(`unexpected token after THEN: ${this.peek().type}`);
    return { when, then };
  }

  // conditions = pred (AND|OR pred)*
  // Parsed left-associatively: a AND b OR c → (a AND b) OR c
  parseConditions() {
    let node = this.parsePred();
    while (this.peek().type === T.AND || this.peek().type === T.OR) {
      const op = this.consume().type; // AND | OR
      const right = this.parsePred();
      node = { type: op, left: node, right };
    }
    return node;
  }

  parsePred() {
    const field = this.expect(T.WORD).value;
    const opTok = this.consume();
    const allowed = new Set([T.EQ, T.NEQ, T.CONTAINS, T.STARTSWITH, T.ENDSWITH, T.MATCHES]);
    if (!allowed.has(opTok.type))
      throw new ParseError(`unknown operator "${opTok.value}"`);
    const valTok = this.consume();
    if (![T.STRING, T.NUMBER, T.WORD].includes(valTok.type))
      throw new ParseError(`expected value, got ${valTok.type}`);
    return { type: 'PRED', field, op: opTok.type, value: valTok.value };
  }

  // mutations = assignment (, assignment)*
  parseMutations() {
    const mutations = [this.parseAssignment()];
    while (this.peek().type === T.COMMA) {
      this.consume(); // eat comma
      if (this.peek().type === T.EOF || this.peek().type === T.THEN) break;
      mutations.push(this.parseAssignment());
    }
    return mutations;
  }

  parseAssignment() {
    const field = this.expect(T.WORD).value;
    this.expect(T.EQ);
    const valTok = this.consume();
    if (![T.STRING, T.NUMBER, T.WORD].includes(valTok.type))
      throw new ParseError(`expected value after = in mutation, got ${valTok.type}`);
    return { field, value: valTok.value };
  }
}

/** Parse an SNRL rule string into an AST. Throws ParseError on failure. */
export function parse(src) {
  const tokens = lex(src);
  return new Parser(tokens).parse();
}
