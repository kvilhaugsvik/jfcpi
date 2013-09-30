# SYNOPSIS
#
#   AX_PROG_SCALA
#
# DESCRIPTION
#
#   AX_PROG_SCALA will check that scala is available and set SCALA for the
#   Makefile.
#
# LICENSE
#
#   Copyright 2013 Sveinung Kvilhaugsvik <sveinung84@users.sourceforge.net>
#
#   Copying and distribution of this file, with or without modification,
#   are permitted in any medium without royalty provided the copyright
#   notice and this notice are preserved.  This file is offered as-is,
#   without any warranty.

AC_ARG_VAR(SCALA, [scala runner])

AC_DEFUN([AX_PROG_SCALA], [
AC_CHECK_PROGS(SCALA, [scala])
AS_IF([test x"$SCALA" != x], [ 
AC_MSG_NOTICE([Scala found: $SCALA])
], 
AC_MSG_WARN([Scala not found])
)
])
