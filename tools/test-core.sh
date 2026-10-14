#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIR="$(mktemp -d)"
trap 'rm -rf "$DIR"' EXIT
javac -encoding UTF-8 --release 17 -d "$DIR" \
 "$ROOT/core/src/main/java/br/com/cidadeviva/sim/"*.java \
 "$ROOT/core/src/main/java/br/com/cidadeviva/input/"*.java \
 "$ROOT/core/src/main/java/br/com/cidadeviva/storage/"*.java \
 "$ROOT/core/src/test/java/br/com/cidadeviva/sim/"*.java \
 "$ROOT/core/src/test/java/br/com/cidadeviva/input/"*.java \
 "$ROOT/core/src/test/java/br/com/cidadeviva/storage/"*.java
java -cp "$DIR" br.com.cidadeviva.sim.CitySmokeTest
java -cp "$DIR" br.com.cidadeviva.input.ControlsMathTest
java -cp "$DIR" br.com.cidadeviva.input.BuildStrokeTest
java -cp "$DIR" br.com.cidadeviva.sim.CityAreaTest
java -cp "$DIR" br.com.cidadeviva.sim.CityRoadAccessTest
java -cp "$DIR" br.com.cidadeviva.storage.CitySaveStoreTest

java -cp "$DIR" br.com.cidadeviva.sim.CityTerrainTest

java -cp "$DIR" br.com.cidadeviva.storage.CitySlotsTest

java -cp "$DIR" br.com.cidadeviva.sim.CityEconomyTest
java -cp "$DIR" br.com.cidadeviva.sim.CityTaxPolicyTest
java -cp "$DIR" br.com.cidadeviva.sim.CityLifeTest

java -cp "$DIR" br.com.cidadeviva.sim.CityProgressTest
java -cp "$DIR" br.com.cidadeviva.sim.CityBalanceTest
java -cp "$DIR" br.com.cidadeviva.sim.CityMobilityTest

java -cp "$DIR" br.com.cidadeviva.sim.CityNeighborhoodsTest
java -cp "$DIR" br.com.cidadeviva.sim.CityServicesTest
java -cp "$DIR" br.com.cidadeviva.sim.CityUtilitiesTest

java -cp "$DIR" br.com.cidadeviva.sim.CityRecoveryTest

java -cp "$DIR" br.com.cidadeviva.sim.CityLoanTest

java -cp "$DIR" br.com.cidadeviva.sim.CityUrbanTest

java -cp "$DIR" br.com.cidadeviva.sim.CityUrbanAuditTest
