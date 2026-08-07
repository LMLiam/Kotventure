#!/usr/bin/env bash

# Normalize zero-based Qodana SARIF region coordinates for GitHub code scanning.

set -euo pipefail

readonly DEFAULT_SARIF_FILE="${RUNNER_TEMP:-/tmp}/qodana/results/qodana.sarif.json"

if (( $# > 1 )); then
    printf 'Usage: %s [sarif-file]\n' "${0##*/}" >&2
    exit 2
fi

sarif_file=${1:-$DEFAULT_SARIF_FILE}

if [[ ! -f $sarif_file ]]; then
    printf 'Qodana SARIF not found at %s; skipping normalization.\n' "$sarif_file"
    exit 0
fi

command -v jq >/dev/null 2>&1 || {
    printf 'Error: jq is required to normalize Qodana SARIF.\n' >&2
    exit 1
}

tmp_file=$(mktemp "${sarif_file}.tmp.XXXXXX")

cleanup() {
    rm -f -- "$tmp_file"
}

trap cleanup EXIT

jq '
    def normalize_region:
        if type != "object" then
            .
        else
            (if (.startLine? | type) == "number" and .startLine < 1 then .startLine = 1 else . end)
            | (if (.startColumn? | type) == "number" and .startColumn < 1 then .startColumn = 1 else . end)
        end;

    walk(
        if type == "object" and (.region? | type) == "object" then
            .region |= normalize_region
        else
            .
        end
    )
' "$sarif_file" > "$tmp_file"

mv -- "$tmp_file" "$sarif_file"
