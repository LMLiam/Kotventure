#!/usr/bin/env bash
set -euo pipefail

pattern='^([a-z]+ )*(class|interface|object) '
status=0
while IFS= read -r file; do
  count=$(grep -cE "$pattern" "$file" || true)
  if (( count > 1 )); then
    echo "::error file=${file}::${count} top-level type declarations — the limit is one class/interface/object per file (AGENTS.md §5)"
    grep -nE "$pattern" "$file" | sed "s|^|${file}:|"
    status=1
  fi
done < <(find modules -path '*/src/main/kotlin/*' -name '*.kt')
exit "$status"
