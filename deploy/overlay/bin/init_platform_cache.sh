#!/usr/bin/env bash

$JAVA_HOME/java -classpath "${ARES_HOME}/lib/rest/*" com.aw.common.system.cli.LocalFileClient -operation init_platform_cache

