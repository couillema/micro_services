#!/bin/sh

#
# svcjava_run.sh, version 1.0
# Suitable script for run maite server
#
# ATTENTION:
#
# REMARKS:
#
# TODO:
#
# MODIFS:
#       HTE, 1/8/03: Creation of script
#

# test called by svcjava.sh
if [ "$CONF_NAME" = "" ] ; then
  echo "ERROR: Must be called by svcjava.sh"
  exit 1
fi


# TESTS ok RUNNING
check_ok_run


# return
return 0

#
# EOF
#
