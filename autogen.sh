#!/bin/sh

echo "Finding the source code"
sh listFiles.sh Core
sh listFiles.sh DependencyHandler

echo "Generating the configure script and related files"
autoreconf --install

echo "Running ./configure $@"
./configure $@
