echo "Setting up base environment ..."

DG_HOST=`hostname`

_TARGET_USER="dg8service"
_TARGET_USER_GRP="dg8service"
read -p "* What is the service user's name? (press enter for $_TARGET_USER) " TARGET_USER
if [ -z "$TARGET_USER" ]; then TARGET_USER=$_TARGET_USER; fi
echo "Your service user is $TARGET_USER"
read -p "* What is the service user's group? (press enter for $_TARGET_USER_GRP) " TARGET_USER_GRP
if [ -z "$TARGET_USER_GRP" ]; then TARGET_USER_GRP=$_TARGET_USER_GRP; fi
echo "Your service user's group is $TARGET_USER_GRP"

# create user or group if doesn't exist, and add the user to the group if it hasn't been added yet
sudo id -u "$TARGET_USER" &>/dev/null || sudo useradd -m "$TARGET_USER"
mkhomedir_helper "$TARGET_USER"
sudo getent group "$TARGET_USER_GRP" || sudo groupadd "$TARGET_USER_GRP"
sudo id -Gn "$TARGET_USER" | grep -i "$TARGET_USER_GRP" || sudo usermod -a -G "$TARGET_USER_GRP" "$TARGET_USER"

echo "Setting up for $TARGET_USER, group $TARGET_USER_GRP ...";

CURRENT_USER=`whoami`
CURRENT_USER_GRP=$(id -g -n $CURRENT_USER)
# temporarily make /opt/aw owned by the current user
sudo chown -hRH  "$CURRENT_USER:$CURRENT_USER_GRP" /opt/dg


if [ ! -d /mnt/dg ]; then
    echo "Linking /opt/aw/roles to /mnt/aw."

    # link /opt/aw/roles to /mnt/aw
    sudo ln -s /opt/dg/roles /mnt/dg
fi

echo "Done setting up base environment."

addCustomEnv() {
    VARIABLE_NAME=$1
    DEFAULT_VALUE=$2
    PROMPT_TEXT=$3
    CONFIRM_TEXT=$4

    if [[ ! -f /opt/dg/conf/custom_env.sh ]] || [[ -z `cat /opt/dg/conf/custom_env.sh | grep "$VARIABLE_NAME"` ]]; then
        read -p "* $PROMPT_TEXT (press enter for $DEFAULT_VALUE) " VARIABLE_VALUE
        if [ -z "$VARIABLE_VALUE" ]; then VARIABLE_VALUE=$DEFAULT_VALUE; fi
        echo "$CONFIRM_TEXT is $VARIABLE_VALUE"
        echo "export $VARIABLE_NAME=\"$VARIABLE_VALUE\"" >> /opt/dg/conf/custom_env.sh

    fi
}

# gather configuration for this node
addCustomEnv "FIRST_NODE" "false" "Is this the first node you are installing?" "The value you chose is"
addCustomEnv "MULTI_NODE" "false" "Is this a multi-node installation?" "The value you chose is"

addCustomEnv "ARES_HOST" $ARES_HOST "What should be used as the local host name?" "The value you chose is"

addCustomEnv "FIRST_NODE_CONNECT" "$DG_HOST:8080" "What is FIRST_NODE host:port ? " "The value you chose is"
addCustomEnv "BEARER_TOKEN_SECRET_KEY" "61f1b78448adfb472861d2730d0e3cb41eb5859b61049635" "What is the  Bearer token shared secret key?" "The value you chose is"
addCustomEnv "SERVICE_SHARED_SECRET" "sharedsecret" "What is the platform node services shared secret key?" "The value you chose is"
addCustomEnv "DISABLE_AUTH_VALIDATION" "false" "Disable security?" "The value you chose is"
addCustomEnv "DEVELOPMENT_MODE" "true" "Development mode on?" "The value you chose is"
addCustomEnv "ALLOW_ORIGIN_HOST" "http://localhost:63342" "What host to allow CORS requests?" "The value you chose is"


#TODO: this will likely only work on centos -- will need to version detect to configure sysstat on other flavors
#configure sysstat


sudo cp /opt/dg/conf/sysstat/sysstat.dgconf /etc/sysconfig/sysstat

