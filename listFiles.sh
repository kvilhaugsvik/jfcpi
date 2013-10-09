#!/bin/bash

# name the parameters
scanFolder="$1"

if test x"$scanFolder" = x; then
  echo "Please specify what module to generate a list for"
  exit 1
fi;

# output location
outputFile="$scanFolder/source_code.list"

# Java code used in the program it self
java_src="${scanFolder}_java_src ="
for sourceFile in `find $scanFolder/src -iname "*.java"`; do
  java_src="$java_src \\\\\n\t$sourceFile"
done;
echo $java_src > $outputFile

# Scala code used in the program it self
scala_src="${scanFolder}_scala_src ="
for sourceFile in `find $scanFolder/src -iname "*.scala"`; do
  scala_src="$scala_src \\\\\n\t$sourceFile"
done;
echo $scala_src >> $outputFile

echo "" >> $outputFile
