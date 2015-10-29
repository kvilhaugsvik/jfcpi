ACLOCAL_AMFLAGS = -I m4

# GeneratorDefaults.java is generated during ./configure
GeneratePackets_java_src_defaults=${GeneratePackets_java_src} \
	GeneratePackets/src/org/freeciv/packetgen/GeneratorDefaults.java

all: tests @SignInTest_JAR@ @FreecivRecorder_JAR@

source: sourceTestPeers sourceFromFreeciv
	touch source

tests: runTests
	touch tests

workFolder:
	mkdir -p ${WORK_FOLDER}
	touch workFolder

@Core_JAR@: workFolder ${Core_src}
	mkdir -p ${COMPILED_Core_FOLDER}
	${JAVAC} -d ${COMPILED_Core_FOLDER} ${Core_java_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_Core_FOLDER} ${Core_java_src}
endif
	${JAR} cf @Core_JAR@ -C ${COMPILED_Core_FOLDER} \.

@COMPILED_Core_TEST_FOLDER@/compile: @Core_JAR@ ${Core_test_src}
	mkdir -p ${COMPILED_Core_TEST_FOLDER}
	${JAVAC} -cp @Core_JAR@:${JUNIT_CPF} -d ${COMPILED_Core_TEST_FOLDER} ${Core_java_test_src}
	touch ${COMPILED_Core_TEST_FOLDER}/compile

@COMPILED_Core_TEST_FOLDER@/run: @COMPILED_Core_TEST_FOLDER@/compile
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packet.ElementsLimitTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packet.HeaderTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packet.PacketTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.types.TestUnderstoodBitVector
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.utility.TestEternalZero
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.utility.TestValidation
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.connection.HeaderDataTest
	${JAVA} -ea -cp ${COMPILED_Core_TEST_FOLDER}:@Core_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.connection.NetworkUninterpreted
	touch ${COMPILED_Core_TEST_FOLDER}/run

@JavaGenerator_JAR@: @Core_JAR@ @Utility_JAR@ javaGen.manifest ${JavaGenerator_src}
	mkdir -p ${COMPILED_JavaGenerator_FOLDER}
	${JAVAC} -cp @Core_JAR@:@Utility_JAR@ -d ${COMPILED_JavaGenerator_FOLDER} ${JavaGenerator_java_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_JavaGenerator_FOLDER} ${JavaGenerator_java_src}
endif
	${JAR} cfm @JavaGenerator_JAR@ javaGen.manifest -C ${COMPILED_JavaGenerator_FOLDER} \.

@COMPILED_JavaGenerator_TEST_FOLDER@/compile: @JavaGenerator_JAR@ ${JavaGenerator_test_src}
	mkdir -p ${COMPILED_JavaGenerator_TEST_FOLDER}
	${JAVAC} -cp @JavaGenerator_JAR@:${JUNIT_CPF} -d ${COMPILED_JavaGenerator_TEST_FOLDER} ${JavaGenerator_java_test_src}
	touch ${COMPILED_JavaGenerator_TEST_FOLDER}/compile

@COMPILED_JavaGenerator_TEST_FOLDER@/run: @COMPILED_JavaGenerator_TEST_FOLDER@/compile
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JavaGenerator_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.CodeGenTest
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JavaGenerator_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.TypedCodeTest
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JavaGenerator_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeIR
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JavaGenerator_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestTreeCodeAtoms
	${JAVA} -ea -cp ${COMPILED_JavaGenerator_TEST_FOLDER}:@JavaGenerator_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore com.kvilhaugsvik.javaGenerator.representation.TestPosition
	touch ${COMPILED_JavaGenerator_TEST_FOLDER}/run

@DependencyHandler_JAR@: workFolder ${DependencyHandler_src}
	mkdir -p ${COMPILED_DependencyHandler_FOLDER}
	${JAVAC} -d ${COMPILED_DependencyHandler_FOLDER} ${DependencyHandler_java_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_DependencyHandler_FOLDER} ${DependencyHandler_java_src}
