const SUPPORTED_EVENTS = [
  'pull_request',
  'push',
  'merge_group',
  'schedule',
  'workflow_dispatch',
] as const;

const STATUS_JOB_NAMES = [
  'triage',
  'lintKotlin',
  'lintActions',
  'build',
  'aggregate',
  'dokka',
  'vanilla',
  'dependencies',
] as const;

const JOB_LABELS = {
  triage: 'Triage',
  lintKotlin: 'Lint (Kotlin)',
  lintActions: 'Lint (Actions)',
  build: 'Build',
  aggregate: 'Aggregate',
  dokka: 'Dokka',
  vanilla: 'Vanilla conformance',
  dependencies: 'Dependencies',
} as const satisfies { [Job in StatusJobName]: string };

const WORKFLOW_RESULTS = ['success', 'failure', 'cancelled', 'skipped'] as const;

const OPTIONAL_NON_TRIAGE_JOBS = STATUS_JOB_NAMES.filter((job) => job !== 'triage');
const ALL_JOBS_EXCEPT_VANILLA = STATUS_JOB_NAMES.filter((job) => job !== 'vanilla');

export type CiEvent = typeof SUPPORTED_EVENTS[number];
export type StatusJobName = typeof STATUS_JOB_NAMES[number];
export type WorkflowResult = typeof WORKFLOW_RESULTS[number];

export interface CiStatusTriage {
  run?: string;
  releaseOnly?: string;
  releaseCandidate?: string;
  documentationOnly?: string;
  code?: string;
  vanilla?: string;
}

export type CiStatusResults = {
  [Job in StatusJobName]: string | undefined;
};

export interface CiStatusInput {
  eventName: string;
  triage: CiStatusTriage;
  results: CiStatusResults;
}

export interface CiStatusEvaluation {
  readonly policy: string;
  readonly acceptedSkips: readonly StatusJobName[];
  readonly summary: string;
}

interface ResolvedFlags {
  readonly eventName: CiEvent;
  readonly run: boolean;
  readonly releaseOnly: boolean;
  readonly releaseCandidate: boolean;
  readonly documentationOnly: boolean;
  readonly code: boolean | undefined;
  readonly vanilla: boolean | undefined;
}

interface StatusPolicyRow {
  readonly name: string;
  readonly events: readonly CiEvent[];
  readonly run: boolean;
  readonly releaseOnly: boolean;
  readonly documentationOnly: boolean;
  readonly code: boolean | undefined;
  readonly vanilla: boolean | undefined;
  readonly required: readonly StatusJobName[];
  readonly optional: readonly StatusJobName[];
}

const REQUIRED_TRIAGE: readonly StatusJobName[] = ['triage'];

/**
 * The event and path rows used by the required Status check.
 */
export const STATUS_POLICY_ROWS: readonly StatusPolicyRow[] = [
  {
    name: 'trusted-release-pr',
    events: ['pull_request'],
    run: false,
    releaseOnly: true,
    documentationOnly: false,
    code: undefined,
    vanilla: undefined,
    required: REQUIRED_TRIAGE,
    optional: OPTIONAL_NON_TRIAGE_JOBS,
  },
  {
    name: 'documentation-pr',
    events: ['pull_request'],
    run: true,
    releaseOnly: false,
    documentationOnly: true,
    code: false,
    vanilla: false,
    required: REQUIRED_TRIAGE,
    optional: OPTIONAL_NON_TRIAGE_JOBS,
  },
  {
    name: 'code-pr-without-vanilla',
    events: ['pull_request'],
    run: true,
    releaseOnly: false,
    documentationOnly: false,
    code: true,
    vanilla: false,
    required: ALL_JOBS_EXCEPT_VANILLA,
    optional: ['vanilla'],
  },
  {
    name: 'code-pr-with-vanilla',
    events: ['pull_request'],
    run: true,
    releaseOnly: false,
    documentationOnly: false,
    code: true,
    vanilla: true,
    required: STATUS_JOB_NAMES,
    optional: [],
  },
  {
    name: 'push-without-code',
    events: ['push'],
    run: true,
    releaseOnly: false,
    documentationOnly: false,
    code: false,
    vanilla: false,
    required: REQUIRED_TRIAGE,
    optional: OPTIONAL_NON_TRIAGE_JOBS,
  },
  {
    name: 'push-with-code-without-vanilla',
    events: ['push'],
    run: true,
    releaseOnly: false,
    documentationOnly: false,
    code: true,
    vanilla: false,
    required: ALL_JOBS_EXCEPT_VANILLA,
    optional: ['vanilla'],
  },
  {
    name: 'push-with-code-and-vanilla',
    events: ['push'],
    run: true,
    releaseOnly: false,
    documentationOnly: false,
    code: true,
    vanilla: true,
    required: STATUS_JOB_NAMES,
    optional: [],
  },
  {
    name: 'full-validation',
    events: ['merge_group', 'schedule', 'workflow_dispatch'],
    run: true,
    releaseOnly: false,
    documentationOnly: false,
    code: true,
    vanilla: true,
    required: STATUS_JOB_NAMES,
    optional: [],
  },
];

