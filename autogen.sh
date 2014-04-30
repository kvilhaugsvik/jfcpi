#!/bin/sh

# Make it possible to run ./configure && make
# Will run ./configure and pass its parameters to it

echo "Finding the source code"
sh listFiles.sh Core
sh listFiles.sh Utility
sh listFiles.sh JavaGenerator
sh listFiles.sh DependencyHandler
sh listFiles.sh GeneratePackets
sh listFiles.sh FreecivRecorder
sh listFiles.sh SignInTest
sh listFiles.sh GenerateTestCode

echo "Generating the configure script and related files"
autoreconf --install

echo "Running ./configure $@"
./configure $@
