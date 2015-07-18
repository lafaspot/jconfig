Usage: sh jconfig-daemon.sh [-c configDirectoryPath -l libDirectoryPath -p logDirectoryPath] 
(start|stop) 

where: 
    -c  JCONFIG loader conf directory path (required as arg or set it as env var)
    -l  lib directory path  (required as arg or set it as env var)
    -p  log directory path (optional. If not specified, logs will be written in libs directory)
    start - start ConfigLoader application 
    stop - stop ConfigLoader application. 
 
You can also specify config/lib/log directory path in form of Environment Variables. 
If both are set priority will be given to arguments passed while running script. 
   {JCONFIG_CDIR}  JCONFIG loader conf directory path. 
   {JCONFIG_HOME}  ConfigLoader lib directory path. This is the path for all dependency jars. 
   {JCONFIG_LOG_DIR} ConfigLoader log directory path.     

Example
NuclearApp in jconfig-test package consumes config via JCONFIG. If Config loader is not running it will run with defaults specified
in NuclearConfig class file. 

https://github.com/lafaspot/jconfig/wiki
