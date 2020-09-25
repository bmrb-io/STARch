#!/bin/sh

CLASSPATH="/bmrb/javaclasses/ojdbc14.jar:/bmrb/javaclasses/starch-min.20050316.jar:/bmrb/javaclasses"
export CLASSPATH
java EDU.bmrb.starch.gui.Main "$@"
