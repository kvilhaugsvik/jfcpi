JAVA ?= java
JAVAC ?= javac
JAR ?= jar
SCALA ?= scala
SCALAC ?= scalac
JUNIT ?= /usr/share/java/junit4.jar

PROTOOUT ?= out/Protocol
PACKETGENOUT ?= out/GeneratePackages
TESTOUT ?= out/Tests

PROTOJAR = FreecivProto.jar

all: protojar

protocol:
	mkdir -p ${PROTOOUT}
	${JAVAC} -d ${PROTOOUT} Protocol/org/freeciv/*.java Protocol/org/freeciv/packet/*.java
	touch protocol

generator:
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
	${JAVAC} -d ${PROTOOUT} -cp ${PROTOOUT} autogenerated/org/freeciv/packet/*.java
	cp autogenerated/org/freeciv/packet/packets.txt ${PROTOOUT}/org/freeciv/packet/
	touch generated

protojar: generated
	${JAR} cf ${PROTOJAR} ${PACKETGENOUT}
	touch protojar

testcode: generated
	mkdir -p ${TESTOUT}
	${JAVAC} -d ${TESTOUT} -cp ${PACKETGENOUT}:${PROTOOUT}:${JUNIT} Tests/org/freeciv/test/*.java
	${SCALAC} -d ${TESTOUT} -classpath ${PACKETGENOUT}:${PROTOOUT}:${JUNIT}:${TESTOUT} Tests/org/freeciv/test/*.scala
	touch testcode

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
	rm -rf autogenerated/* testpackets
	rm -f ${PROTOJAR} protojar

distclean: clean
	rm -rf out autogenerated
