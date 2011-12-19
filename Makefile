JAVA ?= java
JAVAC ?= javac
JAR ?= jar
SCALA ?= scala
SCALAC ?= scalac
JUNIT ?= /usr/share/java/junit4.jar:/usr/share/java/hamcrest-core.jar

GENERATEDOUT ?= autogenerated
GENERATORDEFAULTS ?= GeneratePackets/org/freeciv/packetgen/GeneratorDefaults.java
PROTOOUT ?= out/Protocol
PACKETGENOUT ?= out/GeneratePackages
TESTOUT ?= out/Tests
DEVMODE ?= true

PROTOJAR = FreecivProto.jar

all: protojar
	touch all

protocol:
	mkdir -p ${PROTOOUT}
	${JAVAC} -d ${PROTOOUT} Protocol/org/freeciv/*.java Protocol/org/freeciv/packet/*.java \
	                        Protocol/org/freeciv/*.java Protocol/org/freeciv/packet/*/*.java
	touch protocol

generatordefaults:
	echo "package org.freeciv.packetgen;" >> ${GENERATORDEFAULTS}
	echo "public class GeneratorDefaults {" >> ${GENERATORDEFAULTS}
	echo "  public static final String GENERATEDOUT = \"${GENERATEDOUT}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final boolean DEVMODE = ${DEVMODE};" >> ${GENERATORDEFAULTS}
	echo "}" >>${GENERATORDEFAULTS}
	touch generatordefaults

generator: generatordefaults protocol
	mkdir -p ${PACKETGENOUT}
	${JAVAC} -cp ${PROTOOUT} -d ${PACKETGENOUT} GeneratePackets/org/freeciv/packetgen/*.java
	${SCALAC} -classpath ${PACKETGENOUT} -d ${PACKETGENOUT} GeneratePackets/org/freeciv/packetgen/*.scala
	touch generator

testpackets: protocol generator
	mkdir -p ${TESTOUT}
	${JAVAC} -d ${TESTOUT} -cp ${PACKETGENOUT}:${PROTOOUT} Tests/org/freeciv/test/GenerateTest.java
	${JAVA} -cp ${TESTOUT}:${PACKETGENOUT}:${PROTOOUT} org.freeciv.test.GenerateTest
	touch testpackets

# since the parser isn't finished use GenerateTest as generator
generated: generator protocol testpackets
	${JAVAC} -d ${PROTOOUT} -cp ${PROTOOUT} ${GENERATEDOUT}/org/freeciv/packet/*.java \
	                                        ${GENERATEDOUT}/org/freeciv/packet/*/*.java
	cp ${GENERATEDOUT}/org/freeciv/packet/packets.txt ${PROTOOUT}/org/freeciv/packet/
	touch generated

protojar: generated
	${JAR} cf ${PROTOJAR} ${PACKETGENOUT}
	touch protojar

testout:
	mkdir -p ${TESTOUT}
	touch testout

generatortestcompile: testout generator
	${JAVAC} -d ${TESTOUT} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT} Tests/org/freeciv/packetgen/*.java
	${SCALAC} -d ${TESTOUT} -classpath ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} Tests/org/freeciv/packetgen/*.scala
	touch generatortestcompile

generatortest: generatortestcompile
	${JAVA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.packetgen.PacketsStoreTest
	${JAVA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.packetgen.CodeGenTest
	${SCALA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.packetgen.ParseTest
	${SCALA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.packetgen.CParserTest
	touch generatortest

testcode: generated testout
	${JAVAC} -d ${TESTOUT} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT} Tests/org/freeciv/test/*.java
	touch testcode

# not included in tests since it needs a running Freeciv server
testsignintoserver: testcode
	${JAVA} -cp ${PROTOOUT}:${TESTOUT} org.freeciv.test.SignInAndWait
	touch testsignintoserver

tests: testcode generatortest
	${JAVA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.test.PacketTest
	touch tests

clean:
	rm -rf ${PROTOOUT} protocol
	rm -rf ${PACKETGENOUT} generator
	rm -rf generated
	rm -rf testcode
	rm -rf generatortestcompile
	rm -rf generatortest
	rm -rf tests
	rm -rf testout ${TESTOUT}
	rm -rf ${GENERATEDOUT}/* testpackets
	rm -f ${PROTOJAR} protojar
	rm -f all
	rm -rf testsignintoserver
	rm -rf ${PROTOJAR}

distclean: clean
	rm -rf out ${GENERATEDOUT}
	rm -rf ${GENERATORDEFAULTS} generatordefaults
