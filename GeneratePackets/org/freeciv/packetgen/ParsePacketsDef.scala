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

  def capability = identifierRegEx

  def fieldFlag = (
    "key" |
    "add-cap(" ~ capability ~ ")" |
    "diff" |
    "remove-cap(" ~ capability ~ ")"
    )

  def arrayFullSize = intExpr

  def fieldName = regex(identifierRegEx)

  def fieldVar: Parser[(String, List[Field.ArrayDeclaration])] =
    (fieldName ~ rep("[" ~> arrayFullSize ~ opt(":" ~> fieldName) <~ "]")) ^^ {
    case name~dimensions =>
      name -> dimensions.map({
        case maxSize ~ toTransferThisTime =>
          new Field.ArrayDeclaration(maxSize, toTransferThisTime.getOrElse(null))})}

  def fields = (fieldType ~ rep1sep(fieldVar, ",") <~ ";") ~ repsep(fieldFlag, ",") ^^ {
    case kind~variables~flags => variables.map(variable => new Field.WeakField(variable._1, kind, variable._2: _*))}

  def fieldList: Parser[List[Field.WeakField]] = rep(comment) ~> rep((fields <~ rep(comment))) ^^ {_.flatten}

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

  protected def isNewLineIgnored(source: CharSequence, offset: Int) = true
}
