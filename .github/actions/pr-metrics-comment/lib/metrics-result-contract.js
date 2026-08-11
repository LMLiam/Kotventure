'use strict';

const SCHEMA_VERSION = 1;
const WORKFLOW_NAME = 'CI';
const MAX_RESULT_BYTES = 64 * 1024;
const MAX_MODULES = 32;
const MAX_DECLARATIONS = 100;
const MAX_REF_LENGTH = 200;
const MAX_DECLARATION_LENGTH = 120;
const MAX_COUNT = 1_000_000_000;
const MODULE_PATTERN = /^(?:[A-Za-z0-9_-]){1,32}(?![\s\S])/;
const REPOSITORY_PATTERN = /^(?:[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+)(?![\s\S])/;
const SHA_PATTERN = /^(?:[a-f0-9]){40}(?![\s\S])/;
const CONTROL_CODE_POINT_MAX = 0x1f;
const C1_CONTROL_CODE_POINT_MIN = 0x80;
const C1_CONTROL_CODE_POINT_MAX = 0x9f;
const DELETE_CODE_POINT = 0x7f;
const LINE_SEPARATOR_CODE_POINT = 0x2028;
const PARAGRAPH_SEPARATOR_CODE_POINT = 0x2029;
const BIDI_CONTROL_CODE_POINTS = new Set([
  0x202a,
  0x202b,
  0x202c,
  0x202d,
  0x202e,
  0x2066,
  0x2067,
  0x2068,
  0x2069,
]);
const ZERO_WIDTH_CODE_POINTS = new Set([
  0x200b,
  0x200c,
  0x200d,
  0x2060,
  0xfeff,
]);

function exactKeys(value, expected, label) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error(`${label} must be an object`);
  const actual = Object.keys(value).sort();
  const keys = [...expected].sort();
  if (actual.length !== keys.length || actual.some((key, index) => key !== keys[index])) {
    throw new Error(`${label} has unexpected properties`);
  }
}

function boundedInteger(value, label, minimum = 0, maximum = MAX_COUNT) {
  if (!Number.isSafeInteger(value) || value < minimum || value > maximum) {
    throw new Error(`${label} must be an integer from ${minimum} to ${maximum}`);
  }
  return value;
}

function boundedString(value, pattern, label) {
  if (typeof value !== 'string' || !pattern.test(value)) throw new Error(`${label} has an invalid value`);
  return value;
}

function hasUnsafeTextCharacter(value, allowBacktick) {
  for (const character of value) {
    const codePoint = character.codePointAt(0);
    if (codePoint <= CONTROL_CODE_POINT_MAX
      || codePoint >= C1_CONTROL_CODE_POINT_MIN && codePoint <= C1_CONTROL_CODE_POINT_MAX
      || codePoint === DELETE_CODE_POINT
      || codePoint === LINE_SEPARATOR_CODE_POINT
      || codePoint === PARAGRAPH_SEPARATOR_CODE_POINT
      || BIDI_CONTROL_CODE_POINTS.has(codePoint)
      || ZERO_WIDTH_CODE_POINTS.has(codePoint)
      || (!allowBacktick && character === '`')) {
      return true;
    }
  }
  return false;
}

function boundedRef(value, label) {
  if (typeof value !== 'string' || value.length < 1 || value.length > MAX_REF_LENGTH
    || hasUnsafeTextCharacter(value, true)) {
    throw new Error(`${label} has an invalid value`);
  }
  return value;
}

function boundedDeclaration(value, label) {
  if (typeof value !== 'string' || value.length < 1 || value.length > MAX_DECLARATION_LENGTH
    || hasUnsafeTextCharacter(value, false)) {
    throw new Error(`${label} has an invalid value`);
  }
  return value;
}

module.exports = {
  MAX_COUNT,
  MAX_DECLARATIONS,
  MAX_DECLARATION_LENGTH,
  MAX_MODULES,
  MAX_REF_LENGTH,
  MAX_RESULT_BYTES,
  MODULE_PATTERN,
  REPOSITORY_PATTERN,
  SCHEMA_VERSION,
  SHA_PATTERN,
  WORKFLOW_NAME,
  boundedDeclaration,
  boundedInteger,
  boundedRef,
  boundedString,
  exactKeys,
};
