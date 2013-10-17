ACLOCAL_AMFLAGS = -I m4

include $(top_srcdir)/DependencyHandler/source_code.list
include $(top_srcdir)/Core/source_code.list
include $(top_srcdir)/Utility/source_code.list
include $(top_srcdir)/JavaGenerator/source_code.list
include $(top_srcdir)/GeneratePackets/source_code.list
include $(top_srcdir)/FreecivRecorder/source_code.list
include $(top_srcdir)/SignInTest/source_code.list

# GeneratorDefaults.java is generated during ./configure
GeneratePackets_java_src_defaults=${GeneratePackets_java_src} \
	GeneratePackets/src/org/freeciv/packetgen/GeneratorDefaults.java

all: tests @SIGN_IN_JAR@ @RECORDER_JAR@
	touch all

code: sourceTestPeers sourceFromFreeciv
	touch code

tests: runTests
	touch tests

workFolder:
	mkdir -p ${WORK_FOLDER}
	touch workFolder

@CORE_JAR@: workFolder ${Core_src}
	mkdir -p ${COMPILED_CORE_FOLDER}
	${JAVAC} -d ${COMPILED_CORE_FOLDER} ${Core_java_src}
	${JAR} cf @CORE_JAR@ -C ${COMPILED_CORE_FOLDER} \.

@JAVA_GEN_JAR@: @CORE_JAR@ @UTILS_JAR@ javaGen.manifest ${JavaGenerator_src}
	mkdir -p ${COMPILED_JAVA_GENERATOR_FOLDER}
	${JAVAC} -cp @CORE_JAR@:@UTILS_JAR@ -d ${COMPILED_JAVA_GENERATOR_FOLDER} ${JavaGenerator_java_src}
	${JAR} cfm @JAVA_GEN_JAR@ javaGen.manifest -C ${COMPILED_JAVA_GENERATOR_FOLDER} \.

@DEP_HAND_JAR@: workFolder ${DependencyHandler_src}
	mkdir -p ${COMPILED_DEPENDENCY_FOLDER}
	${JAVAC} -d ${COMPILED_DEPENDENCY_FOLDER} ${DependencyHandler_java_src}
	${JAR} cf @DEP_HAND_JAR@ -C ${COMPILED_DEPENDENCY_FOLDER} \.

@CODE_GEN_JAR@: @CORE_JAR@ @UTILS_JAR@ @JAVA_GEN_JAR@ @DEP_HAND_JAR@ codeGen.manifest ${GeneratePackets_src} GeneratePackets/src/org/freeciv/packetgen/GeneratorDefaults.java packetsExtract
	chmod +x packetsExtract
	mkdir -p ${COMPILED_GENERATOR_FOLDER}
	${JAVAC} -cp @CORE_JAR@:${SCALA_STDLIB_CPF}:@JAVA_GEN_JAR@:@DEP_HAND_JAR@ -d ${COMPILED_GENERATOR_FOLDER} ${GeneratePackets_java_src_defaults}
	${SCALAC} -classpath ${COMPILED_GENERATOR_FOLDER}:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:@UTILS_JAR@ -d ${COMPILED_GENERATOR_FOLDER} ${GeneratePackets_scala_src}
	${JAR} cfm @CODE_GEN_JAR@ codeGen.manifest -C ${COMPILED_GENERATOR_FOLDER} \.

sourceFromFreeciv: @CODE_GEN_JAR@
	sh packetsExtract --source-code-location=${FREECIV_SOURCE_PATH} --version-information=${VERSIONCONFIGURATION} --packets-should-log-to=${LOG_TO} --ignore-problems=${IGNORE_ISSUES} --gpl-source=${NOT_DISTRIBUTED_WITH_FREECIV}
	touch sourceFromFreeciv

