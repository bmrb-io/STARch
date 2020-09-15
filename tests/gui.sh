#!/bin/sh
#
if [ -z "$1" ]
then 
    wd=`pwd`
else
    wd="$1"
fi
env CLASSPATH="../build:/share/dmaziuk/lib/sans.jar:/share/dmaziuk/lib/getopt.jar:/bmrb/javaclasses/postgresql-8.0-311.jdbc3.jar" \
java edu.bmrb.starch.Main -d $wd
