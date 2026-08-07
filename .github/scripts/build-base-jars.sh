#!/usr/bin/env bash

# Build module JARs from the base commit for compatibility checks.
#
# Required environment:
#   BASE_SHA  Commit, branch, tag, or ref to build from origin.
#
# The resulting JARs are written to base-libs/

set -euo pipefail
shopt -s nullglob

die() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

command -v git >/dev/null 2>&1 || die 'git is required but was not found in PATH'
[[ -n ${BASE_SHA:-} ]] || die 'BASE_SHA is not set'

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || die 'not inside a Git repository'

readonly WORKTREE_DIR="$repo_root/base-src"
readonly OUT_DIR="$repo_root/base-libs"

cd -- "$repo_root"

worktree_added=false

cleanup() {
    $worktree_added || return

    if ! git worktree remove --force "$WORKTREE_DIR" >/dev/null 2>&1; then
        rm -rf -- "$WORKTREE_DIR"
        git worktree prune
    fi
}

trap cleanup EXIT

if [[ -e $WORKTREE_DIR ]]; then
    printf 'Removing stale worktree: %s\n' "$WORKTREE_DIR"
    git worktree remove --force "$WORKTREE_DIR" >/dev/null 2>&1 || rm -rf -- "$WORKTREE_DIR"
    git worktree prune
fi

printf 'Fetching base ref: %s\n' "$BASE_SHA"
git fetch --no-tags --depth=1 origin "$BASE_SHA"

base_commit=$(git rev-parse FETCH_HEAD)

printf 'Creating detached worktree at %s\n' "$base_commit"
git worktree add --detach "$WORKTREE_DIR" "$base_commit"
worktree_added=true

[[ -x $WORKTREE_DIR/gradlew ]] || die 'Gradle wrapper is missing or not executable'

module_dirs=()
tasks=()

for module_dir in "$WORKTREE_DIR"/modules/*/; do
    [[ -f ${module_dir}build.gradle || -f ${module_dir}build.gradle.kts ]] || continue

    module=${module_dir%/}
    module=${module##*/}

    [[ $module == bom ]] && continue

    module_dirs+=("${module_dir%/}")
    tasks+=(":$module:jar")
done

(( ${#tasks[@]} > 0 )) || die 'no module JAR tasks were found'

printf 'Building %d module JAR(s):\n' "${#tasks[@]}"
printf '    %s\n' "${tasks[@]}"

(
    cd -- "$WORKTREE_DIR"
    ./gradlew "${tasks[@]}"
)

rm -rf -- "$OUT_DIR"
mkdir -p -- "$OUT_DIR"

jars=()

for module_dir in "${module_dirs[@]}"; do
    for jar in "$module_dir"/build/libs/*.jar; do
        destination="$OUT_DIR/${jar##*/}"

        [[ ! -e $destination ]] || die "duplicate JAR name: ${jar##*/}"

        cp -- "$jar" "$destination"
        jars+=("$destination")
    done
done

(( ${#jars[@]} > 0 )) || die 'the build produced no module JARs'

printf 'Collected %d JAR(s) in %s:\n' "${#jars[@]}" "${OUT_DIR#"$repo_root/"}"
printf '    %s\n' "${jars[@]#"$repo_root/"}"