endif
	${JAR} cf @DependencyHandler_JAR@ -C ${COMPILED_DependencyHandler_FOLDER} \.

# @JavaGenerator_JAR@ and @GeneratePackets_JAR@ are used by tests.
@COMPILED_DependencyHandler_TEST_FOLDER@/compile: @DependencyHandler_JAR@ @JavaGenerator_JAR@ @GeneratePackets_JAR@ ${DependencyHandler_test_src}
	mkdir -p ${COMPILED_DependencyHandler_TEST_FOLDER}
	${JAVAC} -cp @DependencyHandler_JAR@:${JUNIT_CPF}:@JavaGenerator_JAR@:@GeneratePackets_JAR@ -d ${COMPILED_DependencyHandler_TEST_FOLDER} ${DependencyHandler_java_test_src}
	touch ${COMPILED_DependencyHandler_TEST_FOLDER}/compile

@COMPILED_DependencyHandler_TEST_FOLDER@/run: @COMPILED_DependencyHandler_TEST_FOLDER@/compile
	${JAVA} -ea -cp ${COMPILED_DependencyHandler_TEST_FOLDER}:@DependencyHandler_JAR@:${JUNIT_CPF}:@JavaGenerator_JAR@:@GeneratePackets_JAR@ org.junit.runner.JUnitCore com.kvilhaugsvik.dependency.DependencyStoreTest
	touch ${COMPILED_DependencyHandler_TEST_FOLDER}/run

@GeneratePackets_JAR@: @Core_JAR@ @Utility_JAR@ @JavaGenerator_JAR@ @DependencyHandler_JAR@ codeGen.manifest ${GeneratePackets_src} GeneratePackets/src/org/freeciv/packetgen/GeneratorDefaults.java packetsExtract
	chmod +x packetsExtract
	mkdir -p ${COMPILED_GeneratePackets_FOLDER}
	${JAVAC} -cp @Core_JAR@:${SCALA_STDLIB_CPF}:@JavaGenerator_JAR@:@DependencyHandler_JAR@ -d ${COMPILED_GeneratePackets_FOLDER} ${GeneratePackets_java_src_defaults}
	${SCALAC} -classpath ${COMPILED_GeneratePackets_FOLDER}:@JavaGenerator_JAR@:@Core_JAR@:@DependencyHandler_JAR@:@Utility_JAR@ -d ${COMPILED_GeneratePackets_FOLDER} ${GeneratePackets_scala_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_GeneratePackets_FOLDER} -classpath @Core_JAR@:${SCALA_STDLIB_CPF}:@JavaGenerator_JAR@:@DependencyHandler_JAR@ ${GeneratePackets_java_src_defaults}
endif
	${JAR} cfm @GeneratePackets_JAR@ codeGen.manifest -C ${COMPILED_GeneratePackets_FOLDER} \.

# @DependencyHandler_JAR@, @JavaGenerator_JAR@ and @Core_JAR@ are used by the packet generator
@COMPILED_GeneratePackets_TEST_FOLDER@/compile: @GeneratePackets_JAR@ @DependencyHandler_JAR@ ${GeneratePackets_test_src}
	mkdir -p ${COMPILED_GeneratePackets_TEST_FOLDER}
	${JAVAC} -cp @GeneratePackets_JAR@:${JUNIT_CPF} -d ${COMPILED_GeneratePackets_TEST_FOLDER} ${GeneratePackets_java_test_src}
	${SCALAC} -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:@DependencyHandler_JAR@:@JavaGenerator_JAR@:@Core_JAR@ -d ${COMPILED_GeneratePackets_TEST_FOLDER} ${GeneratePackets_scala_test_src}
	touch ${COMPILED_GeneratePackets_TEST_FOLDER}/compile

