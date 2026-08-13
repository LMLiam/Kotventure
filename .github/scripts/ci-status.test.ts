import assert from 'node:assert/strict';
import test from 'node:test';
import {
  evaluateCiStatus,
  JOB_LABELS,
  STATUS_JOB_NAMES,
  STATUS_POLICY_ROWS,
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

function booleanOutput(value: boolean): string {
  return value ? 'true' : 'false';
}

function fixtureForPolicy(policyName: string): CiStatusInput {
  const policy = STATUS_POLICY_ROWS.find((candidate) => candidate.name === policyName);
  assert.ok(policy, `missing policy fixture: ${policyName}`);
  const eventName = policy.events[0];
  assert.ok(eventName);

  const results = { ...ALL_SUCCESS };
  for (const job of policy.optional) results[job] = 'skipped';

  return {
    eventName,
    triage: {
      run: booleanOutput(policy.run),
      releaseOnly: booleanOutput(policy.releaseOnly),
      releaseCandidate: booleanOutput(policy.name === 'trusted-release-pr'),
      documentationOnly: booleanOutput(policy.documentationOnly),
      ...(policy.code === undefined ? {} : { code: booleanOutput(policy.code) }),
      ...(policy.vanilla === undefined ? {} : { vanilla: booleanOutput(policy.vanilla) }),
    },
    results,
  };
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

for (const policy of STATUS_POLICY_ROWS) {
  test(`accepts the ${policy.name} policy row`, () => {
    const input = fixtureForPolicy(policy.name);
    const evaluation = evaluateCiStatus(input);

    assert.equal(evaluation.policy, policy.name);
    assert.deepEqual(evaluation.acceptedSkips, policy.optional);
    assert.match(evaluation.summary, new RegExp(policy.name));
  });
}

for (const policy of STATUS_POLICY_ROWS) {
  for (const job of policy.required) {
    test(`requires success for ${policy.name} ${job}`, () => {
      const input = copyInput(fixtureForPolicy(policy.name));
      input.results[job] = 'skipped';

      assert.throws(
        () => evaluateCiStatus(input),
        (error) => error instanceof Error
          && error.message.includes(`${JOB_LABELS[job]} result is skipped`),
      );
    });
  }

  for (const job of policy.optional) {
    test(`accepts success for optional ${policy.name} ${job}`, () => {
      const input = copyInput(fixtureForPolicy(policy.name));
      input.results[job] = 'success';

      assert.doesNotThrow(() => evaluateCiStatus(input));
    });
  }
}

for (const policy of STATUS_POLICY_ROWS) {
  for (const job of STATUS_JOB_NAMES) {
    for (const result of ['failure', 'cancelled']) {
      test(`rejects ${result} from ${policy.name} ${job}`, () => {
        const input = copyInput(fixtureForPolicy(policy.name));
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
      const input = copyInput(fixtureForPolicy('code-pr-without-vanilla'));
        input.results[job] = result;

        assert.throws(
          () => evaluateCiStatus(input),
          (error) => error instanceof Error
            && error.message.includes(`${JOB_LABELS[job]} result is invalid`),
        );
    });
  }
}

test('rejects an unknown event and still validates all job results', () => {
  const input = copyInput(fixtureForPolicy('code-pr-without-vanilla'));
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
  const input = copyInput(fixtureForPolicy('trusted-release-pr'));
  input.eventName = 'push';

  assert.throws(
    () => evaluateCiStatus(input),
    /trusted release-only pull request/,
  );
});

test('rejects a partial full-validation event', () => {
  const input = copyInput(fixtureForPolicy('full-validation'));
  input.triage.vanilla = 'false';

  assert.throws(
    () => evaluateCiStatus(input),
    /do not match a supported Status policy row/,
  );
});

test('rejects contradictory documentation flags', () => {
  const input = copyInput(fixtureForPolicy('documentation-pr'));
  input.triage.code = 'true';

  assert.throws(
    () => evaluateCiStatus(input),
    /documentation-only pull requests cannot set code or vanilla to true/,
  );
});

test('rejects missing path flags outside a trusted release pull request', () => {
  const input = copyInput(fixtureForPolicy('code-pr-without-vanilla'));
  delete input.triage.code;
  delete input.triage.vanilla;

  assert.throws(
    () => evaluateCiStatus(input),
    /triage\.code must be "true" or "false" \(got missing\)/,
  );
});

test('reports multiple invalid job results in one error', () => {
  const input = copyInput(fixtureForPolicy('code-pr-without-vanilla'));
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
