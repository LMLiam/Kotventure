#!/usr/bin/env bash

set -euo pipefail

repo_root=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
cd -- "$repo_root"

readonly METRICS_FILE='ci-metrics.json'

tests=0
skipped=0

# Test results land in `*/build/test-results/test/` after the Aggregate job downloads the
# per-shard `gradle-test-results-*` artifacts. `find` tolerates the `modules/` prefix that
# upload-artifact may or may not preserve in the stored paths.
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

# Each build shard writes gradle-duration-<shard>.txt and the Aggregate coverage build writes
# gradle-duration.txt (both measured by the gradle-job action). The reported duration is the
# sequential build time on the critical path: the longest shard build plus the Aggregate
# coverage build, which can only start after every shard completes. When only one source
# exists (single-invocation builds), that value is used; when neither is valid, the field
# stays null.
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
