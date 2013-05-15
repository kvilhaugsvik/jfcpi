JAVA ?= java
JAVAC ?= javac -target 1.6 -source 1.6
JAR ?= jar
SCALA ?= scala
SCALAC ?= scalac
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
VERSIONCONFIGURATION ?= GeneratePackets/config/trunk.xml
# assume to be placed in a folder in the top level directory of Freeciv's source code unless told otherwise
FREECIV_SOURCE_PATH ?= ..

# Generated Java source code
GENERATED_SOURCE_FOLDER ?= BindingsUsers/GeneratedPackets
GENERATED_TEST_SOURCE_FOLDER ?= Tests/GeneratedTestPeers
GENERATORDEFAULTS ?= GeneratePackets/org/freeciv/packetgen/GeneratorDefaults.java

# Copy the Freeciv source code used to generate Java code to GENERATED_SOURCE_FOLDER
# This makes it easier to follow the GPL when distributing without having to mirror all of some Freeciv SVN snapshot
NOT_DISTRIBUTED_WITH_FREECIV ?= true

# Generated compiled Java classes
COMPILED_CORE_FOLDER ?= out/Core
COMPILED_UTILS_FOLDER ?= out/utils
COMPILED_JAVA_GENERATOR_FOLDER ?= out/JavaGenerator
COMPILED_DEPENDENCY_FOLDER ?= out/Dependency
COMPILED_GENERATOR_FOLDER ?= out/GeneratePackages
COMPILED_TESTS_FOLDER ?= out/Tests
COMPILED_BINDINGS_USERS_FOLDER ?= out/BindingsUsers
COMPILED_RECORDER_FOLDER ?= out/FreecivRecorder

# Generated jars
PROTOCOL_DISTRIBUTION = FreecivProto.jar

all: tests compileTestSignInToServer compileProxyRecorder protojar
	touch all

code: scriptPacketsExtract scriptTestSignInToServer scriptRunProxyRecorder scriptRunPlayToServer scriptInspectTrace sourceDefaultsForGenerator sourceTestPeers sourceFromFreeciv
	touch code

tests: runTests
	touch tests

compileCore:
	mkdir -p ${COMPILED_CORE_FOLDER}
	${JAVAC} -d ${COMPILED_CORE_FOLDER} `find Core -iname "*.java"`
	touch compileCore

compileJavaGenerator: compileCore compileUtils
	mkdir -p ${COMPILED_JAVA_GENERATOR_FOLDER}
	${JAVAC} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER} -d ${COMPILED_JAVA_GENERATOR_FOLDER} `find JavaGenerator -iname "*.java"`
	touch compileJavaGenerator

compileDependency:
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
	echo "${SCALA} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.packetgen.GeneratePackets \"\$$@\"" > packetsExtract
	chmod +x packetsExtract
	touch scriptPacketsExtract

compileCodeGenerator: sourceDefaultsForGenerator compileCore compileUtils compileJavaGenerator scriptPacketsExtract compileDependency
	mkdir -p ${COMPILED_GENERATOR_FOLDER}
	${JAVAC} -cp ${COMPILED_CORE_FOLDER}:${SCALALIB}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER} -d ${COMPILED_GENERATOR_FOLDER} `find GeneratePackets -iname "*.java"`
	${SCALAC} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_UTILS_FOLDER} -d ${COMPILED_GENERATOR_FOLDER} `find GeneratePackets -iname "*.scala"`
	touch compileCodeGenerator

sourceFromFreeciv: compileCodeGenerator
	sh packetsExtract --source-code-location=${FREECIV_SOURCE_PATH} --version-information=${VERSIONCONFIGURATION} --packets-should-log-to=${LOG_TO} --ignore-problems=${IGNORE_ISSUES} --gpl-source=${NOT_DISTRIBUTED_WITH_FREECIV}
	touch sourceFromFreeciv

