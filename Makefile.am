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
	mkdir -p ${COMPILED_Core_FOLDER}
	${JAVAC} -d ${COMPILED_Core_FOLDER} ${Core_java_src}
	${JAR} cf @CORE_JAR@ -C ${COMPILED_Core_FOLDER} \.

${COMPILED_Core_TEST_FOLDER}/compile: @CORE_JAR@ ${Core_test_src}
	mkdir -p ${COMPILED_Core_TEST_FOLDER}
	${JAVAC} -cp @CORE_JAR@:${JUNIT_CPF} -d ${COMPILED_Core_TEST_FOLDER} ${Core_java_test_src}
	touch ${COMPILED_Core_TEST_FOLDER}/compile

${COMPILED_Core_TEST_FOLDER}/run: ${COMPILED_Core_TEST_FOLDER}/compile
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@CORE_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packet.ElementsLimitTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@CORE_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packet.HeaderTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@CORE_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packet.PacketTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@CORE_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.types.TestUnderstoodBitVector
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@CORE_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.utility.TestEternalZero
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@CORE_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.utility.TestValidation
	touch ${COMPILED_Core_TEST_FOLDER}/run

@JAVA_GEN_JAR@: @CORE_JAR@ @UTILS_JAR@ javaGen.manifest ${JavaGenerator_src}
	mkdir -p ${COMPILED_JavaGenerator_FOLDER}
	${JAVAC} -cp @CORE_JAR@:@UTILS_JAR@ -d ${COMPILED_JavaGenerator_FOLDER} ${JavaGenerator_java_src}
	${JAR} cfm @JAVA_GEN_JAR@ javaGen.manifest -C ${COMPILED_JavaGenerator_FOLDER} \.

${COMPILED_JavaGenerator_TEST_FOLDER}/compile: @JAVA_GEN_JAR@ ${JavaGenerator_test_src}
	mkdir -p ${COMPILED_JavaGenerator_TEST_FOLDER}
	${JAVAC} -cp @JAVA_GEN_JAR@:${JUNIT_CPF} -d ${COMPILED_JavaGenerator_TEST_FOLDER} ${JavaGenerator_java_test_src}
	touch ${COMPILED_JavaGenerator_TEST_FOLDER}/compile

${COMPILED_JavaGenerator_TEST_FOLDER}/run: ${COMPILED_JavaGenerator_TEST_FOLDER}/compile
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JAVA_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.CodeGenTest
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JAVA_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.TypedCodeTest
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JAVA_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeIR
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JAVA_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeCodeAtoms
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JAVA_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestPosition
	touch ${COMPILED_JavaGenerator_TEST_FOLDER}/run

@DEP_HAND_JAR@: workFolder ${DependencyHandler_src}
	mkdir -p ${COMPILED_DependencyHandler_FOLDER}
	${JAVAC} -d ${COMPILED_DependencyHandler_FOLDER} ${DependencyHandler_java_src}
	${JAR} cf @DEP_HAND_JAR@ -C ${COMPILED_DependencyHandler_FOLDER} \.

# @JAVA_GEN_JAR@ and @CODE_GEN_JAR@ are used by tests.
${COMPILED_DependencyHandler_TEST_FOLDER}/compile: @DEP_HAND_JAR@ @JAVA_GEN_JAR@ @CODE_GEN_JAR@ ${DependencyHandler_test_src}
	mkdir -p ${COMPILED_DependencyHandler_TEST_FOLDER}
	${JAVAC} -cp @DEP_HAND_JAR@:${JUNIT_CPF}:@JAVA_GEN_JAR@:@CODE_GEN_JAR@ -d ${COMPILED_DependencyHandler_TEST_FOLDER} ${DependencyHandler_java_test_src}
	touch ${COMPILED_DependencyHandler_TEST_FOLDER}/compile

${COMPILED_DependencyHandler_TEST_FOLDER}/run: ${COMPILED_DependencyHandler_TEST_FOLDER}/compile
	${JAVA} -ea -cp ${COMPILED_DependencyHandler_TEST_FOLDER}:@DEP_HAND_JAR@:${JUNIT_CPF}:@JAVA_GEN_JAR@:@CODE_GEN_JAR@ org.junit.runner.JUnitCore com.kvilhaugsvik.dependency.DependencyStoreTest
	touch ${COMPILED_DependencyHandler_TEST_FOLDER}/run

