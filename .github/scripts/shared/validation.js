'use strict';

function createValidators(reject) {
  function requireObject(value, label) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
      reject(`${label} is missing`);
    }
    return value;
  }

  function requireEqual(actual, expected, label, stale = false) {
    if (actual !== expected) {
      reject(`${label} does not match the trusted value`, stale);
    }
    return actual;
  }

  function requireInteger(value, label, minimum = 1, maximum = Number.MAX_SAFE_INTEGER) {
    if (!Number.isSafeInteger(value) || value < minimum || value > maximum) {
      reject(`${label} is invalid`);
    }
    return value;
  }

  function requireString(value, label) {
    if (typeof value !== 'string' || value.length === 0) {
      reject(`${label} is invalid`);
    }
    return value;
  }

  function requireText(value, label, maximumLength) {
    if (typeof value !== 'string' || value.length < 1 || value.length > maximumLength) {
      reject(`${label} is invalid`);
    }
    return value;
  }

  function requireSha(value, label) {
    if (typeof value !== 'string' || !/^[0-9a-f]{40}$/.test(value)) {
      reject(`${label} is invalid`);
    }
    return value;
  }

  return {
    requireEqual,
    requireInteger,
    requireObject,
    requireSha,
    requireString,
    requireText,
  };
}

module.exports = { createValidators };
