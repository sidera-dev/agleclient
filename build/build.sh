#! /bin/sh

#### Edit the following line to point to your java JDK directory, and remove the #

# Enter your java JDK directory
JAVA_HOME=usr/lib/Java/jdk1.6.0_12
# Enter yor ant directory
ANT_HOME=../Ant
# Enter your ant and java lib directry
CLASSPATH=../Ant/lib;/usr/lib/Java/jdk1.6.0_12/lib
# Enter your ant and java bin directry
PATH=%path%;../Ant/bin;/usr/lib/Java/jdk1.6.0_12/bin

ant -v