@CODE_GEN_JAR@: @CORE_JAR@ @UTILS_JAR@ @JAVA_GEN_JAR@ @DEP_HAND_JAR@ codeGen.manifest ${GeneratePackets_src} GeneratePackets/src/org/freeciv/packetgen/GeneratorDefaults.java packetsExtract
	chmod +x packetsExtract
	mkdir -p ${COMPILED_GeneratePackets_FOLDER}
	${JAVAC} -cp @CORE_JAR@:${SCALA_STDLIB_CPF}:@JAVA_GEN_JAR@:@DEP_HAND_JAR@ -d ${COMPILED_GeneratePackets_FOLDER} ${GeneratePackets_java_src_defaults}
	${SCALAC} -classpath ${COMPILED_GeneratePackets_FOLDER}:@JAVA_GEN_JAR@:@CORE_JAR@:@DEP_HAND_JAR@:@UTILS_JAR@ -d ${COMPILED_GeneratePackets_FOLDER} ${GeneratePackets_scala_src}
	${JAR} cfm @CODE_GEN_JAR@ codeGen.manifest -C ${COMPILED_GeneratePackets_FOLDER} \.

# @DEP_HAND_JAR@, @JAVA_GEN_JAR@ and @CORE_JAR@ are used by the packet generator
${COMPILED_GeneratePackets_TEST_FOLDER}/compile: @CODE_GEN_JAR@ @DEP_HAND_JAR@ ${GeneratePackets_test_src}
	mkdir -p ${COMPILED_GeneratePackets_TEST_FOLDER}
	${JAVAC} -cp @CODE_GEN_JAR@:${JUNIT_CPF} -d ${COMPILED_GeneratePackets_TEST_FOLDER} ${GeneratePackets_java_test_src}
	${SCALAC} -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF}:@DEP_HAND_JAR@:@JAVA_GEN_JAR@:@CORE_JAR@ -d ${COMPILED_GeneratePackets_TEST_FOLDER} ${GeneratePackets_scala_test_src}
	touch ${COMPILED_GeneratePackets_TEST_FOLDER}/compile

# Need the Scala standard library since running compiled Scala
${COMPILED_GeneratePackets_TEST_FOLDER}/run: ${COMPILED_GeneratePackets_TEST_FOLDER}/compile
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.EnumTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.FieldTypeTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.PacketTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.ParseSharedTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.PacketsDefParseTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSyntaxTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSemanticTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@CODE_GEN_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.FromCExtractorTest
	touch ${COMPILED_GeneratePackets_TEST_FOLDER}/run

sourceFromFreeciv: @CODE_GEN_JAR@ ${FREECIV_SOURCE_FILES}
	sh packetsExtract --source-code-location=${FREECIV_SOURCE_PATH} --version-information=${VERSIONCONFIGURATION} --packets-should-log-to=${LOG_TO} --ignore-problems=${IGNORE_ISSUES} --gpl-source=${NOT_DISTRIBUTED_WITH_FREECIV}
	touch sourceFromFreeciv

@FREECIV_VERSION_JAR@: sourceFromFreeciv
	mkdir -p ${COMPILED_FROM_FREECIV_FOLDER}
	${JAVAC} -d ${COMPILED_FROM_FREECIV_FOLDER} -cp @CORE_JAR@ `find ${GENERATED_SOURCE_FOLDER}/generated -iname "*.java"`
	${JAR} cf @FREECIV_VERSION_JAR@ -C ${COMPILED_FROM_FREECIV_FOLDER} \.

compileTestPeerGenerator: @CORE_JAR@ @CODE_GEN_JAR@ folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF} `find LegacyTests/ThatGenerateSourceCode -iname "*.java"`
	${SCALAC} -d ${COMPILED_TESTS_FOLDER} -classpath @CODE_GEN_JAR@:@JAVA_GEN_JAR@:@DEP_HAND_JAR@:@CORE_JAR@:@UTILS_JAR@:${JUNIT_CPF} `find LegacyTests/ThatGenerateSourceCode -iname "*.scala"`
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

runTestsOfGenerator: ${COMPILED_DependencyHandler_TEST_FOLDER}/run ${COMPILED_JavaGenerator_TEST_FOLDER}/run ${COMPILED_GeneratePackets_TEST_FOLDER}/run
	touch runTestsOfGenerator

compileTestGeneratedCode: compileTestPeers folderTestOut
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CODE_GEN_JAR@:@CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} `find LegacyTests/OfGeneratedCode/ -iname "*.java"`
	touch compileTestGeneratedCode

@SIGN_IN_JAR@: @FREECIV_VERSION_JAR@ @UTILS_JAR@ ${SignInTest_src} signIn.manifest testSignInToServer
	chmod +x testSignInToServer
	mkdir -p ${COMPILED_SignInTest_FOLDER}
	${JAVAC} -d ${COMPILED_SignInTest_FOLDER} -cp @CORE_JAR@:@UTILS_JAR@:@FREECIV_VERSION_JAR@ ${SignInTest_java_src}
	${JAR} cfem @SIGN_IN_JAR@ org.freeciv.test.SignInAndWait signIn.manifest -C ${COMPILED_SignInTest_FOLDER} \.

# not included in tests since it needs a running Freeciv server
runtestsignintoserver: @SIGN_IN_JAR@
	sh testSignInToServer && touch runtestsignintoserver

