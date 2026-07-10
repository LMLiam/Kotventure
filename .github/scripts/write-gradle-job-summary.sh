#!/usr/bin/env bash
set -euo pipefail

log_file="${1:-}"

{
  echo "## CI job summary"
  echo
  echo "### Toolchain"
  if command -v java >/dev/null 2>&1; then
    echo "- Java: $(java -version 2>&1 | head -n 1 | tr -d '\r')"
  else
    echo "- Java: (not on PATH)"
  fi

  if [[ -x ./gradlew ]]; then
    gradle_ver="$(./gradlew --version 2>/dev/null | awk '/^Gradle / { print $2; exit }' || true)"
    echo "- Gradle: ${gradle_ver:-unknown}"
  else
    echo "- Gradle: (wrapper missing)"
  fi

  if [[ -f gradle/libs.versions.toml ]]; then
    kotlin_ver="$(awk -F'"' '/^kotlin[[:space:]]*=/ { print $2; exit }' gradle/libs.versions.toml || true)"
    echo "- Kotlin: ${kotlin_ver:-unknown}"
  else
    echo "- Kotlin: (catalog missing)"
  fi

  if [[ -n "${GITHUB_EVENT_NAME:-}" ]]; then
    echo
    echo "### Run"
    echo "- Event: \`${GITHUB_EVENT_NAME}\`"
    if [[ -n "${GRADLE_TASKS:-}" ]]; then
      echo "- Gradle tasks: \`${GRADLE_TASKS}\`"
    fi
  fi

  if [[ -n "${log_file}" && -f "${log_file}" ]] && grep -qE '> Task .+ FAILED' "${log_file}"; then
    echo
    echo "### Failed tasks"
    grep -E '> Task .+ FAILED' "${log_file}" | while IFS= read -r line; do
      task="${line#*> Task }"
      task="${task% FAILED}"
      [[ -n "${task}" ]] || continue
      echo "- \`${task}\`"
    done
  fi
} >> "${GITHUB_STEP_SUMMARY}"
