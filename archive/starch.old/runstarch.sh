#!/bin/sh

CLASSPATH="/bmrb/javaclasses/ojdbc14.jar:/bmrb/javaclasses/starch-min.jar:/bmrb/javaclasses"
export CLASSPATH
java EDU.bmrb.starch.gui.Main "$@"
