SCALA ?= scala
SCALALIB ?= /usr/share/java/scala-library.jar
JUNIT ?= /usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar

# Ignore issues that shouldn't be there in a release.
# All it controls for now is if missing required items in stead of aborting the
# code generation should be ignored so items that don't depend on them still
# are generated.
IGNORE_ISSUES ?= false

# where to log errors
LOG_TO ?= "java.util.logging.Logger.GLOBAL_LOGGER_NAME"

# What it should be generated from
# take instructions from trunk.xml
FC_CONF ?= trunk
VERSIONCONFIGURATION = GeneratePackets/config/${FC_CONF}.xml
# assume to be placed in a folder in the top level directory of Freeciv's source code unless told otherwise
FREECIV_SOURCE_PATH ?= ..

# Generated Java source code
GENERATED_SOURCE_FOLDER = FromFreeciv/${FC_CONF}
GENERATED_TEST_SOURCE_FOLDER ?= Tests/GeneratedTestPeers
GENERATORDEFAULTS ?= GeneratePackets/org/freeciv/packetgen/GeneratorDefaults.java

# Copy the Freeciv source code used to generate Java code to GENERATED_SOURCE_FOLDER
# This makes it easier to follow the GPL when distributing without having to mirror all of some Freeciv SVN snapshot
NOT_DISTRIBUTED_WITH_FREECIV ?= true

WORK_FOLDER ?= out
# Generated compiled Java classes
COMPILED_CORE_FOLDER = ${WORK_FOLDER}/Core
COMPILED_UTILS_FOLDER = ${WORK_FOLDER}/utils
COMPILED_JAVA_GENERATOR_FOLDER = ${WORK_FOLDER}/JavaGenerator
COMPILED_DEPENDENCY_FOLDER = ${WORK_FOLDER}/Dependency
COMPILED_GENERATOR_FOLDER = ${WORK_FOLDER}/GeneratePackages
COMPILED_TESTS_FOLDER = ${WORK_FOLDER}/Tests
COMPILED_FROM_FREECIV_FOLDER = ${WORK_FOLDER}/VersionCode
COMPILED_TEST_SIGN_IN_FOLDER = ${WORK_FOLDER}/SignInTest
COMPILED_RECORDER_FOLDER = ${WORK_FOLDER}/FreecivRecorder

# Generated jars
CORE_JAR = FCJCore.jar
UTILS_JAR = FCJUtils.jar
FREECIV_VERSION_JAR = FCJFreecivVersion-${FC_CONF}.jar
RECORDER_JAR = FCJRecorder.jar
SIGN_IN_JAR = FCJTestSignIn.jar

all: tests compileTestSignInToServer compileProxyRecorder
	touch all

code: scriptPacketsExtract scriptTestSignInToServer scriptRunProxyRecorder scriptRunPlayToServer scriptInspectTrace sourceDefaultsForGenerator sourceTestPeers sourceFromFreeciv
	touch code

tests: runTests
	touch tests

workFolder:
	mkdir -p ${WORK_FOLDER}
	touch workFolder

compileCore: workFolder
	mkdir -p ${COMPILED_CORE_FOLDER}
	${JAVAC} -d ${COMPILED_CORE_FOLDER} `find Core -iname "*.java"`
	${JAR} cf ${CORE_JAR} -C ${COMPILED_CORE_FOLDER} \.
	touch compileCore

compileJavaGenerator: compileCore compileUtils
	mkdir -p ${COMPILED_JAVA_GENERATOR_FOLDER}
	${JAVAC} -cp ${CORE_JAR}:${UTILS_JAR} -d ${COMPILED_JAVA_GENERATOR_FOLDER} `find JavaGenerator -iname "*.java"`
	touch compileJavaGenerator

compileDependency: workFolder
	mkdir -p ${COMPILED_DEPENDENCY_FOLDER}
	${JAVAC} -d ${COMPILED_DEPENDENCY_FOLDER} `find DependencyHandler -iname "*.java"`
	touch compileDependency

