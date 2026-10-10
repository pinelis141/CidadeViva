#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIR="$(mktemp -d)"
trap 'rm -rf "$DIR"' EXIT
javac -encoding UTF-8 -d "$DIR" \
 "$ROOT/core/src/main/java/br/com/cidadeviva/sim/"*.java \
 "$ROOT/core/src/test/java/br/com/cidadeviva/sim/"*.java
java -cp "$DIR" br.com.cidadeviva.sim.CitySmokeTest
