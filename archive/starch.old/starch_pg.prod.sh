#!/bin/sh

# Oracle
#JDBC_JAR="/bmrb/javaclasses/ojdbc14.jar"
# Postgres
JDBC_JAR="/bmrb/javaclasses/postgresql-8.0-311.jdbc3.jar"

CLASSPATH="${JDBC_JAR}:/bmrb/javaclasses/starch-min.jar:/bmrb/javaclasses"
export CLASSPATH

java EDU.bmrb.starch.gui.Main "$@"