sourceDefaultsForGenerator:
	echo "package org.freeciv.packetgen;" > ${GENERATORDEFAULTS}
	echo "public class GeneratorDefaults {" >> ${GENERATORDEFAULTS}
	echo "  public static final String GENERATED_SOURCE_FOLDER = \"${GENERATED_SOURCE_FOLDER}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String GENERATED_TEST_SOURCE_FOLDER = \"${GENERATED_TEST_SOURCE_FOLDER}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String FREECIV_SOURCE_PATH = \"${FREECIV_SOURCE_PATH}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String VERSIONCONFIGURATION = \"${VERSIONCONFIGURATION}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final boolean IGNORE_ISSUES = ${IGNORE_ISSUES};" >> ${GENERATORDEFAULTS}
	echo "  public static final String LOG_TO = \"${LOG_TO}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final boolean NOT_DISTRIBUTED_WITH_FREECIV = ${NOT_DISTRIBUTED_WITH_FREECIV};" >> ${GENERATORDEFAULTS}
	echo "}" >>${GENERATORDEFAULTS}
	touch sourceDefaultsForGenerator

scriptPacketsExtract:
	echo "${SCALA} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR} org.freeciv.packetgen.GeneratePackets \"\$$@\"" > packetsExtract
	chmod +x packetsExtract
	touch scriptPacketsExtract

compileCodeGenerator: sourceDefaultsForGenerator compileCore compileUtils compileJavaGenerator scriptPacketsExtract compileDependency
	mkdir -p ${COMPILED_GENERATOR_FOLDER}
	${JAVAC} -cp ${CORE_JAR}:${SCALALIB}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER} -d ${COMPILED_GENERATOR_FOLDER} `find GeneratePackets -iname "*.java"`
	${SCALAC} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${UTILS_JAR} -d ${COMPILED_GENERATOR_FOLDER} `find GeneratePackets -iname "*.scala"`
	touch compileCodeGenerator

sourceFromFreeciv: compileCodeGenerator
	sh packetsExtract --source-code-location=${FREECIV_SOURCE_PATH} --version-information=${VERSIONCONFIGURATION} --packets-should-log-to=${LOG_TO} --ignore-problems=${IGNORE_ISSUES} --gpl-source=${NOT_DISTRIBUTED_WITH_FREECIV}
	touch sourceFromFreeciv

compileFromFreeciv: sourceFromFreeciv
	mkdir ${COMPILED_FROM_FREECIV_FOLDER}
	${JAVAC} -d ${COMPILED_FROM_FREECIV_FOLDER} -cp ${CORE_JAR} `find ${GENERATED_SOURCE_FOLDER}/generated -iname "*.java"`
	${JAR} cf ${FREECIV_VERSION_JAR} -C ${COMPILED_FROM_FREECIV_FOLDER} \.
	touch compileFromFreeciv

compileTestPeerGenerator: compileCore compileCodeGenerator folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT} `find Tests/ThatGenerateSourceCode -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT} `find Tests/ThatGenerateSourceCode -iname "*.scala"`
	touch compileTestPeerGenerator

sourceTestPeers: compileTestPeerGenerator
	${SCALA} -classpath ${COMPILED_TESTS_FOLDER}:${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR} org.freeciv.packetgen.UsingGenerator ${GENERATED_TEST_SOURCE_FOLDER}
	${JAVA} -cp ${COMPILED_TESTS_FOLDER}:${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR} org.freeciv.packetgen.FromEntetiesAlone ${GENERATED_TEST_SOURCE_FOLDER}
	touch sourceTestPeers

# not included in tests since make will run the code when generating test peers
runTestPeerCreationAsTests: compileTestPeerGenerator
	${JAVA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.FromEntetiesAlone
	touch runTestPeerCreationAsTests

compileTestPeers: compileCodeGenerator compileCore sourceTestPeers
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${CORE_JAR} `find ${GENERATED_TEST_SOURCE_FOLDER}/generated -iname "*.java"`
	touch compileTestPeers

folderTestOut: workFolder
	mkdir -p ${COMPILED_TESTS_FOLDER}
	touch folderTestOut

compileTestsOfGenerator: folderTestOut compileCodeGenerator
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${JUNIT} `find Tests/OfOtherCode/com/ -iname "*.java"`
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.scala"`
	touch compileTestsOfGenerator