sudo cp /opt/dg/conf/sysstat/sysstat.cron /etc/cron.d/sysstat

#restart cron? -- should not be needed TODO: early testing has seen this to be needed after changing the files, but need to follow up
sudo service crond restart


# update ~/.bashrc to source in the environment variables from setEnv.sh
_USER_HOME=$(eval echo ~$TARGET_USER);
if [[ -z `cat $_USER_HOME/.bashrc | grep "setEnv.sh"` ]]; then
    sudo cat $_USER_HOME/.bashrc > /tmp/_bashrc
    echo "source /opt/aw/conf/setEnv.sh" >> /tmp/_bashrc
    sudo cp /tmp/_bashrc $_USER_HOME/.bashrc
    sudo chmod +x $_USER_HOME/.bashrc
fi

if [[ -z `cat ~/.bashrc | grep "setEnv.sh"` ]]; then
    echo "source /opt/aw/conf/setEnv.sh" >> ~/.bashrc
fi

# setup symlinks
rm -f /opt/dg/roles/elastic
ln -s /opt/dg/roles/elasticsearch-2.1.1 /opt/dg/roles/elastic

rm -f /opt/dg/roles/hadoop
ln -s /opt/dg/roles/hadoop-2.7.1 /opt/dg/roles/hadoop

rm -f /opt/dg/roles/java
ln -s /opt/dg/roles/jdk1.8.0_25 /opt/dg/roles/java

rm -f /opt/dg/roles/kafka
ln -s /opt/dg/roles/kafka_2.10-0.8.2.1 /opt/dg/roles/kafka

rm -f /opt/dg/roles/kibana
ln -s /opt/dg/roles/kibana-4.0.2-linux-x64 /opt/dg/roles/kibana

rm -f /opt/dg/roles/zookeeper
ln -s /opt/dg/roles/zookeeper-3.4.6 /opt/dg/roles/zookeeper

rm -f /opt/dg/roles/kafka-manager
ln -s /opt/dg/roles/kafka-manager-1.2.8 /opt/dg/roles/kafka-manager

rm -f /opt/dg/roles/spark
ln -s /opt/dg/roles/spark-1.6.0-bin-hadoop2.6 /opt/dg/roles/spark

# make all shell scripts executable
chmod +x /opt/dg/patch.sh
chmod +x /opt/dg/roles/rest/bin/*.sh
chmod +x /opt/dg/roles/elastic/bin/*
chmod +x /opt/dg/roles/hadoop/bin/*
chmod +x /opt/dg/roles/hadoop/sbin/*
chmod +x /opt/dg/roles/spark/bin/*
chmod +x /opt/dg/roles/spark/sbin/*
chmod +x /opt/dg/roles/java/bin/*
chmod +x /opt/dg/roles/java/jre/bin/*
chmod +x /opt/dg/roles/kafka/bin/*.sh
chmod +x /opt/dg/roles/kibana/bin/*
chmod +x /opt/dg/roles/kibana/node/bin/*
chmod +x /opt/dg/roles/zookeeper/bin/*.sh
chmod +x /opt/dg/roles/kafka-manager/bin/*
chmod +x /opt/dg/roles/http-server/node_modules/http-server/bin/*
chmod +x /opt/dg/roles/node/bin/*
chmod +x /opt/dg/roles/node_service/bin/*.sh

source /opt/dg/conf/setEnv.sh

/opt/dg/bin/init_platform_cache.sh
/opt/dg/bin/init_buildstamp.sh 1

# make it all owned by the target user
echo "Making /opt/aw owned by $TARGET_USER:$TARGET_USER_GRP"
sudo chown -hRH "$TARGET_USER:$TARGET_USER_GRP" /opt/dg

#in case /opt/aw is a sym link, need to chown it explicitly, as hRH seems to leave it behind,
# but is required to handle the nested sym links in roles
sudo chown  "$TARGET_USER:$TARGET_USER_GRP" /opt/dg

echo "Setup Complete. "

sudo su - $TARGET_USER
source /opt/dg/conf/setEnv.sh
startnode
