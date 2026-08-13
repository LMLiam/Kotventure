import assert from 'node:assert/strict';
import test from 'node:test';
import {
  evaluateCiStatus,
  JOB_LABELS,
  STATUS_JOB_NAMES,
  STATUS_POLICY_ROWS,
  type CiStatusTriage,
  type CiStatusResults,
  type CiStatusInput,
} from './ci-status.js';

const ALL_SUCCESS: CiStatusResults = {
  triage: 'success',
  lintKotlin: 'success',
  lintActions: 'success',
  build: 'success',
  aggregate: 'success',
  dokka: 'success',
  vanilla: 'success',
  dependencies: 'success',
};

interface PolicyFixture {
  readonly name: string;
  readonly policy: string;
  readonly input: CiStatusInput;
  readonly required: readonly (keyof CiStatusResults)[];
  readonly optional: readonly (keyof CiStatusResults)[];
}

function createFixture(
  name: string,
  policy: string,
  eventName: string,
  triage: CiStatusTriage,
  required: readonly (keyof CiStatusResults)[],
  optional: readonly (keyof CiStatusResults)[],
): PolicyFixture {
  const results = { ...ALL_SUCCESS };
  for (const job of optional) results[job] = 'skipped';

  return {
    name,
    policy,
    input: { eventName, triage, results },
    required,
    optional,
  };
}

const POLICY_FIXTURES: readonly PolicyFixture[] = [
  createFixture(
    'trusted-release-pr',
    'trusted-release-pr',
    'pull_request',
    { run: 'false', releaseOnly: 'true', releaseCandidate: 'true', documentationOnly: 'false' },
    ['triage'],
    ['lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
  ),
  createFixture(
    'documentation-pr',
    'documentation-pr',
    'pull_request',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'true', code: 'false', vanilla: 'false' },
    ['triage'],
    ['lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
  ),
  createFixture(
    'code-pr-without-vanilla',
    'code-pr-without-vanilla',
    'pull_request',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'false' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'dependencies'],
    ['vanilla'],
  ),
  createFixture(
    'code-pr-with-vanilla',
    'code-pr-with-vanilla',
    'pull_request',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'true' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
    [],
  ),
  createFixture(
    'push-without-code',
    'push-without-code',
    'push',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'false', vanilla: 'false' },
    ['triage'],
    ['lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
  ),
  createFixture(
    'push-with-code-without-vanilla',
    'push-with-code-without-vanilla',
    'push',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'false' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'dependencies'],
    ['vanilla'],
  ),
  createFixture(
    'push-with-code-and-vanilla',
    'push-with-code-and-vanilla',
    'push',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'true' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
    [],
  ),
  createFixture(
    'full-validation-merge-group',
    'full-validation',
    'merge_group',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'true' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
    [],
  ),
  createFixture(
    'full-validation-schedule',
    'full-validation',
    'schedule',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'true' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
    [],
  ),
  createFixture(
    'full-validation-workflow-dispatch',
    'full-validation',
    'workflow_dispatch',
    { run: 'true', releaseOnly: 'false', releaseCandidate: 'false', documentationOnly: 'false', code: 'true', vanilla: 'true' },
    ['triage', 'lintKotlin', 'lintActions', 'build', 'aggregate', 'dokka', 'vanilla', 'dependencies'],
    [],
  ),
];

function fixtureNamed(name: string): PolicyFixture {
  const fixture = POLICY_FIXTURES.find((candidate) => candidate.name === name);
  assert.ok(fixture, `missing policy fixture: ${name}`);
  return fixture;
}

function copyInput(input: CiStatusInput): CiStatusInput {
  const results = input.results;
  return {
    eventName: input.eventName,
    triage: { ...input.triage },
    results: {
      triage: results?.triage,
      lintKotlin: results?.lintKotlin,
      lintActions: results?.lintActions,
      build: results?.build,
      aggregate: results?.aggregate,
      dokka: results?.dokka,
      vanilla: results?.vanilla,
      dependencies: results?.dependencies,
    },
  };
}

for (const fixture of POLICY_FIXTURES) {
  test(`accepts the ${fixture.name} policy row`, () => {
    const evaluation = evaluateCiStatus(fixture.input);

    assert.equal(evaluation.policy, fixture.policy);
    assert.deepEqual(evaluation.acceptedSkips, fixture.optional);
    assert.ok(evaluation.summary.includes(fixture.policy), evaluation.summary);
  });
}

for (const fixture of POLICY_FIXTURES) {
  for (const job of fixture.required) {
    test(`requires success for ${fixture.name} ${job}`, () => {
      const input = copyInput(fixture.input);
      input.results[job] = 'skipped';

      assert.throws(
        () => evaluateCiStatus(input),
        (error) => error instanceof Error
          && error.message.includes(`${JOB_LABELS[job]} result is skipped but the policy does not permit skipping`),
      );
    });
  }

  for (const job of fixture.optional) {
    test(`accepts success for optional ${fixture.name} ${job}`, () => {
      const input = copyInput(fixture.input);
      input.results[job] = 'success';

      assert.doesNotThrow(() => evaluateCiStatus(input));
    });
  }
}

