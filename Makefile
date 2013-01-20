JAVA ?= java
JAVAC ?= javac
JAR ?= jar
SCALA ?= scala
SCALAC ?= scalac
SCALALIB ?= /usr/share/java/scala-library.jar
JUNIT ?= /usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar

# Generate code for items that don't depend on them when
# items are missing in stead of aborting on missing items.
DEVMODE ?= false

# where to log errors
LOG_TO ?= "Logger.GLOBAL_LOGGER_NAME"

# What it should be generated from
# take instructions from trunk.xml
VERSIONCONFIGURATION ?= trunk.xml
# assume to be placed in a folder in the top level directory of Freeciv's source code unless told otherwise
FREECIV_SOURCE_PATH ?= ..

# Generated Java source code
GENERATED_SOURCE_FOLDER ?= BindingsUsers/GeneratedPackets
GENERATED_TEST_SOURCE_FOLDER ?= Tests/GeneratedTestPeers
GENERATORDEFAULTS ?= GeneratePackets/org/freeciv/packetgen/GeneratorDefaults.java

# Generated compiled Java classes
COMPILED_PROTOCOL_FOLDER ?= out/Protocol
COMPILED_GENERATOR_FOLDER ?= out/GeneratePackages
COMPILED_TESTS_FOLDER ?= out/Tests
COMPILED_BINDINGS_USERS_FOLDER ?= out/BindingsUsers

# Generated jars
PROTOCOL_DISTRIBUTION = FreecivProto.jar

all: tests compileTestSignInToServer compileProxyRecorder protojar
	touch all

tests: runTests
	touch tests

compileBasicProtocol:
	mkdir -p ${COMPILED_PROTOCOL_FOLDER}
	${JAVAC} -d ${COMPILED_PROTOCOL_FOLDER} `find Protocol -iname "*.java"`
	touch compileBasicProtocol

sourceDefaultsForGenerator:
	echo "package org.freeciv.packetgen;" > ${GENERATORDEFAULTS}
	echo "public class GeneratorDefaults {" >> ${GENERATORDEFAULTS}
	echo "  public static final String GENERATED_SOURCE_FOLDER = \"${GENERATED_SOURCE_FOLDER}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String GENERATED_TEST_SOURCE_FOLDER = \"${GENERATED_TEST_SOURCE_FOLDER}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String FREECIV_SOURCE_PATH = \"${FREECIV_SOURCE_PATH}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String VERSIONCONFIGURATION = \"${VERSIONCONFIGURATION}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String DEVMODE = \"${DEVMODE}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final String LOG_TO = \"${LOG_TO}\";" >> ${GENERATORDEFAULTS}
	echo "}" >>${GENERATORDEFAULTS}
	touch sourceDefaultsForGenerator

compileCodeGenerator: sourceDefaultsForGenerator compileBasicProtocol compileUtils
	mkdir -p ${COMPILED_GENERATOR_FOLDER}
	${JAVAC} -cp ${COMPILED_PROTOCOL_FOLDER}:${SCALALIB} -d ${COMPILED_GENERATOR_FOLDER} `find GeneratePackets -iname "*.java"`
	${SCALAC} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER} -d ${COMPILED_GENERATOR_FOLDER} `find GeneratePackets -iname "*.scala"`
	echo "${SCALA} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER} org.freeciv.packetgen.GeneratePackets \"\$$@\"" > compileCodeGenerator
	chmod +x compileCodeGenerator || rm compileCodeGenerator

sourceFromFreeciv: compileCodeGenerator
	sh compileCodeGenerator --source-code-location=${FREECIV_SOURCE_PATH} --version-information=${VERSIONCONFIGURATION} --packets-should-log-to=${LOG_TO} --ignore-problems=${DEVMODE}
	touch sourceFromFreeciv

compileFromFreeciv: sourceFromFreeciv
	${JAVAC} -d ${COMPILED_PROTOCOL_FOLDER} -cp ${COMPILED_PROTOCOL_FOLDER} `find ${GENERATED_SOURCE_FOLDER} -iname "*.java"`
	touch compileFromFreeciv

compileTestPeerGenerator: compileBasicProtocol compileCodeGenerator folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT} Tests/OfOtherCode/org/freeciv/packetgen/GenerateTest.java
	touch compileTestPeerGenerator

sourceTestPeers: compileTestPeerGenerator
	${JAVA} -cp ${COMPILED_TESTS_FOLDER}:${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER} org.freeciv.packetgen.GenerateTest ${GENERATED_TEST_SOURCE_FOLDER}
	touch sourceTestPeers

# not included in tests since make will run the code when generating test peers
runTestPeerCreationAsTests: compileTestPeerGenerator
	${JAVA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.GenerateTest
	touch runTestPeerCreationAsTests

compileTestPeers: compileCodeGenerator compileBasicProtocol sourceTestPeers
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_PROTOCOL_FOLDER} `find ${GENERATED_TEST_SOURCE_FOLDER} -iname "*.java"`
	touch compileTestPeers

protojar: compileFromFreeciv
	${JAR} cf ${PROTOCOL_DISTRIBUTION} ${COMPILED_PROTOCOL_FOLDER}
	touch protojar

