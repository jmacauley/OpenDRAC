#!/bin/sh

# chkconfig: - 80 80
# description: This script is used as a startup script.
# processname: OpenDRACWatchdog

# Source function library.
. /etc/init.d/functions


#######################################
# Var declarations
#######################################
OPENDRAC_WATCHDOG_HOME=OPENDRAC_WATCHDOG_HOME_INSTALL_LOCATION
EXIT_CODE=0


#######################################
# Function startOpenDRACWatchdog
#######################################
function startOpenDRACWatchdog(){ 
  daemon --user drac "cd $OPENDRAC_WATCHDOG_HOME && ./bin/watchdog.sh &> /dev/null &"
}


#######################################
# Function stopOpenDRACWatchdog
#######################################
function stopOpenDRACWatchdog(){
  ps -aef | grep -i java | grep -v grep | grep OpenDRAC-Watchdog | awk '{print $2}' | xargs kill -9;  
}


#######################################
# Execute script
#######################################
case "$1" in
   start)
  # start OpenDRAC Watchdog
  startOpenDRACWatchdog;
  exit ${EXIT_CODE}
  ;;
  
   stop)
  # stop OpenDRAC Watchdog
  stopOpenDRACWatchdog;
  exit ${EXIT_CODE}
  ;;
  
   restart)
  # stop and start OpenDRAC Watchdog
  stopOpenDRACWatchdog;
  startOpenDRACWatchdog;
  exit ${EXIT_CODE}
  ;;

   *)
  if [ -z $1 ]
  then
    echo "Usage: $0 {start|stop|restart}" >&2
  fi
  exit 1
  ;;
esac
                                                                                
exit 0
