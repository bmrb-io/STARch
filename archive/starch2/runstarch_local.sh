#!/bin/sh

CLASSPATH="/bmrb/javaclasses/postgresql-8.0-311.jdbc3.jar:dist/starch-min.20061024.jar:/bmrb/javaclasses"
export CLASSPATH
java EDU.bmrb.starch.gui.Main "$@"
