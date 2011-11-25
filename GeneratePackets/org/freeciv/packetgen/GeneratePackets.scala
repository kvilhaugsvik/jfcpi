package org.freeciv.packetgen

import util.parsing.combinator._
import util.parsing.input.{StreamReader, Reader}
import collection.JavaConversions._
import java.io._

class GeneratePackets(packetsDefPath: File, devMode: Boolean) {

  def this(packetsDefPath: String, devMode: Boolean) = this(new File(packetsDefPath), devMode)

  private val storage = new PacketsStore(devMode)
  private val Parser = new ParsePacketsDef(storage)

  if (!packetsDefPath.exists()) {
    throw new IOException(packetsDefPath.getAbsolutePath + " doesn't exist.")
  } else if (!packetsDefPath.canRead()) {
    throw new IOException("Can't read " + packetsDefPath.getAbsolutePath)
  }

  if (!Parser.parsePacketsDef(StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath)))).successful) {
    throw new IOException("Can't parse " + packetsDefPath.getAbsolutePath)
  }

  def writeToDir(path: String): Unit = writeToDir(new File(path))

  def writeToDir(path: File) {
    val files = storage.getJavaCode
    (new File(path + "/org/freeciv/packet/")).mkdirs()
    files.foreach({case (name, code) =>
      val classFile = new File(path + "/org/freeciv/packet/" + name + ".java")
      classFile.createNewFile
      val classWriter = new FileWriter(classFile)
      classWriter.write(code)
      classWriter.close()
    })
  }
}

object GeneratePackets {
  def main(args: Array[String]) {
    val self = new GeneratePackets(args(0), true)
    self.writeToDir("autogenerated")
  }
}

class ParsePacketsDef(storage: PacketsStore) extends RegexParsers {
  def fieldType = regex("""[A-Z](\w|_)*""".r)
  def fieldTypeDef = fieldType | regex("""\w*\((\w|\s)*\)""".r)

  def fieldTypeAssign: Parser[Any] = "type" ~ fieldType ~ "=" ~ fieldTypeDef ^^ {
    case theType~alias~is~aliased => storage.registerTypeAlias(alias, aliased)
  }

  def comment = regex("""/\*(.|[\n\r])*?\*/""".r) | regex("""//[^\n\r]*""".r) | regex("""#[^\n\r]*""".r)

  def packetName = regex("""PACKET_[A-Za-z0-9_]+""".r)

  def field = fieldType ~ regex("""\w+""".r) ~ ";"

  def fieldList = rep(field)

  def packet = packetName ~ "=" ~ regex("""[0-9]+""".r) ~ ";" ~
  fieldList ~
  "end" ^^ {
    case name~has~number~endOfHeader~fields~end => storage.registerPacket(new Packet(name, Integer.parseInt(number)))
  };

  def expr: Parser[Any] = fieldTypeAssign | comment | packet

  def parsePacketsDef(input: String) = parseAll(rep(expr), input)
  def parsePacketsDef(input: Reader[Char]) = parseAll(rep(expr), input)
}
