#!/usr/bin/env bash
source /opt/aw/conf/setEnv.sh
$JAVA_HOME/bin/java -cp "/opt/aw/lib/tools/*:/opt/aw/lib/rest/*" com.aw.tools.DocTool -host_port $DG_HOST:8080 -tenant 0 -op update -file driver.json