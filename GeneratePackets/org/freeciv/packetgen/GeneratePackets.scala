/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.freeciv.packetgen

import util.parsing.combinator._
import util.parsing.input.{StreamReader, Reader}
import collection.JavaConversions._
import java.io._

class GeneratePackets(packetsDefPath: File, devMode: Boolean, hasTwoBytePacketNumber: Boolean) {

  def this(packetsDefPath: String, devMode: Boolean, hasTwoBytePacketNumber: Boolean) =
    this(new File(packetsDefPath), devMode, hasTwoBytePacketNumber)

  private val storage = new PacketsStore(devMode, hasTwoBytePacketNumber)
  private val Parser = new ParsePacketsDef(storage)

  if (!packetsDefPath.exists()) {
    throw new IOException(packetsDefPath.getAbsolutePath + " doesn't exist.")
  } else if (!packetsDefPath.canRead) {
    throw new IOException("Can't read " + packetsDefPath.getAbsolutePath)
  }

  if (!Parser.parsePacketsDef(StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath)))).successful) {
    throw new IOException("Can't parse " + packetsDefPath.getAbsolutePath)
  }

  def writeToDir(path: String): Unit = writeToDir(new File(path))

  def writeToDir(path: File) {
    val files = storage.getJavaCode
    (new File(path + "/org/freeciv/packet/fieldtype")).mkdirs()
    files.foreach({case (name, code) =>
      val packagePath = code.split(" |;")(1).replaceAll("""\.""", "/")
      val classFile = new File(path + "/" + packagePath + "/" + name + ".java")
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
    val self = new GeneratePackets(args(0), GeneratorDefaults.DEVMODE, true)
    self.writeToDir(GeneratorDefaults.GENERATEDOUT)
  }
}

class ParsePacketsDef(storage: PacketsStore) extends RegexParsers {
  def fieldType = regex("""[A-Z](\w|_)*""".r)
  def fieldTypeDef = fieldType | regex("""\w*\((\w|\s)*\)""".r)

  def fieldTypeAssign: Parser[Any] = "type" ~ fieldType ~ "=" ~ fieldTypeDef ^^ {
    case theType~alias~is~aliased => storage.registerTypeAlias(alias, aliased)
  }

  def comment = """/\*\**""".r ~ rep("""([^*\n\r]|\*+[^/*])+""".r) ~ """\**\*/""".r |
    regex("""/\*+\*/""".r) |
    regex("""//[^\n\r]*""".r) |
    regex("""#[^\n\r]*""".r)

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

  def fieldVar = (regex("""\w+""".r) ~ fieldArrayDeclaration) ^^ {
    case varName ~ arrayDec => (varName, arrayDec)
  }

  def field = fieldType ~ fieldVar ~ rep("," ~> fieldVar) ~ ";" ~ fieldFlags ^^ {
    case kind~variable~moreVars~end~flags => (variable :: moreVars).map((vari) => Array(kind, vari._1))
  }

  def fieldList = rep(comment) ~> rep((field <~ rep(comment)))

  def packet = packetName ~ "=" ~ regex("""[0-9]+""".r) ~ ";" ~
  packetFlags ~
  fieldList ~
  "end" ^^ {
    case name~has~number~endOfHeader~flags~fields~end =>
      val flattenFields = fields.flatten
      storage.registerPacket(
        name,
        Integer.parseInt(number),
        flattenFields)
  };

  def expr: Parser[Any] = fieldTypeAssign | comment | packet

  def parsePacketsDef(input: String) = parseAll(rep(expr), input)
  def parsePacketsDef(input: Reader[Char]) = parseAll(rep(expr), input)
}
