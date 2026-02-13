#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_DIR}"

TARGET="${1:-all}"

run_student1() {
  echo "Running Student 1 tests..."
  npm run test -- --watch=false \
    --include src/app/pages/admin/admin-create-driver/admin-create-driver.spec.ts \
    --include src/app/api/admin/admin-create-driver.http-data-source.spec.ts
}

run_student2() {
  echo "Running Student 2 tests..."
  npm run test -- --watch=false \
    --include src/app/pages/user/ride-rate/ride-rate.spec.ts \
    --include src/app/api/user/ride-rating.http.datasource.spec.ts
}

case "${TARGET}" in
  student1)
    run_student1
    ;;
  student2)
    run_student2
    ;;
  all)
    run_student1
    run_student2
    ;;
  *)
    echo "Usage: scripts/run-student-tests.sh [student1|student2|all]"
    exit 1
    ;;
esac
