#!/usr/bin/env bash
export SERVICE_SHARED_SECRET=sharedsecret

curl -X POST dev-dg8-$1:8080/rest/1.0/admin/platform/patch/$2 --data-binary @"./archive/patch.tar.gz" -i -H "serviceSharedSecret: $SERVICE_SHARED_SECRET"
