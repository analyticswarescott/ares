set -xe

echo "** Starting the reporting UI."

# start the ui
cd $DG_HOME/reporting/ui
nohup $DG_ROLES/node/bin/node $DG_ROLES/http-server/node_modules/http-server/bin/http-server -p 8000 > $DG_LOGS/ui.log 2>&1 &

echo "Started the UI on port 8000."