@RECORDER_JAR@: @FREECIV_VERSION_JAR@ @UTILS_JAR@ ${FreecivRecorder_src} fcr.manifest inspectTrace proxyRecorder playRecord
	chmod +x inspectTrace
	chmod +x proxyRecorder
	chmod +x playRecord
	mkdir -p ${COMPILED_FreecivRecorder_FOLDER}
	${JAVAC} -d ${COMPILED_FreecivRecorder_FOLDER} -cp @CORE_JAR@:@UTILS_JAR@:@FREECIV_VERSION_JAR@ ${FreecivRecorder_java_src}
	${JAR} cfm @RECORDER_JAR@ fcr.manifest -C ${COMPILED_FreecivRecorder_FOLDER} \.

# not included in tests since it needs a running Freeciv server and client
runProxyRecorer: @RECORDER_JAR@
	sh proxyRecorder && touch runProxyRecorer

compileConnectionTests: folderTestOut @CORE_JAR@
	${JAVAC} -d ${COMPILED_TESTS_FOLDER} -cp @CORE_JAR@:${JUNIT_CPF} `find LegacyTests/OfOtherCode/org/freeciv/connection/ -iname "*.java"`
	touch compileConnectionTests

runConnectionTests: compileConnectionTests
	${JAVA} -cp @CORE_JAR@:${JUNIT_CPF}:${COMPILED_TESTS_FOLDER} org.junit.runner.JUnitCore org.freeciv.connection.NetworkUninterpreted
	touch runConnectionTests

@UTILS_JAR@: workFolder @CORE_JAR@ ${Utility_src}
	mkdir -p ${COMPILED_Utility_FOLDER}
	${JAVAC} -cp @CORE_JAR@ -d ${COMPILED_Utility_FOLDER} ${Utility_java_src}
	${JAR} cf @UTILS_JAR@ -C ${COMPILED_Utility_FOLDER} \.
	touch @UTILS_JAR@

${COMPILED_Utility_TEST_FOLDER}/compile: @UTILS_JAR@ ${Utility_test_src}
	mkdir -p ${COMPILED_Utility_TEST_FOLDER}
	${JAVAC} -cp @UTILS_JAR@:${JUNIT_CPF} -d ${COMPILED_Utility_TEST_FOLDER} ${Utility_java_test_src}
	touch ${COMPILED_Utility_TEST_FOLDER}/compile

# run time depend on @CORE_JAR@
${COMPILED_Utility_TEST_FOLDER}/run: ${COMPILED_Utility_TEST_FOLDER}/compile  @CORE_JAR@
	${JAVA} -cp ${COMPILED_Utility_TEST_FOLDER}:@UTILS_JAR@:${JUNIT_CPF}:@CORE_JAR@ org.junit.runner.JUnitCore org.freeciv.utility.TestArgumentSettings
	${JAVA} -cp ${COMPILED_Utility_TEST_FOLDER}:@UTILS_JAR@:${JUNIT_CPF}:@CORE_JAR@ org.junit.runner.JUnitCore org.freeciv.utility.TestSettings
	touch ${COMPILED_Utility_TEST_FOLDER}/run

runTests: compileTestGeneratedCode runTestsOfGenerator ${COMPILED_Core_TEST_FOLDER}/run runConnectionTests ${COMPILED_Utility_TEST_FOLDER}/run
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
	rm -rf runConnectionTests compileConnectionTests
	rm -rf compileTestPeerGenerator
	rm -rf compileTestPeers
	rm -rf compileTestGeneratedCode
	rm -rf runTestPeerCreationAsTests
	rm -rf runTestsOfGenerator
	rm -rf runTests tests
	rm -rf folderTestOut ${COMPILED_TESTS_FOLDER}
	rm -rf ${GENERATED_TEST_SOURCE_FOLDER} sourceTestPeers
	rm -rf ${COMPILED_SignInTest_FOLDER}
	rm -f all
	rm -f code
	rm -rf runtestsignintoserver
	rm -rf @SIGN_IN_JAR@
	rm -rf ${COMPILED_FROM_FREECIV_FOLDER}
	rm -rf @FREECIV_VERSION_JAR@
	rm -rf runProxyRecorer
	rm -rf @RECORDER_JAR@
	rm -rf ${COMPILED_Core_FOLDER}
	rm -rf @CORE_JAR@
	rm -rf ${COMPILED_Utility_FOLDER}
	rm -rf @UTILS_JAR@
	rm -rf ${COMPILED_JavaGenerator_FOLDER} @JAVA_GEN_JAR@
	rm -rf ${COMPILED_DependencyHandler_FOLDER} @DEP_HAND_JAR@
	rm -rf ${COMPILED_DependencyHandler_TEST_FOLDER}
	rm -rf @CODE_GEN_JAR@
	rm -rf ${COMPILED_GeneratePackets_FOLDER}
	rm -rf ${GENERATED_SOURCE_FOLDER} sourceFromFreeciv
	rm -rf ${WORK_FOLDER} workFolder
