#!/bin/sh

# Oracle 8
JDBC_JAR="classes12.zip"
# Oracle 10
#JDBC_JAR="ojdbc14.jar"
# Postgres
#JDBC_JAR="/bmrb/javaclasses/postgresql-8.0-311.jdbc3.jar"

CLASSPATH="${JDBC_JAR}:starch.jar"
export CLASSPATH

java EDU.bmrb.starch.gui.Main "$@"
