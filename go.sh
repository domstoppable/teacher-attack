#!/bin/bash

javac -source 1.6 -target 1.6 -d bin src/org/greenlightgo/teacherattack/*.java
if [ $? -eq 0 ]; then
	cd bin
	jar -cfe TeacherAttack-client.jar org.greenlightgo.teacherattack.PlayerApp org/ resources/ >/dev/null 2>&1
	jar -cfe TeacherAttack-server.jar org.greenlightgo.teacherattack.GameServer org/ resources/ >/dev/null 2>&1
	cp TeacherAttack-client.jar /tmp >/dev/null 2>&1
	class=$1
	shift
	java "org.greenlightgo.teacherattack.$class" $*
fi
