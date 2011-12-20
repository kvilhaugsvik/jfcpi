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

import util.parsing.input.Reader
import collection.JavaConversions._

class ParsePacketsDef(storage: PacketsStore) extends ParseShared {
  def fieldType = regex("""[A-Z](\w|_)*""".r)
  def fieldTypeDef = fieldType | regex("""\w*\((\w|\s)*\)""".r)

  def fieldTypeAssign: Parser[Any] = "type" ~> fieldType ~ ("=" ~> fieldTypeDef) ^^ {
    case alias~aliased => storage.registerTypeAlias(alias, aliased)
  }

  def comment = CComment |
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

  def capability = regex("""[A-Za-z0-9_-]+""".r)

  def fieldFlag = (
    "key" |
    "add-cap(" ~ capability ~ ")" |
    "diff" |
    "remove-cap(" ~ capability ~ ")"
    )

  def arrayFullSize = regex("""[0-9a-zA-Z_\+\*-/]+""".r)

  def elementsToTransfer = regex("""[0-9a-zA-Z_\+\*-/]+""".r)

  def fieldArrayDeclaration = rep("[" ~> arrayFullSize ~ opt(":" ~> elementsToTransfer) <~ "]")

  def fieldVar = (regex("""\w+""".r) ~ fieldArrayDeclaration) ^^ {
    case varName ~ arrayDec => (varName, arrayDec)
  }

  def field = (fieldType ~ repsep(fieldVar, ",") <~ ";") ~ repsep(fieldFlag, ",") ^^ {
    case kind~variables~flags => variables.map((vari) => Array(kind, vari._1))
  }

  def fieldList: Parser[List[Array[String]]] = rep(comment) ~> rep((field <~ rep(comment))) ^^ {_.flatten}

  def packet = packetName ~ ("=" ~> regex("""[0-9]+""".r) <~ ";") ~ repsep(packetFlag, ",") ~
  fieldList <~
  "end" ^^ {
    case name~number~flags~fields =>
      storage.registerPacket(
        name,
        Integer.parseInt(number),
        fields)
  };

  def expr: Parser[Any] = fieldTypeAssign | comment | packet

  def parsePacketsDef(input: String) = parseAll(exprs, input)
  def parsePacketsDef(input: Reader[Char]) = parseAll(exprs, input)
}
