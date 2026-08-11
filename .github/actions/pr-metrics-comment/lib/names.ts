export function sanitizeModule(name: string): string {
  const cleaned = String(name).replace(/[^a-zA-Z0-9_-]/g, '');
  return cleaned || 'unknown';
}

export function chartLabel(name: string): string {
  return sanitizeModule(name)
    .replace(/^test-snapshot$/, 'test-snap')
    .replace(/^minimessage$/, 'mini');
}
