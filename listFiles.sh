#!/bin/bash

# name the parameters
scanFolder="$1"

if test x"$scanFolder" = x; then
  echo "Please specify what module to generate a list for"
  exit 1
fi;

# output location
outputFile="$scanFolder/source_code.list"

# explain what it is
echo "# This is an auto generated list of files for the Makefile." > $outputFile

# Java code used in the program it self
sh listVar.sh "${scanFolder}_java_src" "`find ${scanFolder}/src -iname '*.java'`" >> $outputFile

# Scala code used in the program it self
sh listVar.sh "${scanFolder}_scala_src" "`find $scanFolder/src -iname '*.scala'`" >> $outputFile

echo "${scanFolder}_src = \${${scanFolder}_java_src} \${${scanFolder}_scala_src}" >> $outputFile

echo "" >> $outputFile

echo "COMPILED_${scanFolder}_FOLDER = @WORK_FOLDER@/${scanFolder}" >> $outputFile
echo "" >> $outputFile

# test Java code
sh listVar.sh "${scanFolder}_java_test_src" "`find $scanFolder/test -iname '*.java'`" >> $outputFile

# test Scala code
sh listVar.sh "${scanFolder}_scala_test_src" "`find $scanFolder/test -iname '*.scala'`" >> $outputFile

echo "COMPILED_${scanFolder}_TEST_FOLDER = @WORK_FOLDER@/${scanFolder}_TEST" >> $outputFile
echo "" >> $outputFile
