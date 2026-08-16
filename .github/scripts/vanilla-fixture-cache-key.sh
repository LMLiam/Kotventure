#!/usr/bin/env bash

set -euo pipefail

readonly DEFAULT_PINS_FILE='gradle/vanilla-conformance.gradle'
readonly VERSION_PATTERN="^final String targetMinecraftVersion = '([^']+)'$"
readonly SHA1_PATTERN="^final String serverBundleSha1 = '([0-9a-f]{40})'$"
readonly SHA256_PATTERN="^final String serverBundleSha256 = '([0-9a-f]{64})'$"

if (( $# > 1 )); then
    printf 'Usage: %s [pins-file]\n' "${0##*/}" >&2
    exit 2
fi

pins_file=${1:-$DEFAULT_PINS_FILE}

if [[ ! -f $pins_file ]]; then
    printf 'Missing fixture pins file: %s\n' "$pins_file"
    exit 1
fi

version=
sha1=
sha256=

while IFS= read -r line || [[ -n $line ]]; do
    if [[ $line =~ $VERSION_PATTERN ]]; then
        [[ -z $version ]] || {
            printf 'Multiple targetMinecraftVersion declarations in %s\n' "$pins_file" >&2
            exit 1
        }

        version=${BASH_REMATCH[1]}
    elif [[ $line =~ $SHA1_PATTERN ]]; then
        [[ -z $sha1 ]] || {
            printf 'Multiple serverBundleSha1 declarations in %s\n' "$pins_file" >&2
            exit 1
        }

        sha1=${BASH_REMATCH[1]}
    elif [[ $line =~ $SHA256_PATTERN ]]; then
        [[ -z $sha256 ]] || {
            printf 'Multiple serverBundleSha256 declarations in %s\n' "$pins_file" >&2
            exit 1
        }

        sha256=${BASH_REMATCH[1]}
    fi
done < "$pins_file"

if [[ -z $version || -z $sha1 || -z $sha256 ]]; then
    printf 'Could not parse targetMinecraftVersion, serverBundleSha1, and serverBundleSha256 from %s\n' "$pins_file" >&2
    exit 1
fi

printf 'version=%s\n' "$version"
printf 'sha1=%s\n' "$sha1"
printf 'sha256=%s\n' "$sha256"
printf 'key=vanilla-mc-%s-%s-%s\n' "$version" "$sha1" "$sha256"
