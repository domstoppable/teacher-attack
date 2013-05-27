#!/bin/bash

javac -source 1.5 -target 1.5 -d bin src/org/greenlightgo/teacherattack/*.java
if [ $? -eq 0 ]; then
	cd bin
	class=$1
	shift
	java "org.greenlightgo.teacherattack.$class" $*
fi