for (const fixture of POLICY_FIXTURES) {
  for (const job of STATUS_JOB_NAMES) {
    for (const result of ['failure', 'cancelled']) {
      test(`rejects ${result} from ${fixture.name} ${job}`, () => {
        const input = copyInput(fixture.input);
        input.results[job] = result;

        assert.throws(
          () => evaluateCiStatus(input),
          (error) => error instanceof Error
            && error.message.includes(`${JOB_LABELS[job]} result must not be ${result}`),
        );
      });
    }
  }
}

for (const job of STATUS_JOB_NAMES) {
  for (const result of ['', 'unknown', undefined]) {
    test(`rejects ${String(result)} as the ${job} result`, () => {
      const input = copyInput(fixtureNamed('code-pr-without-vanilla').input);
      input.results[job] = result;

      assert.throws(
        () => evaluateCiStatus(input),
        (error) => error instanceof Error
          && error.message.includes(`${JOB_LABELS[job]} result is invalid`),
      );
    });
  }
}

test('classifies every owned job exactly once in every policy row', () => {
  for (const policy of STATUS_POLICY_ROWS) {
    const classifiedJobs = [...policy.required, ...policy.optional];
    assert.equal(classifiedJobs.length, STATUS_JOB_NAMES.length, policy.name);
    assert.equal(new Set(classifiedJobs).size, STATUS_JOB_NAMES.length, policy.name);
    assert.deepEqual([...new Set(classifiedJobs)].sort(), [...STATUS_JOB_NAMES].sort(), policy.name);
  }
});

test('rejects an unknown event and still validates all job results', () => {
  const input = copyInput(fixtureNamed('code-pr-without-vanilla').input);
  input.eventName = 'repository_dispatch';
  input.results.build = 'failure';
  input.results.aggregate = 'unknown';

  assert.throws(
    () => evaluateCiStatus(input),
    (error) => {
      assert.ok(error instanceof Error);
      assert.match(error.message, /eventName must be one of/);
      assert.match(error.message, /Build result must not be failure/);
      assert.match(error.message, /Aggregate result is invalid/);
      return true;
    },
  );
});

test('rejects a trusted release skip outside a pull request', () => {
  const input = copyInput(fixtureNamed('trusted-release-pr').input);
  input.eventName = 'push';

  assert.throws(
    () => evaluateCiStatus(input),
    /trusted release-only pull request/,
  );
});

test('rejects a partial full-validation event', () => {
  const input = copyInput(fixtureNamed('full-validation-merge-group').input);
  input.triage.vanilla = 'false';

  assert.throws(
    () => evaluateCiStatus(input),
    /do not match a supported Status policy row/,
  );
});

test('rejects contradictory documentation flags', () => {
  const input = copyInput(fixtureNamed('documentation-pr').input);
  input.triage.code = 'true';

  assert.throws(
    () => evaluateCiStatus(input),
    /documentation-only pull requests cannot set code or vanilla to true/,
  );
});

test('rejects release_only when CI runs', () => {
  const input = copyInput(fixtureNamed('code-pr-without-vanilla').input);
  input.triage.releaseOnly = 'true';

  assert.throws(
    () => evaluateCiStatus(input),
    /triage\.release_only must be false when CI runs/,
  );
});

test('rejects documentation_only outside a pull request', () => {
  const input = copyInput(fixtureNamed('push-without-code').input);
  input.triage.documentationOnly = 'true';

  assert.throws(
    () => evaluateCiStatus(input),
    /documentation_only is supported only for pull requests/,
  );
});

test('rejects vanilla without code', () => {
  const input = copyInput(fixtureNamed('push-without-code').input);
  input.triage.vanilla = 'true';

  assert.throws(
    () => evaluateCiStatus(input),
    /triage\.vanilla cannot be true when triage\.code is false/,
  );
});

test('rejects path flags on a skipped trusted release pull request', () => {
  const input = copyInput(fixtureNamed('trusted-release-pr').input);
  input.triage.code = 'true';

  assert.throws(
    () => evaluateCiStatus(input),
    /triage\.code and triage\.vanilla must be missing/,
  );
});

test('rejects missing path flags outside a trusted release pull request', () => {
  const input = copyInput(fixtureNamed('code-pr-without-vanilla').input);
  delete input.triage.code;
  delete input.triage.vanilla;

  assert.throws(
    () => evaluateCiStatus(input),
    /triage\.code must be "true" or "false" \(got missing\)/,
  );
});

test('reports multiple invalid job results in one error', () => {
  const input = copyInput(fixtureNamed('code-pr-without-vanilla').input);
  input.results.build = 'failure';
  input.results.aggregate = 'cancelled';
  input.results.dokka = 'unknown';

  assert.throws(
    () => evaluateCiStatus(input),
    (error) => {
      assert.ok(error instanceof Error);
      assert.match(error.message, /Build result must not be failure/);
      assert.match(error.message, /Aggregate result must not be cancelled/);
      assert.match(error.message, /Dokka result is invalid/);
      return true;
    },
  );
});
