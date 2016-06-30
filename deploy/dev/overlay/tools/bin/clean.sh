#!/usr/bin/env bash

export PLATFORM_PATH=$1
$JAVA_HOME/bin/java -classpath "${DG_HOME}/lib/tools/*:${DG_HOME}/lib/rest/*" com.aw.tools.tenant.TenantCli -op UNPROVISION