folderTestOut:
	mkdir -p ${COMPILED_TESTS_FOLDER}
	touch folderTestOut

compileTestsOfGenerator: folderTestOut compileCodeGenerator
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.scala"`
	touch compileTestsOfGenerator

runTestsOfGenerator: compileTestsOfGenerator
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.javaGenerator.CodeGenTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.javaGenerator.TypedCodeTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.javaGenerator.representation.TestTreeIR
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.javaGenerator.representation.TestTreeCodeAtoms
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.EnumTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.FieldTypeTest
	${JAVA} -ea -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.DependencyStoreTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.ParseSharedTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.PacketsDefParseTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSyntaxTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSemanticTest
	${SCALA} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.FromCExtractorTest
	touch runTestsOfGenerator

compileTestGeneratedCode: compileTestPeers folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_GENERATOR_FOLDER}:${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} `find Tests/OfGeneratedCode/org/freeciv/test/ -iname "*.java"`
	touch compileTestGeneratedCode

compileBindingsUsers: compileFromFreeciv compileUtils
	mkdir -p ${COMPILED_BINDINGS_USERS_FOLDER}
	${JAVAC} -d ${COMPILED_BINDINGS_USERS_FOLDER} -cp ${COMPILED_PROTOCOL_FOLDER} `find BindingsUsers/Users -iname "*.java"`
	touch compileBindingsUsers

compileTestSignInToServer: compileBindingsUsers
	echo "${JAVA} -ea -cp ${COMPILED_PROTOCOL_FOLDER}:${COMPILED_BINDINGS_USERS_FOLDER} org.freeciv.test.SignInAndWait \"\$$@\"" > testSignInToServer
	chmod +x testSignInToServer
	touch compileTestSignInToServer

# not included in tests since it needs a running Freeciv server
runtestsignintoserver: compileTestSignInToServer
	sh testSignInToServer && touch runtestsignintoserver

compileProxyRecorder: compileBindingsUsers
	echo "${JAVA} -ea -cp ${COMPILED_PROTOCOL_FOLDER}:${COMPILED_BINDINGS_USERS_FOLDER} org.freeciv.test.ProxyRecorder \"\$$@\"" > proxyRecorder
	chmod +x proxyRecorder
	touch compileProxyRecorder

# not included in tests since it needs a running Freeciv server and client
runProxyRecorer: compileProxyRecorder
	sh proxyRecorder && touch runProxyRecorer

compileConnectionTests: folderTestOut compileBasicProtocol
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/connection/ -iname "*.java"`
	touch compileConnectionTests

runConnectionTests: compileConnectionTests
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.connection.NetworkUninterpreted
	touch runConnectionTests

compileUtils: compileBasicProtocol
	${JAVAC} -d ${COMPILED_PROTOCOL_FOLDER} `find Utility -iname "*.java"`
	touch compileUtils

compileUtilsTests: folderTestOut compileUtils
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/utility/ -iname "*.java"`
	touch compileUtilsTests

runUtilsTests: compileUtilsTests
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestArgumentSettings
	touch runUtilsTests

compilePacketTest: folderTestOut compileBasicProtocol
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT} `find Tests/OfOtherCode/org/freeciv/packet/ -iname "*.java"`
	touch compilePacketTest

runPacketTest: compilePacketTest
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.PacketTest
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.HeaderTest
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.ElementsLimitTest
	touch runPacketTest

runTests: compileTestGeneratedCode runTestsOfGenerator runPacketTest runConnectionTests runUtilsTests
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedPacketTest
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedEnumTest
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedStructTest
	${JAVA} -cp ${COMPILED_PROTOCOL_FOLDER}:${JUNIT}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.FieldTypeTests
	touch runTests

clean:
	rm -rf ${COMPILED_PROTOCOL_FOLDER} compileBasicProtocol
	rm -rf runPacketTest compilePacketTest
	rm -rf runConnectionTests compileConnectionTests
	rm -rf ${PACKETGENOUT} compileCodeGenerator
	rm -rf compileTestPeerGenerator
	rm -rf compileTestPeers
	rm -rf compileTestGeneratedCode
	rm -rf compileTestsOfGenerator
	rm -rf runTestPeerCreationAsTests
	rm -rf runTestsOfGenerator
	rm -rf runTests tests
	rm -rf folderTestOut ${COMPILED_TESTS_FOLDER}
	rm -rf ${GENERATED_TEST_SOURCE_FOLDER}/* sourceTestPeers
	rm -rf ${GENERATED_SOURCE_FOLDER}
	rm -rf ${COMPILED_BINDINGS_USERS_FOLDER}
	rm -f ${PROTOCOL_DISTRIBUTION} protojar
	rm -f all
	rm -rf compileTestSignInToServer testSignInToServer runtestsignintoserver
	rm -rf ${PROTOCOL_DISTRIBUTION}
	rm -rf sourceFromFreeciv
	rm -rf compileFromFreeciv
	rm -rf compileBindingsUsers
	rm -rf compileProxyRecorder proxyRecorder runProxyRecorer
	rm -rf compileUtils compileUtilsTests runUtilsTests

distclean: clean
	rm -rf out ${GENERATED_TEST_SOURCE_FOLDER}
	rm -rf ${GENERATORDEFAULTS} sourceDefaultsForGenerator
