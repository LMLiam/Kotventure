#!/usr/bin/env bash

# Restore coverage, metrics, and module JARs from a successful base CI run.

set -euo pipefail
shopt -s globstar nullglob

die() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

warn() {
    printf 'Warning: %s\n' "$*" >&2
}

command -v git >/dev/null 2>&1 || die 'git is required but was not found in PATH'
command -v gh >/dev/null 2>&1 || die 'GitHub CLI is required but was not found in PATH'
[[ -n ${BASE_SHA:-} ]] || die 'BASE_SHA is not set'

repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || die 'not inside a Git repository'
cd -- "$repo_root"

readonly COVERAGE_FILE='base-coverage/report.xml'
readonly METRICS_FILE='base-metrics/ci-metrics.json'
readonly JARS_DIR='base-libs'

need_coverage=0
need_metrics=0
need_jars=0

[[ -f $COVERAGE_FILE ]] || need_coverage=1
[[ -f $METRICS_FILE ]] || need_metrics=1

jars=("$JARS_DIR"/kotventure-*.jar)
(( ${#jars[@]} > 0 )) || need_jars=1

if (( ! need_coverage && ! need_metrics && ! need_jars )); then
    printf 'Base coverage, metrics, and module JARs restored from cache\n'
    exit 0
fi

run_list_args=(
    run list
    --workflow ci.yml
    --commit "$BASE_SHA"
    --status success
    --limit 1
    --json databaseId
    --jq '.[0].databaseId // empty'
)

run_id=$(gh "${run_list_args[@]}")

[[ -n $run_id ]] || die "no successful CI run found for base commit $BASE_SHA"

printf 'Using base CI run %s for %s\n' "$run_id" "$BASE_SHA"

download_root=$(mktemp -d)

cleanup() {
    rm -rf -- "$download_root"
}

trap cleanup EXIT

restore_file_artifact() {
    local artifact=$1
    local filename=$2
    local destination=$3
    local download_dir="$download_root/$artifact"

    mkdir -p -- "$download_dir"

    if ! gh run download "$run_id" --name "$artifact" --dir "$download_dir"; then
        warn "could not download artifact '$artifact'"
        return 1
    fi

    local matches=("$download_dir"/**/"$filename")

    if (( ${#matches[@]} == 0 )); then
        warn "artifact '$artifact' does not contain $filename"
        return 1
    fi

    if (( ${#matches[@]} > 1 )); then
        warn "artifact '$artifact' contains multiple files named $filename"
        return 1
    fi

    mkdir -p -- "$(dirname -- "$destination")"
    cp -- "${matches[0]}" "$destination"
}

restore_jars() {
    local artifact download_dir destination
    local artifact_names=(
        module-jars
        gradle-build-artifacts
    )

    for artifact in "${artifact_names[@]}"; do
        download_dir="$download_root/$artifact"
        mkdir -p -- "$download_dir"

        if ! gh run download "$run_id" --name "$artifact" --dir "$download_dir"; then
            continue
        fi

        local jars=("$download_dir"/**/kotventure-*.jar)

        (( ${#jars[@]} > 0 )) || continue

        mkdir -p -- "$JARS_DIR"

        for jar in "${jars[@]}"; do
            destination="$JARS_DIR/${jar##*/}"

            if [[ -e $destination ]]; then
                warn "not overwriting existing JAR: $destination"
                continue
            fi

            cp -- "$jar" "$destination"
        done

        return 0
    done

    warn 'could not restore module JARs from any supported artifact'
    return 1
}

if (( need_coverage )); then
    restore_file_artfact coverage-report report.xml "$COVERAGE_FILE" || true
fi

if (( need_metrics )); then
    restore_file_artfact ci-metrics ci-metrics.json "$METRICS_FILE" || true
fi

if (( need_jars )); then
    restore_jars || true
fi
