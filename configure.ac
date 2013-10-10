AC_INIT([Java Freeciv Protocol Implementation], [0.0.2.99])

AM_INIT_AUTOMAKE([-Wall -Werror foreign])

AC_CONFIG_MACRO_DIR([m4])

AX_PROG_JAVA
AX_PROG_JAVAC
AX_PROG_JAR

AX_PROG_SCALA

AX_COMPARE_VERSION($SCALA_VERSION, [lt], [2.9],
  AC_MSG_ERROR([Scala version >= 2.9.x requested but version $SCALA_VERSION found]))

AX_COMPARE_VERSION($SCALA_VERSION, [gt], [2.10.999],
  AC_MSG_ERROR([Scala version <= 2.10.x requested but version $SCALA_VERSION found]))

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

AC_ARG_VAR(VERSION_NAME,
  [a custom name for the generated Freeciv version support code])
AS_IF([test x"$VERSION_NAME" = x],
  AC_SUBST(VERSION_NAME, `${FREECIV_SOURCE_PATH}/fc_version`))

AC_ARG_VAR(VERSIONCONFIGURATION,
  [the location of a custom Freeciv version configuration])
AS_IF([test x"$VERSIONCONFIGURATION" = x],
  AC_SUBST(VERSIONCONFIGURATION, detect))

AC_ARG_VAR(LOG_TO, [Java logger to use])
AS_IF([test x"$LOG_TO" = x],
  AC_SUBST(LOG_TO, [java.util.logging.Logger.GLOBAL_LOGGER_NAME]))

AC_ARG_ENABLE(strict,
  AS_HELP_STRING(
   [--enable-strict],
   [stop the build if a problem is found (default is yes)]),
  AS_IF(test x"$enableval" != xno,
    AC_SUBST(IGNORE_ISSUES, false),
    AC_SUBST(IGNORE_ISSUES, true)),
  AC_SUBST(IGNORE_ISSUES, false))

AC_ARG_ENABLE(sourcecode,
  AS_HELP_STRING(
   [--enable-sourcecode],
   [copy the Freeciv code used to generate Java code (default is yes)]),
  AS_IF(test x"$enableval" != xno,
    AC_SUBST(NOT_DISTRIBUTED_WITH_FREECIV, true),
    AC_SUBST(NOT_DISTRIBUTED_WITH_FREECIV, false)),
  AC_SUBST(NOT_DISTRIBUTED_WITH_FREECIV, true))

# Where to put the generated code by default
AC_SUBST(GENERATED_SOURCE_FOLDER, FromFreeciv/$VERSION_NAME)
AC_SUBST(GENERATED_TEST_SOURCE_FOLDER, Tests/GeneratedTestPeers)

# TODO: Change to autotool's system
AC_SUBST(WORK_FOLDER, out)

# Generated compiled Java classes
AC_SUBST(COMPILED_CORE_FOLDER, ${WORK_FOLDER}/Core)
AC_SUBST(COMPILED_UTILS_FOLDER, ${WORK_FOLDER}/utils)
AC_SUBST(COMPILED_JAVA_GENERATOR_FOLDER, ${WORK_FOLDER}/JavaGenerator)
AC_SUBST(COMPILED_DEPENDENCY_FOLDER, ${WORK_FOLDER}/Dependency)
AC_SUBST(COMPILED_GENERATOR_FOLDER, ${WORK_FOLDER}/GeneratePackages)
AC_SUBST(COMPILED_TESTS_FOLDER, ${WORK_FOLDER}/Tests)
AC_SUBST(COMPILED_FROM_FREECIV_FOLDER, ${WORK_FOLDER}/VersionCode)
AC_SUBST(COMPILED_TEST_SIGN_IN_FOLDER, ${WORK_FOLDER}/SignInTest)
AC_SUBST(COMPILED_RECORDER_FOLDER, ${WORK_FOLDER}/FreecivRecorder)

# Generated jars
AC_SUBST(CORE_JAR, FCJCore.jar)
AC_SUBST(UTILS_JAR, FCJUtils.jar)
AC_SUBST(FREECIV_VERSION_JAR, FCJFreecivVersion-${VERSION_NAME}.jar)
AC_SUBST(RECORDER_JAR, FCJRecorder.jar)
AC_SUBST(SIGN_IN_JAR, FCJTestSignIn.jar)

AC_CONFIG_FILES([Makefile]
 [GeneratePackets/src/org/freeciv/packetgen/GeneratorDefaults.java]
 [inspectTrace]
 [inspectTrace.bat]
 [packetsExtract])
AC_OUTPUT
