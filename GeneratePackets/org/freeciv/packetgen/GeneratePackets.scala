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

    val manifest = new File(path + "/org/freeciv/packet/" + "packets.txt")
    manifest.createNewFile
    val manifestWriter = new FileWriter(manifest)
    manifestWriter.write(storage.getPacketList)
    manifestWriter.close()
  }
}

object GeneratePackets {
  def main(args: Array[String]) {
    val self = new GeneratePackets(args(0), true)
    self.writeToDir(GeneratorDefaults.GENERATEDOUT)
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

  def packetFlag = "is-info" |
    "is-game-info" |
    "force" |
    """cancel(""" ~ packetName ~ """)""" |
    "pre-send" |
    "post-recv" |
    "post-send" |
    "no-delta" |
    "no-packet" |
    "no-handle" |
    "handle-via-packet" |
    "handle-per-conn" |
    "dsend" |
    "lsend" |
    "cs" |
    "sc"

  def packetFlags = opt(packetFlag ~ rep("," ~> packetFlag))

  def capability = regex("""[A-Za-z0-9_-]+""".r)

  def fieldFlag = (
    "key" |
    "add-cap(" ~ capability ~ ")" |
    "diff" |
    "remove-cap(" ~ capability ~ ")"
    )

  def fieldFlags = opt(fieldFlag ~ rep("," ~> fieldFlag))

  def arrayFullSize = regex("""[0-9a-zA-Z_\+\*-/]+""".r)

  def elementsToTransfer = regex("""[0-9a-zA-Z_\+\*-/]+""".r)

  def fieldArrayDeclaration = rep("[" ~ arrayFullSize ~ opt(":" ~ elementsToTransfer) ~ "]")

  def field = fieldType ~ regex("""\w+""".r) ~ fieldArrayDeclaration ~ ";" ~ fieldFlags ^^ {
    case alias~aliased~arrayDec~end~flags => Array(alias, aliased)
  }

  def fieldList = rep(comment) ~> rep((field <~ rep(comment)))

  def packet = packetName ~ "=" ~ regex("""[0-9]+""".r) ~ ";" ~
  packetFlags ~
  fieldList ~
  "end" ^^ {
    case name~has~number~endOfHeader~flags~fields~end => storage.registerPacket(name, Integer.parseInt(number), fields)
  };

  def expr: Parser[Any] = fieldTypeAssign | comment | packet

  def parsePacketsDef(input: String) = parseAll(rep(expr), input)
  def parsePacketsDef(input: Reader[Char]) = parseAll(rep(expr), input)
}