# Need the Scala standard library since running compiled Scala
@COMPILED_GeneratePackets_TEST_FOLDER@/run: @COMPILED_GeneratePackets_TEST_FOLDER@/compile
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.TestGeneratePackets
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.EnumTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.FieldTypeTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.enteties.PacketTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.ParseSharedTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.PacketsDefParseTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSyntaxTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.CParserSemanticTest
	${JAVA} -ea -cp ${COMPILED_GeneratePackets_TEST_FOLDER}:@GeneratePackets_JAR@:${JUNIT_CPF}:${SCALA_STDLIB_CPF} org.junit.runner.JUnitCore org.freeciv.packetgen.parsing.FromCExtractorTest
	touch ${COMPILED_GeneratePackets_TEST_FOLDER}/run

sourceFromFreeciv: @GeneratePackets_JAR@ ${FREECIV_SOURCE_FILES}
	rm -rf ${GENERATED_SOURCE_FOLDER}
	sh packetsExtract --source-code-location=${FREECIV_SOURCE_PATH} --version-information=${VERSIONCONFIGURATION} --ignore-problems=${IGNORE_ISSUES} --gpl-source=${NOT_DISTRIBUTED_WITH_FREECIV}
	touch sourceFromFreeciv

@FREECIV_VERSION_JAR@: sourceFromFreeciv
	mkdir -p ${COMPILED_FROM_FREECIV_FOLDER}
	${JAVAC} -d ${COMPILED_FROM_FREECIV_FOLDER} -cp @Core_JAR@ `find ${GENERATED_SOURCE_FOLDER}/generated -iname "*.java"`
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_FROM_FREECIV_FOLDER} -classpath @Core_JAR@  `find ${GENERATED_SOURCE_FOLDER}/generated -iname "*.java"`
endif
	${JAR} cf @FREECIV_VERSION_JAR@ -C ${COMPILED_FROM_FREECIV_FOLDER} \.

@GenerateTestCode_JAR@: @Core_JAR@ @GeneratePackets_JAR@ ${GenerateTestCode_src}
	mkdir -p ${COMPILED_GenerateTestCode_FOLDER}
	${JAVAC} -d ${COMPILED_GenerateTestCode_FOLDER} -cp @GeneratePackets_JAR@:@JavaGenerator_JAR@:@DependencyHandler_JAR@:@Core_JAR@:@Utility_JAR@:${JUNIT_CPF} ${GenerateTestCode_java_src}
	${SCALAC} -d ${COMPILED_GenerateTestCode_FOLDER} -classpath @GeneratePackets_JAR@:@JavaGenerator_JAR@:@DependencyHandler_JAR@:@Core_JAR@:@Utility_JAR@:${JUNIT_CPF} ${GenerateTestCode_scala_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_GenerateTestCode_FOLDER} -classpath @GeneratePackets_JAR@:@JavaGenerator_JAR@:@DependencyHandler_JAR@:@Core_JAR@:@Utility_JAR@:${JUNIT_CPF} ${GenerateTestCode_java_src}
endif
	${JAR} cf @GenerateTestCode_JAR@ -C ${COMPILED_GenerateTestCode_FOLDER} \.

@COMPILED_GenerateTestCode_TEST_FOLDER@/compile: sourceTestPeers @GeneratePackets_JAR@ @Core_JAR@ ${GenerateTestCode_test_src}
	mkdir -p @COMPILED_GenerateTestCode_TEST_FOLDER@
	${JAVAC} -d @COMPILED_GenerateTestCode_TEST_FOLDER@ -cp @Core_JAR@ `find ${GENERATED_TEST_SOURCE_FOLDER}/generated -iname "*.java"`
	${JAVAC} -d @COMPILED_GenerateTestCode_TEST_FOLDER@ -cp @GeneratePackets_JAR@:@Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ ${GenerateTestCode_java_test_src}
	touch @COMPILED_GenerateTestCode_TEST_FOLDER@/compile

