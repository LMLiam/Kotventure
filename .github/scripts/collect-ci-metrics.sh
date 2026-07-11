#!/usr/bin/env bash
set -euo pipefail

sum_attr() {
  local attr="$1" total=0 value file
  for file in modules/*/build/test-results/test/TEST-*.xml; do
    [[ -f "$file" ]] || continue
    value=$(grep -o "${attr}=\"[0-9]*\"" "$file" | head -1 | grep -o '[0-9]*' || true)
    total=$((total + ${value:-0}))
  done
  echo "$total"
}

tests=$(sum_attr tests)
skipped=$(sum_attr skipped)
duration=null
if [[ -f gradle-duration.txt ]]; then
  duration=$(grep -o '^[0-9]*' gradle-duration.txt || echo null)
  [[ -n "$duration" ]] || duration=null
fi

printf '{"tests": %s, "skipped": %s, "durationSeconds": %s}\n' \
  "$tests" "$skipped" "$duration" > ci-metrics.json
echo "Collected CI metrics: $(cat ci-metrics.json)"
