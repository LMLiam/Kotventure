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

# Each build shard writes gradle-duration-<shard>.txt (measured by the gradle-job action).
# The reported duration is the maximum shard build duration, i.e. the critical-path build
# time in the parallel CI. When no per-shard files exist (single-invocation build), the
# plain gradle-duration.txt is used instead.
shard_files=(gradle-duration-*.txt)
if [[ -n "${shard_files[0]:-}" && -f "${shard_files[0]}" ]]; then
    max_duration=0
    for dfile in "${shard_files[@]}"; do
        [[ -f $dfile ]] || continue
        read -r val < "$dfile"
        if [[ $val =~ ^[0-9]+$ ]] && (( val > max_duration )); then
            max_duration=$val
        fi
    done
    duration=$max_duration
elif [[ -f gradle-duration.txt ]]; then
    read -r val < gradle-duration.txt
    [[ $val =~ ^[0-9]+$ ]] && duration=$val
fi

printf '{"tests": %d, "skipped": %d, "durationSeconds": %s}\n' "$tests" "$skipped" "$duration" > "$METRICS_FILE"

printf 'Collected CI metrics: '
cat "$METRICS_FILE"
