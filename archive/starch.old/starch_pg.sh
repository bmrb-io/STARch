#!/bin/sh

# Oracle
#JDBC_JAR="/bmrb/javaclasses/ojdbc14.jar"
# Postgres
JDBC_JAR="/bmrb/javaclasses/postgresql-8.0-311.jdbc3.jar"

CLASSPATH="${JDBC_JAR}:build:/bmrb/javaclasses"
export CLASSPATH

java EDU.bmrb.starch.gui.Main "$@"
