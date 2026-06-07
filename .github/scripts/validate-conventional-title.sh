#!/usr/bin/env bash
set -euo pipefail

readonly pattern='^[a-z]+\([a-z0-9][a-z0-9-]*\): [^[:space:]].*$'
readonly expected='verb(area): something'

validate_title() {
  local title="$1"

  if [[ "$title" =~ $pattern ]]; then
    return 0
  fi

  printf 'Invalid title: %s\n' "$title" >&2
  printf 'Expected format: %s\n' "$expected" >&2
  return 1
}

if [[ "${1:-}" == "--stdin" ]]; then
  status=0
  while IFS= read -r title; do
    [[ -z "$title" ]] && continue
    validate_title "$title" || status=1
  done
  exit "$status"
fi

if [[ "$#" -ne 1 ]]; then
  printf 'Usage: %s TITLE\n' "$0" >&2
  printf '       %s --stdin < titles.txt\n' "$0" >&2
  exit 2
fi

validate_title "$1"
