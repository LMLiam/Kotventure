#!/usr/bin/env bash
set -euo pipefail

git fetch --depth=1 origin "$BASE_SHA"
rm -rf base-src
git worktree add --detach base-src "$BASE_SHA"

tasks=()
for dir in base-src/modules/*/; do
  name="${dir#base-src/modules/}"
  name="${name%/}"
  [[ "$name" == "bom" ]] && continue
  if [[ -f "${dir}build.gradle" || -f "${dir}build.gradle.kts" ]]; then
    tasks+=(":${name}:jar")
  fi
done
if [[ ${#tasks[@]} -eq 0 ]]; then
  echo "No module jar tasks on base"
  exit 1
fi

echo "Building base jars: ${tasks[*]}"
(cd base-src && ./gradlew "${tasks[@]}")
mkdir -p base-libs
cp base-src/build/libs/kotventure-*.jar base-libs/ 2>/dev/null || true