compileFromFreeciv: sourceFromFreeciv
	${JAVAC} -d ${COMPILED_CORE_FOLDER} -cp ${COMPILED_CORE_FOLDER} `find ${GENERATED_SOURCE_FOLDER} -iname "*.java"`
	touch compileFromFreeciv

compileTestPeerGenerator: compileCore compileCodeGenerator folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT} Tests/OfOtherCode/org/freeciv/packetgen/GenerateTest.java
	touch compileTestPeerGenerator

sourceTestPeers: compileTestPeerGenerator
	${JAVA} -cp ${COMPILED_TESTS_FOLDER}:${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.packetgen.GenerateTest ${GENERATED_TEST_SOURCE_FOLDER}
	touch sourceTestPeers

# not included in tests since make will run the code when generating test peers
runTestPeerCreationAsTests: compileTestPeerGenerator
	${JAVA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.GenerateTest
	touch runTestPeerCreationAsTests

compileTestPeers: compileCodeGenerator compileCore sourceTestPeers
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_CORE_FOLDER} `find ${GENERATED_TEST_SOURCE_FOLDER} -iname "*.java"`
	touch compileTestPeers

protojar: compileFromFreeciv
	${JAR} cf ${PROTOCOL_DISTRIBUTION} ${COMPILED_CORE_FOLDER}
	touch protojar

folderTestOut:
	mkdir -p ${COMPILED_TESTS_FOLDER}
	touch folderTestOut

compileTestsOfGenerator: folderTestOut compileCodeGenerator
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT} `find Tests/OfOtherCode/com/ -iname "*.java"`
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.scala"`
	touch compileTestsOfGenerator

runTestsOfGenerator: compileTestsOfGenerator
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.CodeGenTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER}:${COMPILED_UTILS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.TypedCodeTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeIR
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeCodeAtoms
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestPosition
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.EnumTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.FieldTypeTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.PacketTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.DependencyStoreTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.ParseSharedTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.PacketsDefParseTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSyntaxTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSemanticTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_JAVA_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${COMPILED_DEPENDENCY_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.FromCExtractorTest
	touch runTestsOfGenerator

compileTestGeneratedCode: compileTestPeers folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} `find Tests/OfGeneratedCode/ -iname "*.java"`
	touch compileTestGeneratedCode

compileBindingsUsers: compileFromFreeciv compileUtils
	mkdir -p ${COMPILED_BINDINGS_USERS_FOLDER}
	${JAVAC} -d ${COMPILED_BINDINGS_USERS_FOLDER} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER} `find BindingsUsers/Users -iname "*.java"`
	touch compileBindingsUsers

scriptTestSignInToServer:
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER};${COMPILED_BINDINGS_USERS_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.test.SignInAndWait %*" > testSignInToServer.bat
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER}:${COMPILED_BINDINGS_USERS_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.test.SignInAndWait \"\$$@\"" > testSignInToServer
	chmod +x testSignInToServer
	touch scriptTestSignInToServer

compileTestSignInToServer: compileBindingsUsers scriptTestSignInToServer

# not included in tests since it needs a running Freeciv server
runtestsignintoserver: compileTestSignInToServer
	sh testSignInToServer && touch runtestsignintoserver

scriptInspectTrace:
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER};${COMPILED_RECORDER_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.recorder.traceFormat2.PrintTrace %*" > inspectTrace.bat
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER}:${COMPILED_RECORDER_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.recorder.traceFormat2.PrintTrace \"\$$@\" | less" > inspectTrace
	chmod +x inspectTrace
	touch scriptInspectTrace

scriptRunProxyRecorder:
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER};${COMPILED_RECORDER_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.recorder.ProxyRecorder %*" > proxyRecorder.bat
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER}:${COMPILED_RECORDER_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.recorder.ProxyRecorder \"\$$@\"" > proxyRecorder
	chmod +x proxyRecorder
	touch scriptRunProxyRecorder

scriptRunPlayToServer:
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER};${COMPILED_RECORDER_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.recorder.PlayToServer %*" > playRecord.bat
	echo "${JAVA} -ea -cp ${COMPILED_CORE_FOLDER}:${COMPILED_RECORDER_FOLDER}:${COMPILED_UTILS_FOLDER} org.freeciv.recorder.PlayToServer \"\$$@\"" > playRecord
	chmod +x playRecord
	touch scriptRunPlayToServer

compileProxyRecorder: compileFromFreeciv compileUtils scriptRunProxyRecorder scriptRunPlayToServer scriptInspectTrace
	mkdir -p ${COMPILED_RECORDER_FOLDER}
	${JAVAC} -d ${COMPILED_RECORDER_FOLDER} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER} `find FreecivRecorder/src -iname "*.java"`
	touch compileProxyRecorder

