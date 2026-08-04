#!/bin/sh
set -eu
exec java -jar /app/credit-simulator.jar "$@"
