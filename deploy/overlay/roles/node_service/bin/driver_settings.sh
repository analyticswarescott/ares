#!/usr/bin/env bash
source /opt/dg/conf/setEnv.sh
$JAVA_HOME/bin/java -cp "/opt/aw/lib/tools/*:/opt/aw/lib/rest/*" com.dg.tools.DocTool -host_port $DG_HOST:8080 -tenant 0 -op update -file driver.json