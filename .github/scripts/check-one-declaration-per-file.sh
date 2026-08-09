#!/usr/bin/env bash

set -euo pipefail

readonly declaration_pattern="^([[:alpha:]_][[:alnum:]_]*[[:space:]]+)*(class|interface|object)[[:space:]]+([[:alpha:]_][[:alnum:]_]*|\`[^\`]+\`)"

readonly -a source_paths=(
    ':(glob)modules/**/src/main/kotlin/**/*.kt'
)

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || {
    printf 'Error: not inside a Git repository\n' >&2
    exit 2
}

cd -- "$repo_root"

status=0

while IFS= read -r -d '' file; do
    mapfile -t declarations < <(grep -nE "$declaration_pattern" "$file" || true)

    (( ${#declarations[@]} <= 1 )) && continue

    printf '::error file=%s::%d top-level type declarations; expected at most one\n' "$file" "${#declarations[@]}"

    for declaration in "${declarations[@]}"; do
        printf '%s:%s\n' "$file" "$declaration"
    done

    status=1
done < <(git ls-files -z -- "${source_paths[@]}")

exit "$status"