function displayValue(value: string | undefined): string {
  if (value === undefined) return 'missing';
  return JSON.stringify(value);
}

function parseEvent(value: string | undefined, errors: string[]): CiEvent | undefined {
  if (typeof value === 'string' && (SUPPORTED_EVENTS as readonly string[]).includes(value)) {
    return value as CiEvent;
  }
  errors.push(`eventName must be one of ${SUPPORTED_EVENTS.join(', ')} (got ${displayValue(value)})`);
  return undefined;
}

function parseBooleanOutput(
  value: string | undefined,
  name: string,
  errors: string[],
  allowMissing = false,
): boolean | undefined {
  if (value === undefined && allowMissing) return undefined;
  if (value === 'true') return true;
  if (value === 'false') return false;
  errors.push(`${name} must be "true" or "false" (got ${displayValue(value)})`);
  return undefined;
}

function parseResult(value: string | undefined, job: StatusJobName, errors: string[]): WorkflowResult | undefined {
  if ((WORKFLOW_RESULTS as readonly string[]).includes(value ?? '')) {
    return value as WorkflowResult;
  }
  errors.push(`${JOB_LABELS[job]} result is invalid (got ${displayValue(value)})`);
  return undefined;
}

type ParsedCiStatusResults = {
  [Job in StatusJobName]: WorkflowResult | undefined;
};

function parseResults(input: CiStatusInput, errors: string[]): ParsedCiStatusResults {
  const results = input.results;
  return {
    triage: parseResult(results.triage, 'triage', errors),
    lintKotlin: parseResult(results.lintKotlin, 'lintKotlin', errors),
    lintActions: parseResult(results.lintActions, 'lintActions', errors),
    build: parseResult(results.build, 'build', errors),
    aggregate: parseResult(results.aggregate, 'aggregate', errors),
    dokka: parseResult(results.dokka, 'dokka', errors),
    vanilla: parseResult(results.vanilla, 'vanilla', errors),
    dependencies: parseResult(results.dependencies, 'dependencies', errors),
  };
}

function resolveFlags(input: CiStatusInput, errors: string[]): ResolvedFlags | undefined {
  const eventName = parseEvent(input.eventName, errors);
  const triage = input.triage;

  const run = parseBooleanOutput(triage.run, 'triage.run', errors);
  const releaseOnly = parseBooleanOutput(triage.releaseOnly, 'triage.release_only', errors);
  const releaseCandidate = parseBooleanOutput(triage.releaseCandidate, 'triage.release_candidate', errors);
  const documentationOnly = parseBooleanOutput(triage.documentationOnly, 'triage.documentation_only', errors);
  const pathFlagsMayBeMissing = eventName === 'pull_request' && run === false && releaseOnly === true;
  const code = parseBooleanOutput(triage.code, 'triage.code', errors, pathFlagsMayBeMissing);
  const vanilla = parseBooleanOutput(triage.vanilla, 'triage.vanilla', errors, pathFlagsMayBeMissing);

  if (
    eventName === undefined
    || run === undefined
    || releaseOnly === undefined
    || releaseCandidate === undefined
    || documentationOnly === undefined
  ) return undefined;

  return {
    eventName,
    run,
    releaseOnly,
    releaseCandidate,
    documentationOnly,
    code,
    vanilla,
  };
}

