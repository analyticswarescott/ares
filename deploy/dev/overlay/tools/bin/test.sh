#!/usr/bin/env bash

#run a scale test using the supplied platform file and test definition file
export PLATFORM_PATH=$1
$JAVA_HOME/bin/java -classpath "${DG_HOME}/lib/tools/*:${DG_HOME}/lib/rest/*" com.dg.tools.scale.ScaleTest -testdef $2