@COMPILED_GenerateTestCode_TEST_FOLDER@/run: @COMPILED_GenerateTestCode_TEST_FOLDER@/compile
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.packet.GeneratedUsingEntetiesAlone
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.packet.GeneratedUsingFullGenerator
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.ProtoData
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.test.GeneratedEnumTest
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.test.GeneratedStructTest
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.test.GeneratedBVTest
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.test.FieldTypeTests
	${JAVA} -cp @Core_JAR@:${JUNIT_CPF}:@COMPILED_GenerateTestCode_TEST_FOLDER@ org.junit.runner.JUnitCore org.freeciv.test.IsolatedBugCausers
	touch @COMPILED_GenerateTestCode_TEST_FOLDER@/run

sourceTestPeers: @GenerateTestCode_JAR@
	rm -rf ${GENERATED_TEST_SOURCE_FOLDER}
	${SCALA} -classpath @GenerateTestCode_JAR@:@GeneratePackets_JAR@:@JavaGenerator_JAR@:@DependencyHandler_JAR@:@Core_JAR@:@Utility_JAR@ org.freeciv.packetgen.UsingGenerator ${GENERATED_TEST_SOURCE_FOLDER}
	${JAVA} -cp @GenerateTestCode_JAR@:@GeneratePackets_JAR@:@JavaGenerator_JAR@:@DependencyHandler_JAR@:@Core_JAR@:@Utility_JAR@ org.freeciv.packetgen.FromEntetiesAlone ${GENERATED_TEST_SOURCE_FOLDER}
	touch sourceTestPeers

# not included in tests since make will run the code when generating test peers
runTestPeerCreationAsTests: @GenerateTestCode_JAR@
	${JAVA} -cp @GeneratePackets_JAR@:@JavaGenerator_JAR@:@Core_JAR@:${JUNIT_CPF}:@GenerateTestCode_JAR@ org.junit.runner.JUnitCore org.freeciv.packetgen.FromEntetiesAlone
	touch runTestPeerCreationAsTests

@SignInTest_JAR@: @FREECIV_VERSION_JAR@ @Utility_JAR@ ${SignInTest_src} signIn.manifest signInToServer
	chmod +x signInToServer
	mkdir -p ${COMPILED_SignInTest_FOLDER}
	${JAVAC} -d ${COMPILED_SignInTest_FOLDER} -cp @Core_JAR@:@Utility_JAR@:@FREECIV_VERSION_JAR@ ${SignInTest_java_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_SignInTest_FOLDER} -classpath @Core_JAR@:@Utility_JAR@:@FREECIV_VERSION_JAR@ ${SignInTest_java_src}
endif
	${JAR} cfem @SignInTest_JAR@ org.freeciv.test.SignInAndWait signIn.manifest -C ${COMPILED_SignInTest_FOLDER} \.

# not included in tests since it needs a running Freeciv server
runTestSignInToServer: @SignInTest_JAR@
	sh signInToServer && touch runTestSignInToServer

@FreecivRecorder_JAR@: @FREECIV_VERSION_JAR@ @Utility_JAR@ ${FreecivRecorder_src} fcr.manifest inspectTrace proxyRecorder playRecord
	chmod +x inspectTrace
	chmod +x proxyRecorder
	chmod +x playRecord
	mkdir -p ${COMPILED_FreecivRecorder_FOLDER}
	${JAVAC} -d ${COMPILED_FreecivRecorder_FOLDER} -cp @Core_JAR@:@Utility_JAR@:@FREECIV_VERSION_JAR@ ${FreecivRecorder_java_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_FreecivRecorder_FOLDER} -classpath @Core_JAR@:@Utility_JAR@:@FREECIV_VERSION_JAR@ ${FreecivRecorder_java_src} ${FreecivRecorder_java_src}
endif
	${JAR} cfm @FreecivRecorder_JAR@ fcr.manifest -C ${COMPILED_FreecivRecorder_FOLDER} \.