# not included in tests since it needs a running Freeciv server and client
runProxyRecorer: compileProxyRecorder
	sh proxyRecorder && touch runProxyRecorer

compileConnectionTests: folderTestOut compileCore
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_CORE_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/connection/ -iname "*.java"`
	touch compileConnectionTests

runConnectionTests: compileConnectionTests
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.connection.NetworkUninterpreted
	touch runConnectionTests

compileUtils: compileCore
	mkdir ${COMPILED_UTILS_FOLDER}
	${JAVAC} -cp ${COMPILED_CORE_FOLDER} -d ${COMPILED_UTILS_FOLDER} `find Utility -iname "*.java"`
	touch compileUtils

compileUtilsTests: folderTestOut compileUtils
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/utility/ -iname "*.java"`
	touch compileUtilsTests

runUtilsTests: compileUtilsTests
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestArgumentSettings
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestSettings
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestValidation
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${COMPILED_UTILS_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestEternalZero
	touch runUtilsTests

compilePacketTest: folderTestOut compileCore
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_CORE_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/packet/ -iname "*.java"`
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_CORE_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/types/ -iname "*.java"`
	touch compilePacketTest

runPacketTest: compilePacketTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.PacketTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.HeaderTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.ElementsLimitTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.types.TestUnderstoodBitVector
	touch runPacketTest

runTests: compileTestGeneratedCode runTestsOfGenerator runPacketTest runConnectionTests runUtilsTests
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.GeneratedPacketTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedEnumTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedStructTest
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.FieldTypeTests
	${JAVA} -cp ${COMPILED_CORE_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.IsolatedBugCausers
	touch runTests

clean:
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
	rm -rf ${COMPILED_BINDINGS_USERS_FOLDER}
	rm -f ${PROTOCOL_DISTRIBUTION} protojar
	rm -f all
	rm -f code
	rm -rf compileTestSignInToServer testSignInToServer testSignInToServer.bat runtestsignintoserver
	rm -rf compileFromFreeciv
	rm -rf compileBindingsUsers
	rm -rf compileProxyRecorder proxyRecorder.bat proxyRecorder runProxyRecorer
	rm -f scriptRunProxyRecorder scriptTestSignInToServer
	rm -f scriptRunPlayToServer playRecord.bat playRecord
	rm -rf compileUtilsTests runUtilsTests
	rm -rf scriptInspectTrace inspectTrace.bat inspectTrace

distclean: clean
	rm -rf ${GENERATORDEFAULTS} sourceDefaultsForGenerator
	rm -rf ${COMPILED_CORE_FOLDER} compileCore
	rm -rf ${COMPILED_UTILS_FOLDER} compileUtils
	rm -rf ${COMPILED_JAVA_GENERATOR_FOLDER} compileJavaGenerator
	rm -rf ${COMPILED_DEPENDENCY_FOLDER} compileDependency
	rm -rf packetsExtract scriptPacketsExtract
	rm -rf ${PACKETGENOUT} compileCodeGenerator
	rm -rf ${GENERATED_SOURCE_FOLDER} sourceFromFreeciv
	rm -rf out
