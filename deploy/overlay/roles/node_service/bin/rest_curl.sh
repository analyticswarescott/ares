#!/usr/bin/env bash
set -x
curl -X $1 localhost:8080/rest/1.0/admin/$2 -v  -i -H "serviceSharedSecret: $SERVICE_SHARED_SECRET" $3