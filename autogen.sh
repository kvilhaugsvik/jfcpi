#!/bin/sh

echo "Generating the configure script and related files"
autoreconf --install

echo "Running ./configure $@"
./configure $@
