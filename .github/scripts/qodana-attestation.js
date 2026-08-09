'use strict';

const fs = require('node:fs');
const path = require('node:path');

function attestationFor(sourceKind) {
  if (sourceKind === 'documentation') {
    return {
      version: 'trusted-documentation-only-attestation',
      rule: {
        id: 'attestation/documentation-only-paths',
        name: 'Documentation-only paths',
        shortDescription: 'The pull request changed only approved documentation paths.',
        fullDescription: 'The trusted path classifier approved every changed path as documentation-only.',
      },
    };
  }
  if (sourceKind === 'release') {
    return {
      version: 'trusted-release-allowlist-attestation',
      rule: {
        id: 'attestation/release-please-files',
        name: 'Release Please file allowlist',
        shortDescription: 'Release Please changed only the approved release files.',
        fullDescription: 'The trusted Release Please provenance check approved the release file allowlist.',
      },
    };
  }
  throw new Error(`Cannot create an attestation for source kind ${sourceKind}`);
}

function createAttestation({ sourceKind, headSha }) {
  if (typeof headSha !== 'string' || !/^[0-9a-f]{40}$/.test(headSha)) {
    throw new Error('attestation head SHA is invalid');
  }
  const details = attestationFor(sourceKind);
  return {
    $schema: 'https://json.schemastore.org/sarif-2.1.0.json',
    version: '2.1.0',
    runs: [{
      tool: {
        driver: {
          name: 'QDJVM',
          version: details.version,
          rules: [{
            id: details.rule.id,
            name: details.rule.name,
            shortDescription: { text: details.rule.shortDescription },
            fullDescription: { text: details.rule.fullDescription },
            defaultConfiguration: { level: 'note' },
          }],
        },
      },
      results: [],
    }],
  };
}

function writeAttestation({ sourceKind, headSha, outputPath }) {
  if (typeof outputPath !== 'string' || outputPath.length === 0) {
    throw new Error('attestation output path is required');
  }
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify(createAttestation({ sourceKind, headSha })));
}

module.exports = { createAttestation, writeAttestation };
