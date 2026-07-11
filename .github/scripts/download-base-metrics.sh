#!/usr/bin/env bash
set -euo pipefail

need_coverage=false
need_jars=false
need_metrics=false
[[ -f base-coverage/report.xml ]] || need_coverage=true
[[ -f base-metrics/ci-metrics.json ]] || need_metrics=true
if ! find base-libs -name 'kotventure-*.jar' 2>/dev/null | grep -q .; then
  need_jars=true
fi
if [[ "$need_coverage" != "true" && "$need_jars" != "true" && "$need_metrics" != "true" ]]; then
  echo "Base coverage, jars, and metrics present from cache"
  exit 0
fi

run_id=$(gh run list \
  --workflow ci.yml \
  --commit "$BASE_SHA" \
  --status success \
  --limit 5 \
  --json databaseId \
  --jq '.[0].databaseId // empty')
if [[ -z "$run_id" ]]; then
  echo "No successful CI run for base $BASE_SHA"
  exit 1
fi
echo "Using base CI run $run_id for $BASE_SHA"

if [[ "$need_coverage" == "true" ]]; then
  mkdir -p base-coverage base-coverage-dl
  gh run download "$run_id" -n coverage-report -D base-coverage-dl || true
  report=$(find base-coverage-dl -name report.xml 2>/dev/null | head -1 || true)
  if [[ -n "${report:-}" ]]; then
    cp "$report" base-coverage/report.xml
  fi
fi

if [[ "$need_metrics" == "true" ]]; then
  mkdir -p base-metrics base-metrics-dl
  gh run download "$run_id" -n ci-metrics -D base-metrics-dl || true
  metrics=$(find base-metrics-dl -name ci-metrics.json 2>/dev/null | head -1 || true)
  if [[ -n "${metrics:-}" ]]; then
    cp "$metrics" base-metrics/ci-metrics.json
  fi
fi

if [[ "$need_jars" == "true" ]]; then
  mkdir -p base-libs base-libs-dl
  gh run download "$run_id" -n module-jars -D base-libs-dl || \
    gh run download "$run_id" -n gradle-build-artifacts -D base-libs-dl || true
  find base-libs-dl -name 'kotventure-*.jar' -exec cp {} base-libs/ \; 2>/dev/null || true
fi
