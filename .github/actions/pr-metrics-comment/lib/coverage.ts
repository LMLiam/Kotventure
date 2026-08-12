import { DOMParser } from '@xmldom/xmldom';
import type { Element, Node } from '@xmldom/xmldom';
import { sanitizeModule } from './names.js';

const ELEMENT_NODE = 1;
const LINE_COUNTER = 'LINE';

export interface ModuleCounters {
  readonly missed: number;
  readonly covered: number;
}

export interface CoverageData {
  readonly modules: Map<string, ModuleCounters>;
  readonly totalMissed: number;
  readonly totalCovered: number;
  readonly files: Map<string, Map<number, boolean>>;
}

function isElement(node: Node): node is Element {
  return node.nodeType === ELEMENT_NODE;
}

function attrInt(element: Element, name: string): number {
  const value = element.getAttribute(name);
  return value === null ? Number.NaN : parseInt(value, 10);
}

function moduleFromPackage(pkg: string): string {
  const parts = pkg.split('/');
  const kotIdx = parts.indexOf('kotventure');
  if (kotIdx < 0) return sanitizeModule(parts[0] ?? 'unknown');
  const after = parts.slice(kotIdx + 1);
  if (after[0] === 'test' && after[1] === 'snapshot') return 'test-snapshot';
  return sanitizeModule(after[0] ?? 'unknown');
}

function directChildren(parent: Element, tagName: string): Element[] {
  const children: Element[] = [];
  for (const node of parent.childNodes) {
    if (!isElement(node)) continue;
    if (node.tagName === tagName) children.push(node);
  }
  return children;
}

function lineCounter(container: Element): { missed: number; covered: number } | null {
  for (const counter of directChildren(container, 'counter')) {
    if (counter.getAttribute('type') !== LINE_COUNTER) continue;
    return {
      missed: attrInt(counter, 'missed'),
      covered: attrInt(counter, 'covered'),
    };
  }
  return null;
}

function sourcefileLines(packageName: string, packageElement: Element, files: Map<string, Map<number, boolean>>): void {
  for (const sourcefile of packageElement.getElementsByTagName('sourcefile')) {
    const lines = new Map<number, boolean>();
    for (const line of sourcefile.getElementsByTagName('line')) {
      lines.set(
        attrInt(line, 'nr'),
        attrInt(line, 'ci') > 0,
      );
    }
    if (lines.size > 0) {
      files.set(`${packageName}/${sourcefile.getAttribute('name')}`, lines);
    }
  }
}

export function parseCoverage(xml: string): CoverageData {
  const document = new DOMParser().parseFromString(xml, 'text/xml');
  const root = document.documentElement;
  const modules = new Map<string, ModuleCounters>();
  const files = new Map<string, Map<number, boolean>>();

  if (!root) return { modules, totalMissed: 0, totalCovered: 0, files };

  for (const packageElement of root.getElementsByTagName('package')) {
    const packageName = packageElement.getAttribute('name') ?? '';
    sourcefileLines(packageName, packageElement, files);
    const counters = lineCounter(packageElement);
    if (!counters) continue;
    const moduleName = moduleFromPackage(packageName);
    const previous = modules.get(moduleName);
    modules.set(moduleName, {
      missed: (previous?.missed ?? 0) + counters.missed,
      covered: (previous?.covered ?? 0) + counters.covered,
    });
  }

  const total = lineCounter(root);
  let totalMissed = 0;
  let totalCovered = 0;
  if (total) {
    totalMissed = total.missed;
    totalCovered = total.covered;
  } else {
    for (const data of modules.values()) {
      totalMissed += data.missed;
      totalCovered += data.covered;
    }
  }

  return { modules, totalMissed, totalCovered, files };
}
