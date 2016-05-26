#!/usr/bin/env bash

export PLATFORM_PATH=$1
$JAVA_HOME/bin/java -classpath "${DG_HOME}/lib/tools/*:${DG_HOME}/lib/rest/*" com.dg.tools.tenant.TenantCli -op UNPROVISION
