#!/usr/bin/env bash
# Normalize Qodana SARIF region coordinates so GitHub code scanning accepts them.
# Some Qodana exports use startLine/startColumn of 0; GitHub expects 1-based values ≥ 1.
set -euo pipefail

sarif_file="${1:-${RUNNER_TEMP:-/tmp}/qodana/results/qodana.sarif.json}"
tmp_file="${sarif_file}.tmp"

if [[ ! -f "$sarif_file" ]]; then
  echo "Qodana SARIF not found at $sarif_file; skipping normalization."
  exit 0
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to normalize Qodana SARIF." >&2
  exit 1
fi

cleanup() {
  rm -f "$tmp_file"
}
trap cleanup EXIT

jq '
  def normalize_region:
    if type == "object" then
      (
        if has("startLine") and (.startLine | type == "number") and .startLine < 1 then
          .startLine = 1
        else
          .
        end
      )
      | (
        if has("startColumn") and (.startColumn | type == "number") and .startColumn < 1 then
          .startColumn = 1
        else
          .
        end
      )
    else
      .
    end;

  walk(if type == "object" and has("region") then .region |= normalize_region else . end)
' "$sarif_file" > "$tmp_file"
mv "$tmp_file" "$sarif_file"
