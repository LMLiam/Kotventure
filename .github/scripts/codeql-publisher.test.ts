import assert from 'node:assert/strict';
import test from 'node:test';
import { codeqlAnalysisApplies } from './codeql-publisher.js';

test('does not publish CodeQL for non-code pull-request classifications', () => {
  assert.equal(codeqlAnalysisApplies('documentation'), false);
  assert.equal(codeqlAnalysisApplies('release'), false);
  assert.equal(codeqlAnalysisApplies('code'), true);
  assert.equal(codeqlAnalysisApplies(null), true);
});
