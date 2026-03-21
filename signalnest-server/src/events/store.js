import { config } from '../config.js';

// Simple in-memory ring buffer — enough for a single-user server
const events = [];

export function addEvent(ev) {
  events.unshift({ ...ev, id: ev.id ?? crypto.randomUUID(), ts: ev.ts ?? Date.now() });
  if (events.length > config.maxEvents) events.splice(config.maxEvents);
  return events[0];
}

export function getEvents(limit = 200) {
  return events.slice(0, limit);
}

export function clearEvents() {
  events.splice(0);
}
