# Environment Variables
#   in order to run please set JCONFIG_HOME environment variable - export JCONFIG_HOME=.
#   {JCONFIG_CDIR}   JCONFIG loader conf directory. Default is ${JCONFIG_HOME}/conf.
#   {JCONFIG_LOG_DIR}    Where log files are stored.  PWD by default.
#   {JCONFIG_PID_DIR}    The pid files are stored. /tmp by default.
#   JCONFIG_IDENT_STRING   A string representing this instance of jconfig loader. $USER by default

usage="Usage: jconfig-daemon.sh [--config <conf-dir>]\
 (start|stop)"

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

this="${BASH_SOURCE-$0}"
# convert relative path to absolute path
bin=`dirname "$this"`
script=`basename "$this"`
bin=`cd "$bin">/dev/null; pwd`
this="$bin/$script"

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

add_to_cp_if_exists() {
  if [ -d "$@" ]; then
    CLASSPATH=${CLASSPATH}:"$@"
  fi
}

execLoaderProcess() {
  # Detect if we are in JCONFIG sources dir
  in_dev_env=false
  if [ -d "${JCONFIG_HOME}/target" ]; then
    in_dev_env=true
  fi  

  JAVA_HEAP_MAX=-Xmx2000m 

  # check envvars which might override default args
  if [ "$JCONFIG_HEAPSIZE" != "" ]; then
    SUFFIX="m"
    if [ "${JCONFIG_HEAPSIZE: -1}" == "m" ] || [ "${JCONFIG_HEAPSIZE: -1}" == "M" ]; then
      SUFFIX=""
    fi
    if [ "${JCONFIG_HEAPSIZE: -1}" == "g" ] || [ "${JCONFIG_HEAPSIZE: -1}" == "G" ]; then
      SUFFIX=""
    fi
    #echo "run with heapsize $JCONFIG_HEAPSIZE"
    JAVA_HEAP_MAX="-Xmx""$JCONFIG_HEAPSIZE""$SUFFIX"
    #echo $JAVA_HEAP_MAX
  fi
  # CLASSPATH initially contains $JCONFIG_CDIR
  CLASSPATH="${JCONFIG_CDIR}"
  CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar

  #Add the development env class path stuff
  if $in_dev_env; then
    #add_to_cp_if_exists "${JCONFIG_HOME}/jconfig-loader/target/libs"
  # Add jconfig-loader.jar from target directory to classpath. 
  for f in ${JCONFIG_HOME}/jconfig-loader/target/libs/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
  done
  for f in ${JCONFIG_HOME}/jconfig-loader/target/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
  done

  fi

  # Add libs to CLASSPATH
  for f in $JCONFIG_HOME/lib/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
  done

  CLASS='org.commons.jconfig.configloader.ConfigLoaderRunner'
  export CLASSPATH
  echo "$JAVA" $JAVA_HEAP_MAX -DJCONFIG_LOG_DIR=$JCONFIG_LOG_DIR -DJCONFIG_CDIR=$JCONFIG_CDIR -cp $CLASSPATH $CLASS
  exec "$JAVA" $JAVA_HEAP_MAX -DJCONFIG_LOG_DIR=$JCONFIG_LOG_DIR -DJCONFIG_CDIR=$JCONFIG_CDIR -cp $CLASSPATH $CLASS -DJCONFIG_LOG_DIR=$JCONFIG_LOG_DIR  "$@" < /dev/null > ${logout} 2>&1
}

# the root of the JCONFIG installation
if [ -z "$JCONFIG_HOME" ]; then
  export JCONFIG_HOME=`dirname "$this"`/..
  echo $JCONFIG_HOME
fi

if [ "$JCONFIG_PID_DIR" = "" ]; then
  JCONFIG_PID_DIR=/tmp
fi


if [ "$JCONFIG_IDENT_STRING" = "" ]; then
  export JCONFIG_IDENT_STRING="$USER"
fi

#check to see if the conf dir or jconfig home are given as an optional arguments
while [ $# -gt 1 ]
do
  if [ "--config" = "$1" ]
  then
    shift
    confdir=$1
    shift
    JCONFIG_CDIR=$confdir
  fi
done

# if we didn't set it
if [ -z "$JAVA_HOME" ]; then
  cat 1>&2 <<EOF
+======================================================================+
|      Error: JAVA_HOME is not set and Java could not be found         |
+----------------------------------------------------------------------+
| Please download the latest Sun JDK from the Sun Java web site        |
|       > http://java.sun.com/javase/downloads/ <                      |
|                                                                      |
| JCONFIG requires Java 1.7 or later.                                    |
| NOTE: This script will find Sun Java whether you install using the   |
|       binary or the RPM based installer.                             |
+======================================================================+
EOF
  exit 1
fi

# get log directory. set JCONFIG_LOG_DIR as java argument. This will be consumed by log4j properties file. 
if [ "$JCONFIG_LOG_DIR" = "" ]; then
  JCONFIG_LOG_DIR="$JCONFIG_HOME/logs"
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
echo $JAVA_HOME
export JCONFIG_LOG_PREFIX=jconfig-$JCONFIG_IDENT_STRING-loader
JAVA=$JAVA_HOME/bin/java
logout=$JCONFIG_LOG_DIR/$JCONFIG_LOG_PREFIX.out
pid=$JCONFIG_PID_DIR/jconfig-$JCONFIG_IDENT_STRING-loader.pid
export JCONFIG_START_FILE=$JCONFIG_PID_DIR/jconfig-$JCONFIG_IDENT_STRING-loader.autorestart

# get arguments
startStop=$1
shift

case $startStop in

(start)
  echo "starting jconfig loader"
  touch "$JCONFIG_START_FILE"
  execLoaderProcess
  echo $! > $pid
  ;;

(stop)
  echo "stoping jconfig loader"
  rm -f "$JCONFIG_START_FILE"
    if [ -f $pid ]; then
      pidToKill=`cat $pid`
      # kill -0 == see if the PID exists
      if kill -0 $pidToKill > /dev/null 2>&1; then
        echo -n stopping $command
        echo "`date` Terminating $command" >> $loglog
        kill $pidToKill > /dev/null 2>&1
        rm $pid
      else
        retval=$?
        echo no $command to stop because kill -0 of pid $pidToKill failed with status $retval
      fi
    else
      echo no $command to stop because no pid file $pid
    fi
  ;;
(*)
  echo $usage
  exit 1
  ;;
esac    