function matchesPolicy(row: StatusPolicyRow, flags: ResolvedFlags): boolean {
  return row.events.includes(flags.eventName)
    && row.run === flags.run
    && row.releaseOnly === flags.releaseOnly
    && row.documentationOnly === flags.documentationOnly
    && row.code === flags.code
    && row.vanilla === flags.vanilla;
}

function findPolicy(flags: ResolvedFlags, errors: string[]): StatusPolicyRow | undefined {
  if (flags.run === false) {
    if (flags.eventName !== 'pull_request' || !flags.releaseOnly || !flags.releaseCandidate || flags.documentationOnly) {
      errors.push('triage flags do not describe a trusted release-only pull request');
    }
    if (flags.code !== undefined || flags.vanilla !== undefined) {
      errors.push('triage.code and triage.vanilla must be missing for a skipped trusted release-only pull request');
    }
  } else {
    if (flags.releaseOnly) errors.push('triage.release_only must be false when CI runs');
    if (flags.eventName === 'pull_request' && flags.documentationOnly && (flags.code || flags.vanilla)) {
      errors.push('documentation-only pull requests cannot set code or vanilla to true');
    }
    if (flags.eventName !== 'pull_request' && flags.documentationOnly) {
      errors.push('documentation_only is supported only for pull requests');
    }
    if (flags.code === false && flags.vanilla === true) {
      errors.push('triage.vanilla cannot be true when triage.code is false');
    }
  }

  const row = STATUS_POLICY_ROWS.find((candidate) => matchesPolicy(candidate, flags));
  if (row === undefined) {
    errors.push(`triage flags do not match a supported Status policy row (event ${flags.eventName})`);
  }
  return row;
}

function validateJobResults(
  row: StatusPolicyRow | undefined,
  results: ParsedCiStatusResults,
  errors: string[],
): StatusJobName[] {
  const acceptedSkips: StatusJobName[] = [];
  for (const job of STATUS_JOB_NAMES) {
    const result = results[job];
    if (result === undefined) continue;

    if (result === 'failure' || result === 'cancelled') {
      errors.push(`${JOB_LABELS[job]} result must not be ${result}`);
      continue;
    }
    if (result === 'skipped') {
      if (row?.optional.includes(job)) {
        acceptedSkips.push(job);
      } else {
        errors.push(`${JOB_LABELS[job]} result is skipped but the policy does not permit skipping`);
      }
    }
  }
  return acceptedSkips;
}

function createSummary(row: StatusPolicyRow, acceptedSkips: readonly StatusJobName[]): string {
  if (acceptedSkips.length === 0) return `Status policy ${row.name} passed; no jobs were skipped.`;
  const jobs = acceptedSkips.map((job) => JOB_LABELS[job]).join(', ');
  return `Status policy ${row.name} passed; accepted skipped jobs: ${jobs}.`;
}

/**
 * Evaluates the completed CI jobs against one event and path policy.
 *
 * @throws Error when the event, triage outputs, or owned job results do not describe a supported policy.
 */
export function evaluateCiStatus(input: CiStatusInput): CiStatusEvaluation {
  const errors: string[] = [];
  const flags = resolveFlags(input, errors);
  const row = flags === undefined ? undefined : findPolicy(flags, errors);
  const results = parseResults(input, errors);
  const acceptedSkips = validateJobResults(row, results, errors);

  if (errors.length > 0 || row === undefined) {
    throw new Error(`CI Status policy rejected the workflow:\n- ${errors.join('\n- ')}`);
  }

  return {
    policy: row.name,
    acceptedSkips,
    summary: createSummary(row, acceptedSkips),
  };
}

export {
  JOB_LABELS,
  STATUS_JOB_NAMES,
};
