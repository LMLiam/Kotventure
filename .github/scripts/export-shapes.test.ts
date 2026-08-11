// Runtime export-shape smoke test.
//
// Workflows and the composite action load these entry points with require()
// at fixed paths. Asserting the exported names here proves that the compiled
// output keeps the same module shapes after the TypeScript conversion.

const test = require('node:test');
const assert = require('node:assert/strict');

const SCRIPTS_DIR = __dirname;

function load(relativePath: string): Record<string, unknown> {
  return require(`${SCRIPTS_DIR}/${relativePath}`);
}

test('CI entry points keep their runtime export shapes', () => {
  // Entry points loaded by ci.yml, codeql.yml, qodana.yml, pr-metrics-publish.yml
  // and qodana-publish.yml.
  const scripts = {
    './ci-gate.js': ['decideGate', 'isDocumentationPath'],
    './pr-metrics-publisher.js': [
      'publishMetrics',
      'resolveSource',
      'validateSource',
    ],
    './pr-metrics-publisher-storage.js': [
      'downloadMetricsArtifact',
      'readMetricsArtifact',
    ],
    './workflow-run-check.js': [
      'buildCheckExternalId',
      'completeWorkflowCheck',
      'createWorkflowCheck',
      'workflowResultConclusion',
    ],
    './qodana-publisher.js': [
      'QodanaPublicationRejectedError',
      'resolvePublication',
      'writePublicationOutputs',
    ],
    './qodana-publisher-storage.js': ['downloadQodanaArtifact'],
    './qodana-source.js': [
      'QodanaSourceRejectedError',
      'hasTrustedReleaseMetadata',
      'resolvePullRequestEventSource',
      'resolvePullRequestSource',
    ],
    './qodana-attestation.js': ['createAttestation', 'writeAttestation'],
  };
  const ciGate = load('./ci-gate.js');
  assert.ok(
    ciGate.RELEASE_ONLY_FILES instanceof Set,
    './ci-gate.js must export RELEASE_ONLY_FILES as a Set',
  );
  for (const [entry, names] of Object.entries(scripts)) {
    const mod = load(entry);
    for (const name of names) {
      assert.equal(typeof mod[name], 'function', `${entry} must export ${name}`);
    }
  }

  // The composite action calls lib/main.js directly.
  const main = load('../actions/pr-metrics-comment/lib/main.js');
  assert.equal(typeof main, 'function', 'lib/main.js must be directly callable');
});
