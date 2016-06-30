#!/usr/bin/env bash
#TODO: this is a temporary measure to establish a build stamp on install
$JAVA_HOME/bin/java -classpath "${DG_HOME}/lib/rest/*" com.aw.common.system.cli.LocalFileClient -operation init_buildstamp -version $1

