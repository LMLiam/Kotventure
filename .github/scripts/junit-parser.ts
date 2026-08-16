import { DOMParser } from '@xmldom/xmldom';
import type { Document, Element, Node } from '@xmldom/xmldom';
import { isSafeRepositoryPath } from './shared/path-classification.js';
import {
  MAX_JUNIT_ANNOTATIONS,
  MAX_JUNIT_TEST_CASES,
  MAX_JUNIT_TEXT,
} from './junit-contract.js';

const ELEMENT_NODE = 1;
const TEXT_NODE = 3;
const MAX_XML_DEPTH = 100;
const MAX_XML_NODES = 250_000;
const MAX_XML_TEXT_BYTES = 2 * 1024 * 1024;

export type JunitCaseOutcome = 'passed' | 'failed' | 'error' | 'skipped';

export interface JunitAnnotation {
  path: string;
  line: number;
  level: 'failure' | 'notice';
  title: string;
  message: string;
}

export interface JunitCase {
  name: string;
  className: string;
  duration: number;
  outcome: JunitCaseOutcome;
  message: string | null;
  annotation: JunitAnnotation | null;
}

export interface JunitReport {
  fileName: string;
  cases: JunitCase[];
  passed: number;
  failed: number;
  errors: number;
  skipped: number;
  annotations: JunitAnnotation[];
}

function reject(message: string): never {
  throw new Error(`JUnit report ${message}`);
}

function isElement(node: Node): node is Element {
  return node.nodeType === ELEMENT_NODE;
}

function directChildren(parent: Element, tagName: string): Element[] {
  const children: Element[] = [];
  for (const node of parent.childNodes) {
    if (isElement(node) && node.tagName === tagName) children.push(node);
  }
  return children;
}

function textContent(element: Element): string {
  const value = element.textContent ?? '';
  if (value.length > MAX_JUNIT_TEXT) reject('text field is too long');
  return value;
}

function requiredAttribute(element: Element, name: string): string {
  const value = element.getAttribute(name);
  if (value == null || value.length < 1 || value.length > MAX_JUNIT_TEXT) {
    reject(`${name} is invalid`);
  }
  return value;
}

function optionalTextAttribute(element: Element, name: string): string | null {
  const value = element.getAttribute(name);
  if (value == null) return null;
  if (value.length > MAX_JUNIT_TEXT) reject(`${name} is invalid`);
  return value;
}

function durationAttribute(element: Element): number {
  const value = element.getAttribute('time');
  if (value == null) return 0;
  const duration = Number(value);
  if (!Number.isFinite(duration) || duration < 0 || duration > 1_000_000) reject('test duration is invalid');
  return duration;
}

function lineAttribute(element: Element): number | null {
  const value = element.getAttribute('line');
  if (value == null) return null;
  const line = Number(value);
  if (!Number.isSafeInteger(line) || line < 1 || line > 1_000_000) reject('test line is invalid');
  return line;
}

function validateDocumentTree(root: Node): void {
  const pending: Array<{ node: Node; depth: number }> = [{ node: root, depth: 0 }];
  let nodes = 0;
  let textBytes = 0;
  while (pending.length > 0) {
    const entry = pending.pop();
    if (entry == null) continue;
    const { node, depth } = entry;
    nodes += 1;
    if (nodes > MAX_XML_NODES || depth > MAX_XML_DEPTH) reject('structure is too complex');
    if (node.nodeType === TEXT_NODE) {
      const text = node.nodeValue ?? '';
      textBytes += Buffer.byteLength(text, 'utf8');
      if (textBytes > MAX_XML_TEXT_BYTES) reject('text content is too large');
    }
    for (const child of node.childNodes) pending.push({ node: child, depth: depth + 1 });
  }
}

interface StackTracePosition {
  path: string;
  line: number;
}