runTestsOfGenerator: compileTestsOfGenerator
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.CodeGenTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER}:${UTILS_JAR} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.TypedCodeTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeIR
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeCodeAtoms
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestPosition
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.EnumTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${JUNIT}:${UTILS_JAR}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.FieldTypeTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${JUNIT}:${UTILS_JAR}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.PacketTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${CORE_JAR}:${JUNIT}:${UTILS_JAR}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.DependencyStoreTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${JUNIT}:${UTILS_JAR}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.ParseSharedTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.PacketsDefParseTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSyntaxTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSemanticTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${CORE_JAR}:${COMPILED_DEPENDENCY_FOLDER}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.FromCExtractorTest
	touch runTestsOfGenerator

compileTestGeneratedCode: compileTestPeers folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} `find Tests/OfGeneratedCode/ -iname "*.java"`
	touch compileTestGeneratedCode

compileTestSignInToServer: compileFromFreeciv compileUtils scriptTestSignInToServer
	mkdir -p ${COMPILED_TEST_SIGN_IN_FOLDER}
	${JAVAC} -d ${COMPILED_TEST_SIGN_IN_FOLDER} -cp ${CORE_JAR}:${UTILS_JAR}:${FREECIV_VERSION_JAR} `find SignInTest -iname "*.java"`
	echo "Class-Path: ${CORE_JAR} ${UTILS_JAR} ${FREECIV_VERSION_JAR}" > ${WORK_FOLDER}/signIn.manifest
	${JAR} cfem ${SIGN_IN_JAR} org.freeciv.test.SignInAndWait ${WORK_FOLDER}/signIn.manifest -C ${COMPILED_TEST_SIGN_IN_FOLDER} \.
	touch compileTestSignInToServer

scriptTestSignInToServer:
	echo "${JAVA} -jar ${SIGN_IN_JAR} %*" > testSignInToServer.bat
	echo "${JAVA} -jar ${SIGN_IN_JAR} \"\$$@\"" > testSignInToServer
	chmod +x testSignInToServer
	touch scriptTestSignInToServer

# not included in tests since it needs a running Freeciv server
runtestsignintoserver: compileTestSignInToServer
	sh testSignInToServer && touch runtestsignintoserver

scriptInspectTrace:
	echo "${JAVA} -ea -cp ${RECORDER_JAR};${FREECIV_VERSION_JAR} org.freeciv.recorder.traceFormat2.PrintTrace %*" > inspectTrace.bat
	echo "${JAVA} -ea -cp ${RECORDER_JAR}:${FREECIV_VERSION_JAR} org.freeciv.recorder.traceFormat2.PrintTrace \"\$$@\" | less" > inspectTrace
	chmod +x inspectTrace
	touch scriptInspectTrace

scriptRunProxyRecorder:
	echo "${JAVA} -ea -cp ${RECORDER_JAR};${FREECIV_VERSION_JAR} org.freeciv.recorder.ProxyRecorder %*" > proxyRecorder.bat
	echo "${JAVA} -ea -cp ${RECORDER_JAR}:${FREECIV_VERSION_JAR} org.freeciv.recorder.ProxyRecorder \"\$$@\"" > proxyRecorder
	chmod +x proxyRecorder
	touch scriptRunProxyRecorder

scriptRunPlayToServer:
	echo "${JAVA} -ea -cp ${RECORDER_JAR};${FREECIV_VERSION_JAR} org.freeciv.recorder.PlayToServer %*" > playRecord.bat
	echo "${JAVA} -ea -cp ${RECORDER_JAR}:${FREECIV_VERSION_JAR} org.freeciv.recorder.PlayToServer \"\$$@\"" > playRecord
	chmod +x playRecord
	touch scriptRunPlayToServer

compileProxyRecorder: compileFromFreeciv compileUtils scriptRunProxyRecorder scriptRunPlayToServer scriptInspectTrace
	mkdir -p ${COMPILED_RECORDER_FOLDER}
	${JAVAC} -d ${COMPILED_RECORDER_FOLDER} -cp ${CORE_JAR}:${UTILS_JAR}:${FREECIV_VERSION_JAR} `find FreecivRecorder/src -iname "*.java"`
	echo "Class-Path: ${CORE_JAR} ${UTILS_JAR}" > ${WORK_FOLDER}/fcr.manifest
	${JAR} cfm ${RECORDER_JAR} ${WORK_FOLDER}/fcr.manifest -C ${COMPILED_RECORDER_FOLDER} \.
	touch compileProxyRecorder

