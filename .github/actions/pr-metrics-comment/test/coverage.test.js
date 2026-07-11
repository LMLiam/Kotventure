'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { parseCoverage } = require('../lib/coverage.js');

const sampleXml = [
  '<report name="kotventure">',
  '<package name="io/github/lmliam/kotventure/core/text">',
  '<class name="io/github/lmliam/kotventure/core/text/Texts">',
  '<counter type="LINE" missed="99" covered="1"/>',
  '</class>',
  '<sourcefile name="Texts.kt">',
  '<line nr="5" mi="0" ci="3" mb="0" cb="0"/>',
  '<line nr="6" mi="2" ci="0" mb="0" cb="0"/>',
  '<counter type="LINE" missed="99" covered="1"/>',
  '</sourcefile>',
  '<counter type="LINE" missed="10" covered="90"/>',
  '</package>',
  '<package name="io/github/lmliam/kotventure/core/style">',
  '<counter type="LINE" missed="5" covered="45"/>',
  '</package>',
  '<package name="io/github/lmliam/kotventure/test/snapshot">',
  '<counter type="LINE" missed="2" covered="18"/>',
  '</package>',
  '<counter type="LINE" missed="17" covered="153"/>',
  '</report>',
].join('\n');

test('aggregates package counters per module, ignoring class and sourcefile counters', () => {
  const result = parseCoverage(sampleXml);
  assert.deepEqual(result.modules.get('core'), { missed: 15, covered: 135 });
  assert.deepEqual(result.modules.get('test-snapshot'), { missed: 2, covered: 18 });
});

test('reads totals from the report-level counter', () => {
  const result = parseCoverage(sampleXml);
  assert.equal(result.totalMissed, 17);
  assert.equal(result.totalCovered, 153);
});

test('sums module counters when no report-level counter exists', () => {
  const xml = [
    '<report>',
    '<package name="io/github/lmliam/kotventure/core/text">',
    '<counter type="LINE" missed="10" covered="90"/>',
    '</package>',
    '</report>',
  ].join('\n');
  const result = parseCoverage(xml);
  assert.equal(result.totalMissed, 10);
  assert.equal(result.totalCovered, 90);
});

test('skips packages without a LINE counter', () => {
  const xml = '<report><package name="io/github/lmliam/kotventure/core/text"></package></report>';
  const result = parseCoverage(xml);
  assert.equal(result.modules.size, 0);
});

test('exposes per-sourcefile line coverage keyed by package path', () => {
  const result = parseCoverage(sampleXml);
  const lines = result.files.get('io/github/lmliam/kotventure/core/text/Texts.kt');
  assert.deepEqual([...lines.entries()], [[5, true], [6, false]]);
});

test('maps foreign packages to their first path segment', () => {
  const xml = [
    '<report>',
    '<package name="net/kyori/adventure">',
    '<counter type="LINE" missed="1" covered="9"/>',
    '</package>',
    '</report>',
  ].join('\n');
  const result = parseCoverage(xml);
  assert.ok(result.modules.has('net'));
});
