#!/bin/sh

set -e
set -x

java -cp /usr/share/java/velocity.jar:/usr/share/java/commons-collections3.jar:/usr/share/java/commons-lang.jar:hppc-templateprocessor-0.6.0-SNAPSHOT.jar com.carrotsearch.hppc.generator.TemplateProcessor hppc-core/src/main/templates gen/java/src
rm -f gen/java/src/com/carrotsearch/hppc/ObjectHeap.java
mkdir -p gen/java/class
(cd gen/java/src && find . -name "*.java" | xargs javac -cp ../../../hppc-0.6.0-SNAPSHOT.jar -g -d ../../../gen/java/class)
javac -cp hppc-0.6.0-SNAPSHOT.jar:gen/java/class HeapTest.java
