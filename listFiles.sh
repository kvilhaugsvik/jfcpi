#!/bin/bash

# name the parameters
scanFolder="$1"

# output location
outputFile="$scanFolder/source_code.list"

# Java code used in the program it self
java_src="${scanFolder}_java_src ="
for sourceFile in `find $scanFolder/src -iname "*.java"`; do
  java_src="$java_src \\\\\n\t$sourceFile"
done;
echo $java_src > $outputFile
