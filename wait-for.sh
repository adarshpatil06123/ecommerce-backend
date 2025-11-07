#!/bin/sh
# wait-for.sh: Wait for a service to be ready before starting the application

set -e

host="$1"
shift
cmd="$@"

# Default values
WAIT_TIMEOUT=${WAIT_TIMEOUT:-60}
WAIT_INTERVAL=${WAIT_INTERVAL:-2}

echo "Waiting for $host to be ready..."

elapsed=0
until nc -z "$host" 3306 2>/dev/null || [ $elapsed -ge $WAIT_TIMEOUT ]; do
  echo "  $host is unavailable - sleeping ($elapsed/$WAIT_TIMEOUT seconds)"
  sleep $WAIT_INTERVAL
  elapsed=$((elapsed + WAIT_INTERVAL))
done

if [ $elapsed -ge $WAIT_TIMEOUT ]; then
  echo "Timeout waiting for $host after $WAIT_TIMEOUT seconds"
  exit 1
fi

echo "$host is ready - executing command: $cmd"
exec $cmd
