JAVA ?= java
JAVAC ?= javac
JAR ?= jar
SCALA ?= scala
SCALAC ?= scalac
JUNIT ?= /usr/share/java/junit4.jar

GENERATEDOUT ?= autogenerated
GENERATORDEFAULTS ?= GeneratePackets/org/freeciv/packetgen/GeneratorDefaults.java
PROTOOUT ?= out/Protocol
PACKETGENOUT ?= out/GeneratePackages
TESTOUT ?= out/Tests
DEVMODE ?= true

PROTOJAR = FreecivProto.jar

all: protojar

protocol:
	mkdir -p ${PROTOOUT}
	${JAVAC} -d ${PROTOOUT} Protocol/org/freeciv/*.java Protocol/org/freeciv/packet/*.java
	touch protocol

generatordefaults:
	echo "package org.freeciv.packetgen;" >> ${GENERATORDEFAULTS}
	echo "public class GeneratorDefaults {" >> ${GENERATORDEFAULTS}
	echo "  public static final String GENERATEDOUT = \"${GENERATEDOUT}\";" >> ${GENERATORDEFAULTS}
	echo "  public static final boolean DEVMODE = ${DEVMODE};" >> ${GENERATORDEFAULTS}
	echo "}" >>${GENERATORDEFAULTS}
	touch generatordefaults

generator: generatordefaults
	mkdir -p ${PACKETGENOUT}
	${JAVAC} -d ${PACKETGENOUT} GeneratePackets/org/freeciv/packetgen/*.java
	${SCALAC} -classpath ${PACKETGENOUT} -d ${PACKETGENOUT} GeneratePackets/org/freeciv/packetgen/GeneratePackets.scala
	touch generator

testpackets:
	mkdir -p ${TESTOUT}
	${JAVAC} -d ${TESTOUT} -cp ${PACKETGENOUT} Tests/org/freeciv/test/GenerateTest.java
	${JAVA} -cp ${TESTOUT}:${PACKETGENOUT} org.freeciv.test.GenerateTest
	touch testpackets

# since the parser isn't finished use GenerateTest as generator
generated: generator protocol testpackets
	${JAVAC} -d ${PROTOOUT} -cp ${PROTOOUT} ${GENERATEDOUT}/org/freeciv/packet/*.java
	cp ${GENERATEDOUT}/org/freeciv/packet/packets.txt ${PROTOOUT}/org/freeciv/packet/
	touch generated

protojar: generated
	${JAR} cf ${PROTOJAR} ${PACKETGENOUT}
	touch protojar

testcode: generated
	mkdir -p ${TESTOUT}
	${JAVAC} -d ${TESTOUT} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT} Tests/org/freeciv/test/*.java
	${SCALAC} -d ${TESTOUT} -classpath ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} Tests/org/freeciv/test/*.scala
	touch testcode

# not included in tests since it needs a running Freeciv server
testsignintoserver: testcode
	${JAVA} -cp ${PROTOOUT}:${TESTOUT} org.freeciv.test.SignInAndWait
	touch testsignintoserver

tests: testcode
	${JAVA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.test.PacketTest
	${JAVA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.test.PacketsStoreTest
	${SCALA} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} org.junit.runner.JUnitCore org.freeciv.test.ParseTest
	touch tests

clean:
	rm -rf ${PROTOOUT} protocol
	rm -rf ${PACKETGENOUT} generator
	rm -rf generated
	rm -rf testcode
	rm -rf tests ${TESTOUT}
	rm -rf ${GENERATEDOUT}/* testpackets
	rm -f ${PROTOJAR} protojar
	rm -rf testsignintoserver

distclean: clean
	rm -rf out ${GENERATEDOUT}
	rm -rf ${GENERATORDEFAULTS} generatordefaults
