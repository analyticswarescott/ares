#!/usr/bin/env bash
echo "Stopping platform..."
./rest_curl.sh PUT platform/STOPPED

#TODO: wait for platform to enter stopped state (use Local Client for this?) -- stopnode alias can be used manaually for now
#echo "Stopping control services (REST and node service)..."
#./node_service.sh stop