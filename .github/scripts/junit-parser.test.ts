import assert from 'node:assert/strict';
import test from 'node:test';
import {
  aggregateJunitReports,
  parseJunitReport,
} from './junit-parser.js';
import {
  buildJunitArtifactName,
  parseJunitArtifactName,
} from './junit-contract.js';

const HEAD_SHA = 'a'.repeat(40);

test('parses passed, failed, error, and skipped JUnit cases', () => {
  const report = parseJunitReport('modules/core/build/test-results/test/TEST-example.xml', Buffer.from(`
    <testsuite name="example">
      <testcase name="passes" classname="ExampleTest" time="0.1" />
      <testcase name="fails" classname="ExampleTest" file="modules/core/src/test/kotlin/ExampleTest.kt" line="18">
        <failure message="expected true">stack</failure>
      </testcase>
      <testcase name="errors" classname="ExampleTest">
        <error>broken</error>
      </testcase>
      <testcase name="skips" classname="ExampleTest" file="modules/core/src/test/kotlin/ExampleTest.kt" line="22">
        <skipped message="not applicable" />
      </testcase>
    </testsuite>
  `));

  assert.deepEqual({
    passed: report.passed,
    failed: report.failed,
    errors: report.errors,
    skipped: report.skipped,
  }, { passed: 1, failed: 1, errors: 1, skipped: 1 });
  assert.equal(report.annotations.length, 2);
  assert.equal(report.annotations[0]?.path, 'modules/core/src/test/kotlin/ExampleTest.kt');
  assert.equal(report.annotations[1]?.level, 'notice');

  const aggregate = aggregateJunitReports([report]);
  assert.equal(aggregate.cases, 4);
  assert.equal(aggregate.failed, 1);
});

test('resolves Gradle failure stack frames to source annotations', () => {
  const report = parseJunitReport('modules/core/build/test-results/test/TEST-example.xml', Buffer.from(`
    <testsuite name="example">
      <testcase name="fails" classname="io.github.lmliam.kotventure.ExampleTest">
        <failure message="expected true">java.lang.AssertionError: expected true
          at io.github.lmliam.kotventure.ExampleTest.fails(ExampleTest.kt:18)
          at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
        </failure>
      </testcase>
    </testsuite>
  `));

  assert.deepEqual(report.annotations[0], {
    path: 'modules/core/src/test/kotlin/io/github/lmliam/kotventure/ExampleTest.kt',
    line: 18,
    level: 'failure',
    title: 'failed: io.github.lmliam.kotventure.ExampleTest.fails',
    message: 'expected true',
  });
});

test('rejects a JUnit document type declaration', () => {
  assert.throws(
    () => parseJunitReport('TEST-invalid.xml', Buffer.from('<!DOCTYPE testsuite><testsuite />')),
    /document type declarations are not allowed/,
  );
});

test('rejects malformed and unsafe JUnit reports', () => {
  assert.throws(
    () => parseJunitReport('TEST-invalid.xml', Buffer.from('<testsuite><testcase></testsuite>')),
    /XML is malformed|root element is invalid/,
  );
  assert.throws(
    () => parseJunitReport('TEST-invalid.xml', Buffer.from('<testsuite><testcase name="bad" file="../../bad.kt" line="1"><failure /></testcase></testsuite>')),
    /test file path is unsafe/,
  );
});

test('builds and parses a bound JUnit artefact name', () => {
  const name = buildJunitArtifactName({
    kind: 'build',
    shard: 'core',
    workflowId: 99,
    runId: 123,
    runAttempt: 2,
    headSha: HEAD_SHA,
  });
  assert.deepEqual(parseJunitArtifactName(name), {
    kind: 'build',
    shard: 'core',
    workflowId: 99,
    runId: 123,
    runAttempt: 2,
    headSha: HEAD_SHA,
  });
  assert.equal(parseJunitArtifactName('junit-results-build-vanilla-123-2-' + HEAD_SHA), null);
});
