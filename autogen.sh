#!/bin/sh

echo "Finding the source code"
sh listFiles.sh Core
sh listFiles.sh Utility
sh listFiles.sh JavaGenerator
sh listFiles.sh DependencyHandler
sh listFiles.sh GeneratePackets
sh listFiles.sh FreecivRecorder
sh listFiles.sh SignInTest

echo "Generating the configure script and related files"
autoreconf --install

echo "Running ./configure $@"
./configure $@