# not included in tests since it needs a running Freeciv server and client
runProxyRecorer: compileProxyRecorder
	sh proxyRecorder && touch runProxyRecorer

compileConnectionTests: folderTestOut compileCore
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${CORE_JAR}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/connection/ -iname "*.java"`
	touch compileConnectionTests

runConnectionTests: compileConnectionTests
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.connection.NetworkUninterpreted
	touch runConnectionTests

compileUtils: workFolder compileCore
	mkdir ${COMPILED_UTILS_FOLDER}
	${JAVAC} -cp ${CORE_JAR} -d ${COMPILED_UTILS_FOLDER} `find Utility -iname "*.java"`
	${JAR} cf ${UTILS_JAR} -C ${COMPILED_UTILS_FOLDER} \.
	touch compileUtils

compileUtilsTests: folderTestOut compileUtils
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${CORE_JAR}:${UTILS_JAR}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/utility/ -iname "*.java"`
	touch compileUtilsTests

runUtilsTests: compileUtilsTests
	${JAVA} -cp ${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestArgumentSettings
	${JAVA} -cp ${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestSettings
	${JAVA} -cp ${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestValidation
	${JAVA} -cp ${CORE_JAR}:${UTILS_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestEternalZero
	touch runUtilsTests

compilePacketTest: folderTestOut compileCore
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${CORE_JAR}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/packet/ -iname "*.java"`
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${CORE_JAR}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/types/ -iname "*.java"`
	touch compilePacketTest

runPacketTest: compilePacketTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.PacketTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.HeaderTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.ElementsLimitTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.types.TestUnderstoodBitVector
	touch runPacketTest

runTests: compileTestGeneratedCode runTestsOfGenerator runPacketTest runConnectionTests runUtilsTests
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.GeneratedUsingEntetiesAlone
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.GeneratedUsingFullGenerator
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.ProtoData
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedEnumTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedStructTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedBVTest
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.FieldTypeTests
	${JAVA} -cp ${CORE_JAR}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.IsolatedBugCausers
	touch runTests

clean-local:
	rm -rf runPacketTest compilePacketTest
	rm -rf runConnectionTests compileConnectionTests
	rm -rf compileTestPeerGenerator
	rm -rf compileTestPeers
	rm -rf compileTestGeneratedCode
	rm -rf compileTestsOfGenerator
	rm -rf runTestPeerCreationAsTests
	rm -rf runTestsOfGenerator
	rm -rf runTests tests
	rm -rf folderTestOut ${COMPILED_TESTS_FOLDER}
	rm -rf ${GENERATED_TEST_SOURCE_FOLDER} sourceTestPeers
	rm -rf ${COMPILED_TEST_SIGN_IN_FOLDER}
	rm -f all
	rm -f code
	rm -rf compileTestSignInToServer testSignInToServer testSignInToServer.bat runtestsignintoserver
	rm -rf ${SIGN_IN_JAR}
	rm -rf ${COMPILED_FROM_FREECIV_FOLDER} compileFromFreeciv
	rm -rf ${FREECIV_VERSION_JAR}
	rm -rf compileProxyRecorder proxyRecorder.bat proxyRecorder runProxyRecorer ${WORK_FOLDER}/fcr.manifest
	rm -rf ${RECORDER_JAR}
	rm -f scriptRunProxyRecorder scriptTestSignInToServer
	rm -f scriptRunPlayToServer playRecord.bat playRecord
	rm -rf compileUtilsTests runUtilsTests
	rm -rf scriptInspectTrace inspectTrace.bat inspectTrace

distclean-local: clean
	rm -rf ${GENERATORDEFAULTS} sourceDefaultsForGenerator
	rm -rf ${COMPILED_CORE_FOLDER} compileCore
	rm -rf ${CORE_JAR}
	rm -rf ${COMPILED_UTILS_FOLDER} compileUtils
	rm -rf ${UTILS_JAR}
	rm -rf ${COMPILED_JAVA_GENERATOR_FOLDER} compileJavaGenerator
	rm -rf ${COMPILED_DEPENDENCY_FOLDER} compileDependency
	rm -rf packetsExtract scriptPacketsExtract
	rm -rf ${PACKETGENOUT} compileCodeGenerator
	rm -rf ${GENERATED_SOURCE_FOLDER} sourceFromFreeciv
	rm -rf ${WORK_FOLDER} workFolder
