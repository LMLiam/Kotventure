#!/usr/bin/env bash

# Collect CI test and build-duration metrics.

set -euo pipefail
shopt -s nullglob

repo_root=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd -- "$repo_root"

readonly METRICS_FILE='ci-metrics.json'
readonly DURATION_FILE='gradle-duration.txt'

tests=0
skipped=0

for file in modules/*/build/test-results/test/TEST-*.xml; do
    xml=$(<"$file")

    if [[ $xml =~ tests=\"([0-9]+)\" ]]; then
        tests=$((tests + 10#${BASH_REMATCH[1]}))
    fi

    if [[ $xml =~ skipped=\"([0-9]+)\" ]]; then
        skipped=$((skipped + 10#${BASH_REMATCH[1]}))
    fi
done

duration=null

if [[ -f $DURATION_FILE ]]; then
    read -r duration < "$DURATION_FILE"

    [[ $duration =~ ^[0-9]+$ ]] || duration=null
fi

printf '{"tests": %d, "skipped": %d, "durationSeconds": %s}\n' "$tests" "$skipped" "$duration" > "$METRICS_FILE"

printf 'Collected CI metrics: '
cat "$METRICS_FILE"
