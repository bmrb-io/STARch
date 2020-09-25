#!/bin/sh
#
# Uncomment and change paths if Java 1.5 is not the default JVM and uncomment "export" line below
#JAVA_HOME="/usr/java5"
#PATH="/usr/java5/bin:${PATH}"
#export JAVA_HOME PATH
#
# Replace CHANGE_PATH_TO with path to Postgres JDBC driver jar
PGDRIVER=/CHANGE_PATH_TO/postgresql-8.0-311.jdbc3.jar

#
# Replace CHANGE_PATH_TO with path to STARch jar
STARCH=/CHANGE_PATH_TO/starchbin.jar

CLASSPATH="${PGDRIVER}:${STARCH}"
export CLASSPATH

if [ -z $1 ] 
then
    dir=`pwd`
else
    dir="$1"
fi

java edu.bmrb.starch.Main -d $dir
