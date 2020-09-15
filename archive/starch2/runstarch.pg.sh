#!/bin/sh

CLASSPATH="/bmrb/javaclasses/postgresql-8.0-311.jdbc3.jar:/bmrb/javaclasses/starch-min.jar:/bmrb/javaclasses"
export CLASSPATH
java EDU.bmrb.starch.gui.Main "$@"