const STACK_FRAME_PATTERN = /^\s*at\s+([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\.[^(]+\(([^():]+):(\d+)\)\s*$/gm;

function stackTracePosition({ message, className, fileName }: {
  message: string | null;
  className: string;
  fileName: string;
}): StackTracePosition | null {
  if (message == null) return null;
  const report = /^((?:modules\/[A-Za-z0-9_-]+))\/build\/test-results\/(test|vanillaConformanceTest)\//.exec(fileName);
  if (report == null) return null;
  const matches = [...message.matchAll(STACK_FRAME_PATTERN)];
  const match = [...matches].reverse().find((candidate) => {
    const owner = candidate[1];
    return owner === className || owner?.startsWith(`${className}$`) || owner === `${className}Kt`;
  });
  if (match == null) return null;
  const frameFile = match[2];
  const line = Number(match[3]);
  if (frameFile == null || !/^[A-Za-z0-9_.-]+\.(?:java|kt)$/.test(frameFile)
    || !Number.isSafeInteger(line) || line < 1 || line > 1_000_000) return null;
  const modulePath = report[1];
  const sourceSet = report[2];
  if (modulePath == null || sourceSet == null) return null;
  const language = frameFile.endsWith('.java') ? 'java' : 'kotlin';
  const packageName = className.includes('.') ? className.slice(0, className.lastIndexOf('.')) : '';
  const packagePath = packageName.replaceAll('.', '/');
  const path = [modulePath, 'src', sourceSet, language, packagePath, frameFile].filter((part) => part.length > 0).join('/');
  if (!isSafeRepositoryPath(path)) return null;
  return { path, line };
}

function parseDocument(xml: Buffer): Document {
  const text = xml.toString('utf8');
  if (/<!DOCTYPE/i.test(text)) reject('document type declarations are not allowed');
  let parserError: string | null = null;
  let document: Document;
  try {
    document = new DOMParser({
      onError: (level, message) => {
        if (level === 'error' || level === 'fatalError') parserError = message;
      },
    }).parseFromString(text, 'text/xml');
  } catch (error) {
    reject(`XML is malformed: ${error instanceof Error ? error.message : String(error)}`);
  }
  if (parserError != null) reject(`XML is malformed: ${parserError}`);
  const root = document.documentElement;
  if (root == null || (root.tagName !== 'testsuite' && root.tagName !== 'testsuites')) reject('root element is invalid');
  validateDocumentTree(document);
  return document;
}

function suitesFromRoot(root: Element): Element[] {
  if (root.tagName === 'testsuite') return [root];
  const suites = directChildren(root, 'testsuite');
  if (suites.length < 1) reject('contains no test suites');
  return suites;
}

function suiteElements(root: Element): Element[] {
  const result: Element[] = [];
  const pending = [...suitesFromRoot(root)];
  while (pending.length > 0) {
    const suite = pending.shift();
    if (suite == null) continue;
    result.push(suite);
    pending.push(...directChildren(suite, 'testsuite'));
  }
  return result;
}

function annotationFor({
  element,
  issue,
  fileName,
  outcome,
  message,
  name,
  className,
}: {
  element: Element;
  issue: Element | null;
  fileName: string;
  outcome: JunitCaseOutcome;
  message: string | null;
  name: string;
  className: string;
}): JunitAnnotation | null {
  const file = optionalTextAttribute(element, 'file') ?? (issue == null ? null : optionalTextAttribute(issue, 'file'));
  const line = lineAttribute(element) ?? (issue == null ? null : lineAttribute(issue));
  if (file != null && !isSafeRepositoryPath(file)) reject('test file path is unsafe');
  const fallback = stackTracePosition({
    message: issue == null ? message : textContent(issue),
    className,
    fileName,
  });
  const path = file ?? fallback?.path;
  const annotationLine = line ?? fallback?.line;
  if (path == null || annotationLine == null) return null;
  if (!isSafeRepositoryPath(path)) reject('test file path is unsafe');
  return {
    path,
    line: annotationLine,
    level: outcome === 'skipped' ? 'notice' : 'failure',
    title: `${outcome}: ${className}.${name}`.slice(0, MAX_JUNIT_TEXT),
    message: (message ?? `${outcome} test case`).slice(0, MAX_JUNIT_TEXT),
  };
}

function parseCase(element: Element, fileName: string): JunitCase {
  const name = requiredAttribute(element, 'name');
  const className = optionalTextAttribute(element, 'classname') ?? 'unknown';
  if (className.length < 1) reject('classname is invalid');
  const failures = directChildren(element, 'failure');
  const errors = directChildren(element, 'error');
  const skipped = directChildren(element, 'skipped');
  const issue = failures[0] ?? errors[0] ?? null;
  const outcomeCount = failures.length + errors.length + skipped.length;
  if (outcomeCount > 1) reject('test case has multiple outcomes');
  let outcome: JunitCaseOutcome = 'passed';
  let message: string | null = null;
  if (failures.length === 1) {
    const failure = failures[0];
    if (failure == null) reject('failure element is missing');
    outcome = 'failed';
    message = optionalTextAttribute(failure, 'message') ?? textContent(failure);
  } else if (errors.length === 1) {
    const error = errors[0];
    if (error == null) reject('error element is missing');
    outcome = 'error';
    message = optionalTextAttribute(error, 'message') ?? textContent(error);
  } else if (skipped.length === 1) {
    const skippedElement = skipped[0];
    if (skippedElement == null) reject('skipped element is missing');
    outcome = 'skipped';
    message = optionalTextAttribute(skippedElement, 'message') ?? textContent(skippedElement);
  }
  return {
    name,
    className,
    duration: durationAttribute(element),
    outcome,
    message,
    annotation: annotationFor({
      element,
      issue,
      fileName,
      outcome,
      message,
      name,
      className,
    }),
  };
}

export function parseJunitReport(fileName: string, xml: Buffer): JunitReport {
  if (!Buffer.isBuffer(xml) || xml.length < 1) reject('document is empty');
  const document = parseDocument(xml);
  const root = document.documentElement;
  if (root == null) reject('root element is missing');
  const cases: JunitCase[] = [];
  for (const suite of suiteElements(root)) {
    for (const testcase of directChildren(suite, 'testcase')) {
      cases.push(parseCase(testcase, fileName));
      if (cases.length > MAX_JUNIT_TEST_CASES) reject('contains too many test cases');
    }
  }
  if (cases.length < 1) reject('contains no test cases');
  const annotations = cases.flatMap((testcase) => testcase.annotation == null ? [] : [testcase.annotation]);
  return {
    fileName,
    cases,
    passed: cases.filter((testcase) => testcase.outcome === 'passed').length,
    failed: cases.filter((testcase) => testcase.outcome === 'failed').length,
    errors: cases.filter((testcase) => testcase.outcome === 'error').length,
    skipped: cases.filter((testcase) => testcase.outcome === 'skipped').length,
    annotations,
  };
}

export interface JunitAggregate {
  files: number;
  cases: number;
  passed: number;
  failed: number;
  errors: number;
  skipped: number;
  annotations: JunitAnnotation[];
}

export function aggregateJunitReports(reports: JunitReport[]): JunitAggregate {
  const aggregate: JunitAggregate = {
    files: reports.length,
    cases: 0,
    passed: 0,
    failed: 0,
    errors: 0,
    skipped: 0,
    annotations: [],
  };
  for (const report of reports) {
    aggregate.cases += report.cases.length;
    aggregate.passed += report.passed;
    aggregate.failed += report.failed;
    aggregate.errors += report.errors;
    aggregate.skipped += report.skipped;
    aggregate.annotations.push(...report.annotations);
  }
  return aggregate;
}

export function junitSummary(label: string, aggregate: JunitAggregate, omittedAnnotations: number): string {
  const omitted = omittedAnnotations > 0 ? `\n\n${omittedAnnotations} annotations were omitted because GitHub limits each update to ${MAX_JUNIT_ANNOTATIONS}.` : '';
  return `### ${label}\n\n| Result | Count |\n| --- | ---: |\n| Test files | ${aggregate.files} |\n| Test cases | ${aggregate.cases} |\n| Passed | ${aggregate.passed} |\n| Failed | ${aggregate.failed} |\n| Errors | ${aggregate.errors} |\n| Skipped | ${aggregate.skipped} |${omitted}`;
}
