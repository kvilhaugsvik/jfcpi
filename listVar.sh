#!/bin/sh

var_name="$1"
if test x"$var_name" = x; then
  echo "Please specify the variable name"
  exit 1
fi;

# could be empthy so no check
fileList="$2"

# Format a list
declaration="${var_name} ="
for file_to_add in ${fileList}; do
  declaration="$declaration \\\\\n\t$file_to_add"
done;
echo $declaration
