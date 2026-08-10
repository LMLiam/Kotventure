#!/usr/bin/env bash

set -euo pipefail

repo_root=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd -- "$repo_root"

readonly METRICS_FILE='ci-metrics.json'

tests=0
skipped=0

while IFS= read -r file; do
    xml=$(<"$file")

    if [[ $xml =~ tests=\"([0-9]+)\" ]]; then
        tests=$((tests + 10#${BASH_REMATCH[1]}))
    fi

    if [[ $xml =~ skipped=\"([0-9]+)\" ]]; then
        skipped=$((skipped + 10#${BASH_REMATCH[1]}))
    fi
done < <(find . -type f -name 'TEST-*.xml' -path '*/build/test-results/test/*' 2>/dev/null)

duration=null

max_shard_duration=0
aggregate_duration=0
has_shard=false
has_aggregate=false

for dfile in gradle-duration-*.txt; do
    [[ -f $dfile ]] || continue
    read -r val < "$dfile"
    if [[ $val =~ ^[0-9]+$ ]]; then
        has_shard=true
        if (( val > max_shard_duration )); then
            max_shard_duration=$val
        fi
    fi
done

if [[ -f gradle-duration.txt ]]; then
    read -r val < gradle-duration.txt
    if [[ $val =~ ^[0-9]+$ ]]; then
        has_aggregate=true
        aggregate_duration=$val
    fi
fi

if [[ $has_shard == true && $has_aggregate == true ]]; then
    duration=$((max_shard_duration + aggregate_duration))
elif [[ $has_shard == true ]]; then
    duration=$max_shard_duration
elif [[ $has_aggregate == true ]]; then
    duration=$aggregate_duration
fi

printf '{"tests": %d, "skipped": %d, "durationSeconds": %s}\n' "$tests" "$skipped" "$duration" > "$METRICS_FILE"

printf 'Collected CI metrics: '
cat "$METRICS_FILE"
