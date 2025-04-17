@echo off

rem ------------------------------------------------------------------------------

rem Enter your java JDK directory
set JAVA_HOME=C:\Programmi\Java\jdk1.6.0_12
rem Enter yor ant directory
set ANT_HOME=C:\Programmi\Ant
rem Enter your ant and java lib directry
set CLASSPATH=C:\Programmi\Ant\lib;C:\Programmi\Java\jdk1.6.0_12\lib
rem Enter your ant and java bin directry
set PATH=%path%;C:\Programmi\Ant\bin;C:\Programmi\Java\jdk1.6.0_12\bin

rem ------------------------------------------------------------------------------

rem Execute Ant for compiling
ant -v

