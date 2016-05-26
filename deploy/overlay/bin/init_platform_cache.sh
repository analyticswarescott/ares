#!/usr/bin/env bash

$JAVA_HOME/bin/java -classpath "${DG_HOME}/lib/rest/*" com.dg.common.system.cli.LocalFileClient -operation init_platform_cache

