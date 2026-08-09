#!/usr/bin/env bash

set -euo pipefail

if (( $# > 1 )); then
    printf 'Usage: %s [gradle-log]\n' "${0##*/}" >&2
    exit 2
fi

readonly SUMMARY_FILE=${GITHUB_STEP_SUMMARY:?GITHUB_STEP_SUMMARY is not set}
readonly LOG_FILE=${1:-}

java_version='(not on PATH)'
gradle_version='(wrapper missing)'
kotlin_version='(catalog missing)'

if command -v java >/dev/null 2>&1; then
    java_version=$(java -version 2>&1 | awk 'NR == 1 { sub(/\r$/, ""); print; exit }')
fi

if [[ -x ./gradlew ]]; then
    gradle_version=$(./gradlew --version 2>/dev/null | awk '/^Gradle / { print $2; exit }')
    gradle_version=${gradle_version:-unknown}
fi

if [[ -f gradle/libs.versions.toml ]]; then
    kotlin_version=$(awk -F '"' '/^kotlin[[:space:]]*=/ { print $2; exit }' gradle/libs.versions.toml)
    kotlin_version=${kotlin_version:-unknown}
fi

{
    printf '## CI job summary\n\n'
    printf '### Toolchain\n'
    printf '%s\n' "- Java: $java_version"
    printf '%s\n' "- Gradle: $gradle_version"
    printf '%s\n' "- Kotlin: $kotlin_version"

    if [[ -n ${GITHUB_EVENT_NAME:-} ]]; then
        printf '\n### Run\n'
        printf '%s\n' "- Event: \`${GITHUB_EVENT_NAME}\`"

        if [[ -n ${GRADLE_TASKS:-} ]]; then
            printf '%s\n' "- Gradle tasks: \`${GRADLE_TASKS}\`"
        fi
    fi

    if [[ -n $LOG_FILE && -f $LOG_FILE ]]; then
        mapfile -t failed_tasks < <(
            awk '
                /^> Task .+ FAILED$/ {
                    sub(/^> Task /, "")
                    sub(/ FAILED$/, "")
                    if (!seen[$0]++) {
                        print
                    }
                }
            ' "$LOG_FILE"
        )

        if (( ${#failed_tasks[@]} > 0 )); then
            printf '\n### Failed tasks\n'

            for task in "${failed_tasks[@]}"; do
                printf '%s\n' "- \`$task\`"
            done
        fi
    fi
} >> "$SUMMARY_FILE"
