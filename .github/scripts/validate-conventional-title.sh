#!/usr/bin/env bash

set -euo pipefail

readonly TITLE_PATTERN='^[a-z]+\([a-z0-9]+(-[a-z0-9]+)*\): [^[:space:]].*$'
readonly EXPECTED_FORMAT='verb(area): something'

usage() {
    printf 'Usage: %s TITLE\n' "${0##*/}" >&2
    printf '       %s --stdin < titles.txt\n' "${0##*/}"
}

validate_title() {
    local title=$1

    [[ $title =~ $TITLE_PATTERN ]] && return 0

    printf 'Invalid title: %s\n' "$title" >&2
    printf 'Expected format: %s\n' "$EXPECTED_FORMAT" >&2
    return 1
}

if (( $# != 1 )); then
    usage
    exit 2
fi

if [[ $1 != --stdin ]]; then
    validate_title "$1"
    exit
fi

status=0

while IFS= read -r title || [[ -n $title ]]; do
    [[ -z $title ]] && continue
    validate_title "$title" || status=1
done

exit "$status"