@FREECIV_VERSION_JAR@: sourceFromFreeciv
	mkdir -p ${COMPILED_FROM_FREECIV_FOLDER}
	${JAVAC} -d ${COMPILED_FROM_FREECIV_FOLDER} -cp @CORE_JAR@ `find ${GENERATED_SOURCE_FOLDER}/generated -iname "*.java"`
	${JAR} cf @FREECIV_VERSION_JAR@ -C ${COMPILED_FROM_FREECIV_FOLDER} \.

compileTestPeerGenerator: @CORE_JAR@ @CODE_GEN_JAR@ folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF} `find Tests/ThatGenerateSourceCode -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF} `find Tests/ThatGenerateSourceCode -iname "*.scala"`
	touch compileTestPeerGenerator

sourceTestPeers: compileTestPeerGenerator
	${SCALA} -classpath ${COMPILED_TESTS_FOLDER}:@CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@ org.freeciv.packetgen.UsingGenerator ${GENERATED_TEST_SOURCE_FOLDER}
	${JAVA} -cp ${COMPILED_TESTS_FOLDER}:@CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@ org.freeciv.packetgen.FromEntetiesAlone ${GENERATED_TEST_SOURCE_FOLDER}
	touch sourceTestPeers

# not included in tests since make will run the code when generating test peers
runTestPeerCreationAsTests: compileTestPeerGenerator
	${JAVA} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.FromEntetiesAlone
	touch runTestPeerCreationAsTests

compileTestPeers: @CODE_GEN_JAR@ @CORE_JAR@ sourceTestPeers
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CORE_JAR@ `find ${GENERATED_TEST_SOURCE_FOLDER}/generated -iname "*.java"`
	touch compileTestPeers

folderTestOut: workFolder
	mkdir -p ${COMPILED_TESTS_FOLDER}
	touch folderTestOut

compileTestsOfGenerator: folderTestOut @CODE_GEN_JAR@
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:${JUNIT_CPF} `find Tests/OfOtherCode/com/ -iname "*.java"`
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:${JUNIT_CPF} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} `find Tests/OfOtherCode/org/freeciv/packetgen/ -iname "*.scala"`
	touch compileTestsOfGenerator

runTestsOfGenerator: compileTestsOfGenerator
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.CodeGenTest
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER}:@UTILS_JAR@ org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.TypedCodeTest
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeIR
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeCodeAtoms
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestPosition
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.EnumTest
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:${JUNIT_CPF}:@UTILS_JAR@:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.FieldTypeTest
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:${JUNIT_CPF}:@UTILS_JAR@:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.PacketTest
	${JAVA} -ea -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:${JUNIT_CPF}:@UTILS_JAR@:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.DependencyStoreTest
	${SCALA} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:${JUNIT_CPF}:@UTILS_JAR@:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.ParseSharedTest
	${SCALA} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.PacketsDefParseTest
	${SCALA} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSyntaxTest
	${SCALA} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSemanticTest
	${SCALA} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.FromCExtractorTest
	touch runTestsOfGenerator

compileTestGeneratedCode: compileTestPeers folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CODE_GEN_JAR@:@CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} `find Tests/OfGeneratedCode/ -iname "*.java"`
	touch compileTestGeneratedCode

@SIGN_IN_JAR@: @FREECIV_VERSION_JAR@ @UTILS_JAR@ ${SignInTest_src} signIn.manifest testSignInToServer
	chmod +x testSignInToServer
	mkdir -p ${COMPILED_TEST_SIGN_IN_FOLDER}
	${JAVAC} -d ${COMPILED_TEST_SIGN_IN_FOLDER} -cp @CORE_JAR@:@UTILS_JAR@:@FREECIV_VERSION_JAR@ ${SignInTest_java_src}
	${JAR} cfem @SIGN_IN_JAR@ org.freeciv.test.SignInAndWait signIn.manifest -C ${COMPILED_TEST_SIGN_IN_FOLDER} \.

# not included in tests since it needs a running Freeciv server
runtestsignintoserver: @SIGN_IN_JAR@
	sh testSignInToServer && touch runtestsignintoserver

@RECORDER_JAR@: @FREECIV_VERSION_JAR@ @UTILS_JAR@ ${FreecivRecorder_src} fcr.manifest inspectTrace proxyRecorder playRecord
	chmod +x inspectTrace
	chmod +x proxyRecorder
	chmod +x playRecord
	mkdir -p ${COMPILED_RECORDER_FOLDER}
	${JAVAC} -d ${COMPILED_RECORDER_FOLDER} -cp @CORE_JAR@:@UTILS_JAR@:@FREECIV_VERSION_JAR@ ${FreecivRecorder_java_src}
	${JAR} cfm @RECORDER_JAR@ fcr.manifest -C ${COMPILED_RECORDER_FOLDER} \.

# not included in tests since it needs a running Freeciv server and client
runProxyRecorer: @RECORDER_JAR@
	sh proxyRecorder && touch runProxyRecorer

compileConnectionTests: folderTestOut @CORE_JAR@
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CORE_JAR@:${JUNIT_CPF} `find Tests/OfOtherCode/org/freeciv/connection/ -iname "*.java"`
	touch compileConnectionTests

runConnectionTests: compileConnectionTests
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.connection.NetworkUninterpreted
	touch runConnectionTests

@UTILS_JAR@: workFolder @CORE_JAR@ ${Utility_src}
	mkdir -p ${COMPILED_UTILS_FOLDER}
	${JAVAC} -cp @CORE_JAR@ -d ${COMPILED_UTILS_FOLDER} ${Utility_java_src}
	${JAR} cf @UTILS_JAR@ -C ${COMPILED_UTILS_FOLDER} \.
	touch @UTILS_JAR@

compileUtilsTests: folderTestOut @UTILS_JAR@
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF} `find Tests/OfOtherCode/org/freeciv/utility/ -iname "*.java"`
	touch compileUtilsTests

runUtilsTests: compileUtilsTests
	${JAVA} -cp @CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestArgumentSettings
	${JAVA} -cp @CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestSettings
	${JAVA} -cp @CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestValidation
	${JAVA} -cp @CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.utility.TestEternalZero
	touch runUtilsTests

compilePacketTest: folderTestOut @CORE_JAR@
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CORE_JAR@:${JUNIT_CPF} `find Tests/OfOtherCode/org/freeciv/packet/ -iname "*.java"`
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CORE_JAR@:${JUNIT_CPF} `find Tests/OfOtherCode/org/freeciv/types/ -iname "*.java"`
	touch compilePacketTest

runPacketTest: compilePacketTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.PacketTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.HeaderTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.ElementsLimitTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.types.TestUnderstoodBitVector
	touch runPacketTest

runTests: compileTestGeneratedCode runTestsOfGenerator runPacketTest runConnectionTests runUtilsTests
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.GeneratedUsingEntetiesAlone
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.packet.GeneratedUsingFullGenerator
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.ProtoData
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedEnumTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedStructTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.GeneratedBVTest
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.FieldTypeTests
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.test.IsolatedBugCausers
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
	rm -rf runtestsignintoserver
	rm -rf @SIGN_IN_JAR@
	rm -rf ${COMPILED_FROM_FREECIV_FOLDER}
	rm -rf @FREECIV_VERSION_JAR@
	rm -rf runProxyRecorer
	rm -rf @RECORDER_JAR@
	rm -rf compileUtilsTests runUtilsTests
	rm -rf ${COMPILED_CORE_FOLDER}
	rm -rf @CORE_JAR@
	rm -rf ${COMPILED_UTILS_FOLDER}
	rm -rf @UTILS_JAR@
	rm -rf ${COMPILED_JAVA_GENERATOR_FOLDER} @JAVA_GEN_JAR@
	rm -rf ${COMPILED_DEPENDENCY_FOLDER} @DEP_HAND_JAR@
	rm -rf @CODE_GEN_JAR@
	rm -rf ${COMPILED_GENERATOR_FOLDER}
	rm -rf ${GENERATED_SOURCE_FOLDER} sourceFromFreeciv
	rm -rf ${WORK_FOLDER} workFolder
