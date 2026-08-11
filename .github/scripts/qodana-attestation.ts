import * as fs from 'node:fs';
import * as path from 'node:path';

export type AttestationSourceKind = 'documentation' | 'release';

export interface QodanaSarifLocation {
  uri?: string | null;
  uriBaseId?: string | null;
}

export interface QodanaSarifRule {
  id: string;
  name: string;
  shortDescription: { text: string };
  fullDescription: { text: string };
  defaultConfiguration: { level: string };
}

export interface QodanaSarifArtifact {
  location?: QodanaSarifLocation | null;
}

export interface QodanaSarifRun {
  tool: {
    driver: {
      name: string;
      version: string;
      rules: QodanaSarifRule[];
    };
  };
  results: unknown[];
  artifacts?: QodanaSarifArtifact[];
  originalUriBaseIds?: Record<string, QodanaSarifLocation>;
}

export interface QodanaSarifDocument {
  $schema: string;
  version: string;
  runs: QodanaSarifRun[];
}

interface AttestationRuleDetails {
  version: string;
  rule: {
    id: string;
    name: string;
    shortDescription: string;
    fullDescription: string;
  };
}

function attestationFor(sourceKind: AttestationSourceKind): AttestationRuleDetails {
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

export interface CreateAttestationOptions {
  sourceKind: AttestationSourceKind;
  headSha: string;
}

export function createAttestation({ sourceKind, headSha }: CreateAttestationOptions): QodanaSarifDocument {
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

export interface WriteAttestationOptions extends CreateAttestationOptions {
  outputPath: string;
}

export function writeAttestation({ sourceKind, headSha, outputPath }: WriteAttestationOptions): void {
  if (typeof outputPath !== 'string' || outputPath.length === 0) throw new Error('attestation output path is required');
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify(createAttestation({ sourceKind, headSha })));
}
