import * as fs from 'node:fs';
import * as path from 'node:path';
import { parseDocument } from 'yaml';

const WRITE_PERMISSIONS = new Set(['write', 'write-all']);
const TRUSTED_EVENTS = new Set(['pull_request_target', 'workflow_run']);
const VALIDATION_ENTRY_POINTS = [
  'junit-publisher',
  'codeql-publisher',
  'qodana-publisher',
  'pr-metrics-publisher',
];

type RecordValue = Record<string, unknown>;

export interface WorkflowTrustViolation {
  file: string;
  job: string;
  message: string;
}

function isRecord(value: unknown): value is RecordValue {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function asRecord(value: unknown): RecordValue {
  return isRecord(value) ? value : {};
}

function workflowEvents(value: unknown): Set<string> {
  if (typeof value === 'string') return new Set([value]);
  if (Array.isArray(value)) return new Set(value.filter((entry): entry is string => typeof entry === 'string'));
  if (!isRecord(value)) return new Set();
  return new Set(Object.keys(value));
}

function hasWritePermission(value: unknown): boolean {
  if (value === 'write-all') return true;
  if (!isRecord(value)) return false;
  return Object.values(value).some((permission) => typeof permission === 'string' && WRITE_PERMISSIONS.has(permission));
}

function permissionForJob(workflow: RecordValue, job: RecordValue): unknown {
  return job.permissions === undefined ? workflow.permissions : job.permissions;
}

function steps(job: RecordValue): RecordValue[] {
  if (!Array.isArray(job.steps)) return [];
  return job.steps.filter(isRecord);
}

function stepUses(step: RecordValue): string | null {
  return typeof step.uses === 'string' ? step.uses : null;
}

function isCheckout(step: RecordValue): boolean {
  const uses = stepUses(step);
  return uses != null && uses.startsWith('actions/checkout@');
}

function isLocalAction(step: RecordValue): boolean {
  const uses = stepUses(step);
  return uses != null && uses.startsWith('./');
}

function checkoutRef(step: RecordValue): string | null {
  const withValues = asRecord(step.with);
  return typeof withValues.ref === 'string' ? withValues.ref : null;
}

function isTrustedCheckout(step: RecordValue): boolean {
  const ref = checkoutRef(step);
  return ref === '${{ github.sha }}'
    || ref === '${{ github.event.repository.default_branch }}';
}

function containsUntrustedRunExpression(step: RecordValue): boolean {
  if (typeof step.run !== 'string') return false;
  return /\$\{\{\s*github\.event\.(?:pull_request|workflow_run|issue|comment|review|inputs)\b/.test(step.run);
}

function hasExecution(jobSteps: RecordValue[]): boolean {
  return jobSteps.some((step) => typeof step.run === 'string' || stepUses(step) != null);
}

function hasValidationEntryPoint(jobSteps: RecordValue[]): boolean {
  const source = jobSteps.map((step) => JSON.stringify(step)).join('\n');
  return VALIDATION_ENTRY_POINTS.some((entryPoint) => source.includes(entryPoint));
}

function eventsForJob(job: RecordValue, events: Set<string>): Set<string> {
  const condition = typeof job.if === 'string' ? job.if : '';
  return new Set([...events].filter((event) => {
    if (event === 'pull_request' && /github\.event_name\s*!=\s*'pull_request'/.test(condition)) return false;
    if (event === 'merge_group' && /github\.event_name\s*!=\s*'merge_group'/.test(condition)) return false;
    return true;
  }));
}

function inspectJob({
  workflow,
  jobName,
  job,
  events,
}: {
  workflow: RecordValue;
  jobName: string;
  job: RecordValue;
  events: Set<string>;
}): WorkflowTrustViolation[] {
  const violations: WorkflowTrustViolation[] = [];
  const jobSteps = steps(job);
  const jobEvents = eventsForJob(job, events);
  const writeCapable = hasWritePermission(permissionForJob(workflow, job));
  if (!writeCapable || !hasExecution(jobSteps)) return violations;

  if (jobEvents.has('pull_request')) {
    violations.push({
      file: '',
      job: jobName,
      message: 'pull_request execution has write permission',
    });
  }

  for (const step of jobSteps) {
    if (isLocalAction(step) && (jobEvents.has('pull_request') || eventsHasTrustedBoundary(jobEvents))) {
      violations.push({ file: '', job: jobName, message: 'write-capable trusted job invokes a local action' });
    }
    if (containsUntrustedRunExpression(step) && (jobEvents.has('pull_request') || eventsHasTrustedBoundary(jobEvents))) {
      violations.push({ file: '', job: jobName, message: 'run command interpolates untrusted event text' });
    }
    if (isCheckout(step) && eventsHasTrustedBoundary(jobEvents) && !isTrustedCheckout(step)) {
      violations.push({ file: '', job: jobName, message: 'write-capable trusted job checks out an untrusted ref' });
    }
  }

  if (jobEvents.has('workflow_run') && !hasValidationEntryPoint(jobSteps)) {
    violations.push({ file: '', job: jobName, message: 'workflow_run publisher has no recognised validation entry point' });
  }
  return violations;
}

function eventsHasTrustedBoundary(events: Set<string>): boolean {
  return [...TRUSTED_EVENTS].some((event) => events.has(event));
}

export function validateWorkflowDocument(value: unknown, file = '<workflow>'): WorkflowTrustViolation[] {
  const workflow = asRecord(value);
  const events = workflowEvents(workflow.on);
  const jobs = asRecord(workflow.jobs);
  const violations: WorkflowTrustViolation[] = [];
  for (const [jobName, valueForJob] of Object.entries(jobs)) {
    const job = asRecord(valueForJob);
    for (const violation of inspectJob({ workflow, jobName, job, events })) {
      violations.push({ ...violation, file });
    }
  }
  return violations;
}

export function parseWorkflowDocument(source: string, file = '<workflow>'): unknown {
  const document = parseDocument(source, { stringKeys: true });
  if (document.errors.length > 0) {
    throw new Error(`${file}: ${document.errors.map((error) => error.message).join('; ')}`);
  }
  return document.toJS();
}

export function validateWorkflowSource(source: string, file = '<workflow>'): WorkflowTrustViolation[] {
  return validateWorkflowDocument(parseWorkflowDocument(source, file), file);
}

export function validateWorkflowDirectory(directory: string): WorkflowTrustViolation[] {
  const violations: WorkflowTrustViolation[] = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (!entry.isFile() || !/\.(?:yml|yaml)$/.test(entry.name)) continue;
    const file = path.join(directory, entry.name);
    const source = fs.readFileSync(file, 'utf8');
    violations.push(...validateWorkflowSource(source, file));
  }
  return violations;
}

export {
  containsUntrustedRunExpression,
  hasWritePermission,
  isTrustedCheckout,
};
