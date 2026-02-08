#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Student 2 Backend Tests (2.7 - Finish Ride) ==="
./mvnw -Dtest=Student2FinishRideSuite test

echo
echo "=== All Backend Tests ==="
./mvnw -Dtest=AllBackendTestsSuite test
