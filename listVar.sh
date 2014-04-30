#!/bin/sh

var_name="$1"
if test x"$var_name" = x; then
  echo "Please specify the variable name"
  exit 1
fi;

# could be empthy so no check
fileList="$2"

# '=' is a special value that indicate no variable should be declared
if test "${var_name}" != "="; then
  declaration="AC_SUBST(${var_name}, [\""
else
  declaration=""
fi;

# Format a list
for file_to_add in ${fileList}; do
  declaration="$declaration \\\\\n $file_to_add"
done;

# '=' is a special value that indicate no variable should be declared
if test "${var_name}" != "="; then
  declaration="$declaration\"])"
fi;

echo $declaration
