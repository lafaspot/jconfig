# Environment Variables
#
#   {JCONFIG_CONF_DIR}   Alternate hbase conf dir. Default is ${JCONFIG_HOME}/conf.
#   {JCONFIG_LOG_DIR}    Where log files are stored.  PWD by default.
#   {JCONFIG_PID_DIR}    The pid files are stored. /tmp by default.
#

usage="Usage: jconfig-daemon.sh [--config <conf-dir>]\
 (start|stop|restart|autorestart) <jconfig-command> \
 <args...>"

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

check_before_start(){
    #ckeck if the process is not running
    mkdir -p "$JCONFIG_PID_DIR"
    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi
}

# get log directory
if [ "$JCONFIG_LOG_DIR" = "" ]; then
  export JCONFIG_LOG_DIR="$JCONFIG_HOME/logs"
fi
mkdir -p "$JCONFIG_LOG_DIR"

if [ "$JCONFIG_PID_DIR" = "" ]; then
  JCONFIG_PID_DIR=/tmp
fi

# Some variables
# Work out java location so can print version into log.
if [ "$JAVA_HOME" != "" ]; then
  #echo "run java in $JAVA_HOME"
  JAVA_HOME=$JAVA_HOME
fi
if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA=$JAVA_HOME/bin/java

