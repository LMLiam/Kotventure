#!/usr/bin/env bash
set -euo pipefail

pins_file="${1:-gradle/vanilla-conformance.gradle}"
if [[ ! -f "$pins_file" ]]; then
  echo "Missing fixture pins file: $pins_file" >&2
  exit 1
fi

version="$(sed -n "s/^final String targetMinecraftVersion = '\\([^']*\\)'$/\\1/p" "$pins_file")"
sha1="$(sed -n "s/^final String serverBundleSha1 = '\\([^']*\\)'$/\\1/p" "$pins_file")"

if [[ -z "$version" || -z "$sha1" ]]; then
  echo "Could not parse targetMinecraftVersion / serverBundleSha1 from $pins_file" >&2
  exit 1
fi
if [[ ! "$sha1" =~ ^[0-9a-f]{40}$ ]]; then
  echo "serverBundleSha1 is not a 40-char hex SHA-1: $sha1" >&2
  exit 1
fi

{
  echo "version=$version"
  echo "sha1=$sha1"
  echo "key=vanilla-mc-${version}-${sha1}"
}
