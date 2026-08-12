export type ValidatorReject = (message: string, stale?: boolean) => never;

export function createValidators(reject: ValidatorReject) {
  function requireObject<T extends object>(value: unknown, label: string): T {
    if (!value || typeof value !== 'object' || Array.isArray(value)) reject(`${label} is missing`);
    return value as T;
  }

  function requireEqual<T>(actual: T, expected: T, label: string, stale = false): T {
    if (actual !== expected) reject(`${label} does not match the trusted value`, stale);
    return actual;
  }

  function requireBoundedInteger(value: unknown, label: string, minimum = 1, maximum = Number.MAX_SAFE_INTEGER): number {
    if (typeof value !== 'number' || !Number.isSafeInteger(value) || value < minimum || value > maximum) {
      reject(`${label} is invalid`);
    }
    return value;
  }

  function requireString(value: unknown, label: string): string {
    if (typeof value !== 'string' || value.length === 0) reject(`${label} is invalid`);
    return value;
  }

  function requireText(value: unknown, label: string, maximumLength: number): string {
    if (typeof value !== 'string' || value.length < 1 || value.length > maximumLength) reject(`${label} is invalid`);
    return value;
  }

  function requireSha(value: unknown, label: string): string {
    if (typeof value !== 'string' || !/^[0-9a-f]{40}$/.test(value)) reject(`${label} is invalid`);
    return value;
  }

  return {
    requireBoundedInteger,
    requireEqual,
    requireObject,
    requireSha,
    requireString,
    requireText,
  };
}
