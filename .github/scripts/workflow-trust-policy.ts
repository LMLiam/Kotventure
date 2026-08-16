import * as fs from 'node:fs';
import * as path from 'node:path';
import { parseDocument } from 'yaml';
import { z } from 'zod';

const WRITE_PERMISSIONS = new Set(['write', 'write-all']);
const TRUSTED_EVENTS = new Set(['pull_request_target', 'workflow_run']);
const VALIDATION_ENTRY_POINTS = [
  'junit-publisher',
  'codeql-publisher',
  'qodana-publisher',
  'pr-metrics-publisher',
];
const UNTRUSTED_EVENT_MEMBERS = [
  'pull_request',
  'pull_request_review',
  'pull_request_review_comment',
  'issue',
  'issue_comment',
  'discussion',
  'discussion_comment',
  'commit_comment',
  'workflow_run',
  'client_payload',
];
const UNTRUSTED_EVENT_EXPRESSION = new RegExp(`\\$\\{\\{\\s*github\\.event\\.(?:${UNTRUSTED_EVENT_MEMBERS.join('|')})\\b`);

export interface WorkflowTrustViolation {
  file: string;
  job: string;
  message: string;
}

const permissionLevelSchema = z.enum(['read', 'write', 'none', 'read-all', 'write-all']);

const permissionsSchema = z.union([
  permissionLevelSchema,
  z.record(z.string(), permissionLevelSchema),
]);

const eventNamesSchema = z.union([
  z.string(),
  z.array(z.string()),
  z.object({}).passthrough(),
]).transform((trigger): string[] => {
  if (typeof trigger === 'string') return [trigger];
  if (Array.isArray(trigger)) return trigger;
  return Object.keys(trigger);
});

const stepSchema = z.object({
  uses: z.string().optional(),
  run: z.string().optional(),
  with: z.object({
    ref: z.string().optional(),
    script: z.string().optional(),
  }).passthrough().optional(),
}).passthrough();

const jobSchema = z.object({
  if: z.string().optional(),
  permissions: permissionsSchema.optional(),
  steps: z.array(stepSchema).optional(),
}).passthrough();

const workflowSchema = z.object({
  on: eventNamesSchema,
  permissions: permissionsSchema.optional(),
  jobs: z.record(z.string(), jobSchema),
}).passthrough();

type WorkflowDocument = z.infer<typeof workflowSchema>;
type Job = z.infer<typeof jobSchema>;
type Step = z.infer<typeof stepSchema>;
type Permissions = z.infer<typeof permissionsSchema>;

function grantsWrite(permissions: Permissions): boolean {
  if (typeof permissions === 'string') return WRITE_PERMISSIONS.has(permissions);
  return Object.values(permissions).some((level) => WRITE_PERMISSIONS.has(level));
}

function writeCapable(workflow: WorkflowDocument, job: Job): boolean {
  const permissions = job.permissions ?? workflow.permissions;
  return permissions !== undefined && grantsWrite(permissions);
}

function eventsForJob(job: Job, events: ReadonlySet<string>): Set<string> {
  const condition = job.if ?? '';
  return new Set([...events].filter((event) => {
    if (event === 'pull_request' && /github\.event_name\s*!=\s*'pull_request'/.test(condition)) return false;
    if (event === 'merge_group' && /github\.event_name\s*!=\s*'merge_group'/.test(condition)) return false;
    return true;
  }));
}

function hasExecution(steps: Step[]): boolean {
  return steps.some((step) => step.run !== undefined || step.uses !== undefined);
}

function hasValidationEntryPoint(steps: Step[]): boolean {
  return steps.some((step) => {
    const searchable = [step.uses, step.run, step.with?.script].filter(
      (value): value is string => value !== undefined,
    );
    return VALIDATION_ENTRY_POINTS.some((entryPoint) =>
      searchable.some((text) => text.includes(entryPoint)));
  });
}

function containsUntrustedRunExpression(step: Step): boolean {
  return step.run !== undefined && UNTRUSTED_EVENT_EXPRESSION.test(step.run);
}

function isCheckout(step: Step): boolean {
  return step.uses !== undefined && step.uses.startsWith('actions/checkout@');
}

function isLocalAction(step: Step): boolean {
  return step.uses !== undefined && step.uses.startsWith('./');
}

function isTrustedCheckout(step: Step): boolean {
  return step.with?.ref === '${{ github.sha }}'
    || step.with?.ref === '${{ github.event.repository.default_branch }}';
}

function eventsHaveTrustedBoundary(events: ReadonlySet<string>): boolean {
  return [...TRUSTED_EVENTS].some((event) => events.has(event));
}

function inspectJob({
  workflow,
  jobName,
  job,
  events,
}: {
  workflow: WorkflowDocument;
  jobName: string;
  job: Job;
  events: ReadonlySet<string>;
}): WorkflowTrustViolation[] {
  const violations: WorkflowTrustViolation[] = [];
  const jobSteps = job.steps ?? [];
  const jobEvents = eventsForJob(job, events);
  if (!writeCapable(workflow, job) || !hasExecution(jobSteps)) return violations;

  if (jobEvents.has('pull_request')) {
    violations.push({
      file: '',
      job: jobName,
      message: 'pull_request execution has write permission',
    });
  }

  for (const step of jobSteps) {
    if (isLocalAction(step) && (jobEvents.has('pull_request') || eventsHaveTrustedBoundary(jobEvents))) {
      violations.push({ file: '', job: jobName, message: 'write-capable trusted job invokes a local action' });
    }
    if (containsUntrustedRunExpression(step) && (jobEvents.has('pull_request') || eventsHaveTrustedBoundary(jobEvents))) {
      violations.push({ file: '', job: jobName, message: 'run command interpolates untrusted event text' });
    }
    if (isCheckout(step) && eventsHaveTrustedBoundary(jobEvents) && !isTrustedCheckout(step)) {
      violations.push({ file: '', job: jobName, message: 'write-capable trusted job checks out an untrusted ref' });
    }
  }

  if (jobEvents.has('workflow_run') && !hasValidationEntryPoint(jobSteps)) {
    violations.push({ file: '', job: jobName, message: 'workflow_run publisher has no recognised validation entry point' });
  }
  return violations;
}

function validateWorkflowDocument(workflow: WorkflowDocument, file = '<workflow>'): WorkflowTrustViolation[] {
  const events = new Set(workflow.on);
  const violations: WorkflowTrustViolation[] = [];
  for (const [jobName, job] of Object.entries(workflow.jobs)) {
    for (const violation of inspectJob({ workflow, jobName, job, events })) {
      violations.push({ ...violation, file });
    }
  }
  return violations;
}

function parseWorkflowDocument(source: string, file = '<workflow>'): WorkflowDocument {
  const document = parseDocument(source, { stringKeys: true });
  if (document.errors.length > 0) {
    throw new Error(`${file}: ${document.errors.map((error) => error.message).join('; ')}`);
  }
  return workflowSchema.parse(document.toJS());
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
