all : jar docs 

classfiles :
	${JAVA_HOME}/bin/javac -d classes \
-sourcepath src \
src/jssh/*.java src/de/mud/ssh/*.java

# Use this target to compile the source files and create the JAR file.
jar : classfiles
	cd classes && ${JAVA_HOME}/bin/jar cvf ../lib/jssh.jar jssh com de

# Use this target to generate documentation in the doc directory.
docs :
	rm -rf doc/*
	${JAVA_HOME}/bin/javadoc \
    -sourcepath src \
    -classpath classes \
    -package \
    -overview src/overview.html \
    -doctitle "Java SSH Client Library" \
    -windowtitle "Java SSH Client" \
    -d doc \
    jssh

# Delete all the class files and the documentation files.
clean :
	rm -rf classes/* doc/* lib/jssh.jar
