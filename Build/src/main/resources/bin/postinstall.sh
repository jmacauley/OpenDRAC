#!/bin/bash


#######################################
# Var declarations
#######################################
INSTALL_LOCATION=`pwd`;
DEFAULT_IP_PRIMARY=localhost
DEFAULT_IP_SECONDARY=localhost
WEB_MODE="no"
CTRL_IP_PRIMARY=$DEFAULT_IP_PRIMARY
LAUNCHER_TEMPLATE_XML="conf/templates/opendrac.xml"
LAUNCHER_XML="conf/opendrac.xml"


#######################################
# Function checkStatusOfLastCommand
#######################################
function checkLastCommand(){
  if [ $? -ne 0 ]; then
    echo -n "ERROR: $1";
    echo " Exiting now!";
    exit -1;
  fi;
}


#######################################
# Function editLauncherXml
#######################################
function editLauncherXml(){
  echo "*** Replacing primary ip address in $LAUNCHER_XML with $OPENDRAC_IP_PRIMARY";
  sed "s|IP_ADDRESS_PRIMARY|$OPENDRAC_IP_PRIMARY|g" $LAUNCHER_XML >$LAUNCHER_XML.bak;
  checkLastCommand "*** ERROR: creation of $LAUNCHER_XML went wrong";
  mv $LAUNCHER_XML.bak $LAUNCHER_XML;
  
  echo "*** Replacing secondary ip address in $LAUNCHER_XML with $OPENDRAC_IP_SECONDARY";
  sed "s|IP_ADDRESS_SECONDARY|$OPENDRAC_IP_SECONDARY|g" $LAUNCHER_XML >$LAUNCHER_XML.bak;
  checkLastCommand "*** ERROR: creation of $LAUNCHER_XML went wrong";
  mv $LAUNCHER_XML.bak $LAUNCHER_XML;
}

#######################################
# Function editOpenDRACInitD
#######################################
function editOpenDRACInitD(){
  echo "*** Replacing OPENDRAC_HOME_INSTALL_LOCATION in share/scripts/init.d/opendrac.sh with $INSTALL_LOCATION";
  sed "s|OPENDRAC_HOME_INSTALL_LOCATION|$INSTALL_LOCATION|g" share/scripts/init.d/opendrac.sh >share/scripts/init.d/opendrac.sh.changed;
  checkLastCommand "*** ERROR: creation of share/scripts/init.d/opendrac.sh went wrong";
  mv share/scripts/init.d/opendrac.sh.changed share/scripts/init.d/opendrac.sh;
}

#######################################
# Function editOpenDRACWatchdogInitD
#######################################
function editOpenDRACWatchdogInitD(){
  echo "*** Replacing OPENDRAC_WATCHDOG_HOME_INSTALL_LOCATION in share/scripts/init.d/opendrac-watchdog.sh with $INSTALL_LOCATION/share/opendrac-watchdog/share/opendrac-watchdog";
  sed "s|OPENDRAC_WATCHDOG_HOME_INSTALL_LOCATION|$INSTALL_LOCATION/share/opendrac-watchdog|g" share/scripts/init.d/opendrac-watchdog.sh >share/scripts/init.d/opendrac-watchdog.sh.changed
  checkLastCommand "*** ERROR: creation of share/scripts/init.d/opendrac.sh went wrong";
  mv share/scripts/init.d/opendrac-watchdog.sh.changed share/scripts/init.d/opendrac-watchdog.sh;
}

#######################################
# Function editOpenDRACRedundancyConfig
#######################################
function editOpenDRACRedundancyConfig(){
  echo "*** Replacing OPENDRAC_HOME_INSTALL_LOCATION in share/opendrac-watchdog/conf/env/opendrac-redundancy-default.xml with $INSTALL_LOCATION";
  sed "s|OPENDRAC_HOME_INSTALL_LOCATION|$INSTALL_LOCATION|g" share/opendrac-watchdog/conf/env/opendrac-redundancy-default.xml > share/opendrac-watchdog/conf/env/opendrac-redundancy-default.xml.changed
  checkLastCommand "*** ERROR: creation of share/opendrac-watchdog/conf/env/opendrac-redundancy-default.xml went wrong";
  mv share/opendrac-watchdog/conf/env/opendrac-redundancy-default.xml.changed share/opendrac-watchdog/conf/env/opendrac-redundancy-default.xml;
}


#######################################
# Function readUserInput
#######################################
function readUserInput(){
  echo "*** Postinstall phase of OpenDRAC installation";
  
  read -p "*** Enter OpenDRAC user home directory (default=$HOME) :"  OPENDRAC_USER_HOME; # needed for storing the keystore
  if [ -z $OPENDRAC_USER_HOME ]
  then
    OPENDRAC_USER_HOME=$HOME;
  fi
  checkLastCommand "*** ERROR, validating user home $OPENDRAC_USER_HOME";
  
  read -p "*** Enter primary OpenDRAC address (default=$DEFAULT_IP_PRIMARY) :"   OPENDRAC_IP_PRIMARY; 
  if [ -z $OPENDRAC_IP_PRIMARY ]
   then
     OPENDRAC_IP_PRIMARY=$DEFAULT_IP_PRIMARY;
  fi
  
  read -p "*** Enter secondary OpenDRAC address (default=$DEFAULT_IP_SECONDARY) :"   OPENDRAC_IP_SECONDARY; 
  if [ -z $OPENDRAC_IP_SECONDARY ]
   then
     OPENDRAC_IP_SECONDARY=$DEFAULT_IP_SECONDARY;
  fi
  cp $LAUNCHER_TEMPLATE_XML $LAUNCHER_XML;
  checkLastCommand "*** ERROR, validating IP address $OPENDRAC_IP_PRIMARY"; 
  
  # read -p "*** Enter OpenDRAC JAVA_HOME (default=$JAVA_HOME) :"   J_HOME; 
  # if [ -z $J_HOME ]
  # then
  #  J_HOME=$JAVA_HOME;
  # fi
  # checkLastCommand "*** ERROR, validating $JAVA_HOME";
    
}


#######################################
# Function changePermissions
#######################################
function changePermissions(){
  chmod +x bin/*.sh;
  chmod +x share/scripts/init.d/*.sh;
  checkLastCommand "*** ERROR: giving execute permissions to file in bin/*.sh";
}


#######################################
# Function copy def keystore
#######################################

# TODO: Generate using the https://svn.surfnet.nl/trac/opendrac/wiki/DracInstallation
# 3.2.4 Configuring the workstation certificate

function copyKeystore(){
  if [ ! -f $OPENDRAC_USER_HOME/.keystore ]
  then
    echo "*** Copying default keystore to $OPENDRAC_USER_HOME/.keystore";
    cp conf/templates/dot-keystore $OPENDRAC_USER_HOME/.keystore;
  fi
  checkLastCommand "*** ERROR: copying default keystore to $OPENDRAC_USER_HOME/.keystore";
}


#######################################
# Execute script
#######################################
readUserInput;
editLauncherXml;
editOpenDRACInitD;
editOpenDRACWatchdogInitD;
editOpenDRACRedundancyConfig;
copyKeystore;
changePermissions;