# not included in tests since it needs a running Freeciv server and client
runProxyRecorer: @FreecivRecorder_JAR@
	sh proxyRecorder && touch runProxyRecorer

@Utility_JAR@: workFolder @Core_JAR@ ${Utility_src}
	mkdir -p ${COMPILED_Utility_FOLDER}
	${JAVAC} -cp @Core_JAR@ -d ${COMPILED_Utility_FOLDER} ${Utility_java_src}
if BUILD_JAVADOC
	${JAVADOC} -d ${COMPILED_Utility_FOLDER} ${Utility_java_src}
endif
	${JAR} cf @Utility_JAR@ -C ${COMPILED_Utility_FOLDER} \.
	touch @Utility_JAR@

@COMPILED_Utility_TEST_FOLDER@/compile: @Utility_JAR@ ${Utility_test_src}
	mkdir -p ${COMPILED_Utility_TEST_FOLDER}
	${JAVAC} -cp @Utility_JAR@:${JUNIT_CPF} -d ${COMPILED_Utility_TEST_FOLDER} ${Utility_java_test_src}
	touch ${COMPILED_Utility_TEST_FOLDER}/compile

# run time depend on @Core_JAR@
@COMPILED_Utility_TEST_FOLDER@/run: @COMPILED_Utility_TEST_FOLDER@/compile  @Core_JAR@
	${JAVA} -cp ${COMPILED_Utility_TEST_FOLDER}:@Utility_JAR@:${JUNIT_CPF}:@Core_JAR@ org.junit.runner.JUnitCore org.freeciv.utility.TestArgumentSettings
	${JAVA} -cp ${COMPILED_Utility_TEST_FOLDER}:@Utility_JAR@:${JUNIT_CPF}:@Core_JAR@ org.junit.runner.JUnitCore org.freeciv.utility.TestSettings
	touch ${COMPILED_Utility_TEST_FOLDER}/run

runTests: @COMPILED_GenerateTestCode_TEST_FOLDER@/run ${COMPILED_DependencyHandler_TEST_FOLDER}/run ${COMPILED_JavaGenerator_TEST_FOLDER}/run ${COMPILED_GeneratePackets_TEST_FOLDER}/run ${COMPILED_Core_TEST_FOLDER}/run ${COMPILED_Utility_TEST_FOLDER}/run
	touch runTests

clean-local:
	rm -rf @GenerateTestCode_JAR@
	rm -rf @COMPILED_GenerateTestCode_TEST_FOLDER@
	rm -rf runTestPeerCreationAsTests
	rm -rf runTests tests
	rm -rf ${GENERATED_TEST_SOURCE_FOLDER} sourceTestPeers
	rm -rf ${COMPILED_SignInTest_FOLDER}
	rm -f all
	rm -f source
	rm -rf runTestSignInToServer
	rm -rf @SignInTest_JAR@
	rm -rf ${COMPILED_FROM_FREECIV_FOLDER}
	rm -rf @FREECIV_VERSION_JAR@
	rm -rf runProxyRecorer
	rm -rf @FreecivRecorder_JAR@
	rm -rf ${COMPILED_Core_FOLDER}
	rm -rf @Core_JAR@
	rm -rf ${COMPILED_Utility_FOLDER}
	rm -rf @Utility_JAR@
	rm -rf ${COMPILED_JavaGenerator_FOLDER} @JavaGenerator_JAR@
	rm -rf ${COMPILED_DependencyHandler_FOLDER} @DependencyHandler_JAR@
	rm -rf ${COMPILED_DependencyHandler_TEST_FOLDER}
	rm -rf @GeneratePackets_JAR@
	rm -rf ${COMPILED_GeneratePackets_FOLDER}
	rm -rf ${GENERATED_SOURCE_FOLDER} sourceFromFreeciv
	rm -rf ${WORK_FOLDER} workFolder
