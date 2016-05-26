#!/usr/bin/env bash
exec > /tmp/patch.log 2>&1
set -x
/opt/dg/roles/node_service/bin/node_service.sh stop
cd $DG_HOME
tar -xf patch.tar.gz
tar -xf overlay.tar
rm overlay.tar
rm conf.tar
mv patch.tar.gz /tmp
bin/init_buildstamp.sh $1
/opt/dg/roles/node_service/bin/node_service.sh start