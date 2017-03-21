#!/bin/sh

#
# svcjava_install.sh, version 1.0
# Suitable script for install maite server on unix platforms
#
# ATTENTION:
#
# REMARKS:
#
# TODO:
#
# MODIFS:
#	HTE, 1/8/03: Creation of script
#

# test called by svcjava.sh
if [ "$CONF_NAME" = "" ] ; then
  echo "ERROR: Must be called by svcjava.sh"
  exit 1
fi


# variables
SRV_DATAS=$J2EE_DATAS/$CONF_NAME
SHARED_DATAS=/data/chr

# create server ressources
create_dirandlink "$SRV_DATAS/logs"	"./logs"	; RC=$? ; [ "$RC" -ne 0 ] && return 1

# return
return 0

#
# EOF svcjava_install.sh
#
