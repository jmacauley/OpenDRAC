#!/bin/sh

# chkconfig: - 80 80
# description: This script is used as a startup script.
# processname: OpenDRAC

# Source function library.
. /etc/init.d/functions



#######################################
# Var declarations
#######################################
OPENDRAC_HOME=/home/robert/opendrac
EXIT_CODE=0


#######################################
# Function startOpenDRAC
#######################################
function startOpenDRAC(){
  IS_RUNNING=`ps -aef | grep -i java | grep -i drac | grep -v WatchDog | grep -v grep`;
  if [ -z  "$IS_RUNNING" ]
    then
      echo "Starting OpenDRAC";
      daemon --user drac "cd $OPENDRAC_HOME && ./bin/start.sh &> /dev/null &"
    else  
    echo "OpenDRAC is already running!"
  fi;
}


#######################################
# Function stopOpenDRAC
#######################################
function stopOpenDRAC(){
 echo "Stopping OpenDRAC";
  ps -aef | grep -i java | grep OpenDRAC-Main | awk '{print $2}' | xargs kill -9;
  cd $OPENDRAC_HOME/var/run
  for i in `ls | egrep "[0-9]{1,}" -o`; do kill -9 $i; rm *.$i.pid; done;
  exit ${EXIT_CODE};
}


#######################################
# Execute script
#######################################
case "$1" in
   start)
  # start OpenDRAC Server
  startOpenDRAC;
  exit ${EXIT_CODE}
  ;;
  
   stop)
  # stop OpenDRAC Server
  stopOpenDRAC;
  exit ${EXIT_CODE}
  ;;
  
   restart)
  # stop and start OpenDRAC Server
  stopOpenDRAC;
  startOpenDRAC;
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
