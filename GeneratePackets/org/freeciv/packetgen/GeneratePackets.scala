package org.freeciv.packetgen

import util.parsing.combinator._
import java.io.{InputStreamReader, FileInputStream, IOException, File}
import util.parsing.input.{StreamReader, Reader}

class GeneratePackets(packetsDefPath: File, devMode: Boolean) {

  def this(packetsDefPath: String, devMode: Boolean) = this(new File(packetsDefPath), devMode)

  private def storage = new PacketsStore(devMode)
  private def Parser = new ParsePacketsDef(storage)

  if (!packetsDefPath.exists()) {
    throw new IOException(packetsDefPath.getAbsolutePath + " doesn't exist.")
  } else if (!packetsDefPath.canRead()) {
    throw new IOException("Can't read " + packetsDefPath.getAbsolutePath)
  }

  val toReadFrom: StreamReader = StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath)))
  Parser.parsePacketsDef(toReadFrom)
}

object GeneratePackets {
  def main(args: Array[String]) {
    val self = new GeneratePackets(args(0), true)
  }
}

class ParsePacketsDef(storage: PacketsStore) extends RegexParsers {
  def fieldType = regex("""[A-Z](\w|_)*""".r)
  def fieldTypeDef = fieldType | regex("""\w*\((\w|\s)*\)""".r)

  def fieldTypeAssign: Parser[Any] = "type" ~ fieldType ~ "=" ~ fieldTypeDef ^^ {
    case theType~alias~is~aliased => storage.registerTypeAlias(alias, aliased)
  }

  def expr: Parser[Any] = fieldTypeAssign

  def parsePacketsDef(input: String) = parseAll(rep(expr), input)
  def parsePacketsDef(input: Reader[Char]) = parseAll(rep(expr), input)
}
