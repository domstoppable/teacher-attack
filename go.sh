#!/bin/bash

javac -d bin src/org/greenlightgo/teacherattack/*.java
if [ $? -eq 0 ]; then
	cd bin
	class=$1
	shift
	java "org.greenlightgo.teacherattack.$class" $*
fi
