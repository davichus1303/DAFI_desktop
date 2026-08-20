#!/usr/bin/env bash
# Regenerate codebase maps for dafi-desktop
# Idempotent: overwrites previous maps with fresh generation.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
python3 "$SCRIPT_DIR/.codebase-map/generate-local-map.py"
