JCC = javac
JVM= java

JAVA_FILES = $(wildcard *.java)
CLASS_FILES = $(JAVA_FILES:.java=.class)

default: classes

classes: $(CLASS_FILES)

%.class: %.java
	 $(JCC) $<

run: $(CLASS_FILES)
	$(JVM) -javaagent:../target/mapleir-full-with-deps.jar MemeIn

clean:
	$(RM) *.class

.PHONY: default classes run clean

