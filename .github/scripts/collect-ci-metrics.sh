#!/usr/bin/env bash

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
max_duration=0
found_duration=false

# Check per-shard duration files (gradle-duration-*.txt) and aggregate duration (gradle-duration.txt).
# Duration represents the wall-clock indicative time for the longest shard, which is the
# critical-path build time in the parallel CI (max of shards). If only a single file exists,
# it is used directly.
for dfile in gradle-duration-*.txt "$DURATION_FILE"; do
    [[ -f $dfile ]] || continue
    read -r val < "$dfile"
    if [[ $val =~ ^[0-9]+$ ]]; then
        found_duration=true
        if (( val > max_duration )); then
            max_duration=$val
        fi
    fi
done

if [[ $found_duration == true ]]; then
    duration=$max_duration
else
    duration=null
fi

printf '{"tests": %d, "skipped": %d, "durationSeconds": %s}\n' "$tests" "$skipped" "$duration" > "$METRICS_FILE"

printf 'Collected CI metrics: '
cat "$METRICS_FILE"
