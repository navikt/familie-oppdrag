#!/bin/sh
echo "running init.sh script"
export SERVICEUSER_USERNAME=$(cat /secret/serviceuser/username)
export SERVICEUSER_PASSWORD=$(cat /secret/serviceuser/password)