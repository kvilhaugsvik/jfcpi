AC_INIT([Java Freeciv Protocol Implementation], [0.0.2.99])

AM_INIT_AUTOMAKE([-Wall -Werror foreign])

AC_CONFIG_MACRO_DIR([m4])

AX_PROG_JAVA
AX_PROG_JAVAC
AX_PROG_JAR

AX_PROG_SCALA
AX_PROG_SCALAC

AX_FIND_SCALA_STDLIB
AX_FIND_JUNIT

AC_MSG_NOTICE([Looking for the Freeciv source code])
# override
AC_ARG_VAR(FREECIV_SOURCE_PATH, [the location of the Freeciv source code])
AS_IF([test x"$FREECIV_SOURCE_PATH" = x],
  AC_SUBST(FREECIV_SOURCE_PATH, [[".."]]))
# abort if the Freeciv source isn't found
AC_CHECK_FILE("$FREECIV_SOURCE_PATH/fc_version",
  AC_MSG_NOTICE(Freeciv source code found in $FREECIV_SOURCE_PATH),
  AC_MSG_ERROR([Freeciv source code not found in $FREECIV_SOURCE_PATH]))

AC_ARG_ENABLE(strict,
  AS_HELP_STRING(
   [--enable-strict],
   [stop the build if a problem is found (default is yes)]),
  AS_IF(test x"$enableval" != xno,
    AC_SUBST(IGNORE_ISSUES, false),
    AC_SUBST(IGNORE_ISSUES, true)),
  AC_SUBST(IGNORE_ISSUES, false))

AC_CONFIG_FILES([Makefile])
AC_OUTPUT
