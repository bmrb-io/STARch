#!/bin/sh

CLASSPATH="./build:/bmrb/javaclasses/ojdbc14.jar:/bmrb/javaclasses/hsqldb-1.7.3/lib/hsqldb.jar:/share/dmaziuk/sansj"
export CLASSPATH
java EDU.bmrb.starch.gui.Main "$@"
