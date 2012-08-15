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

package org.freeciv.packetgen.parsing

import util.parsing.input.Reader
import collection.JavaConversions._
import org.freeciv.packetgen.PacketsStore
import org.freeciv.packetgen.enteties.supporting.{WeakFlag, WeakField, Field}

class ParsePacketsDef(storage: PacketsStore) extends ParseShared {
  def fieldTypeAlias = regex(identifierRegEx)

  def basicFieldType: Parser[(String, String)] = identifierRegEx ~ ("(" ~> cType <~ ")") ^^ {
    case iotype ~ ptype => iotype -> ptype.reduce(_ + " " + _)
  }

  def fieldType: PackratParser[Any] = basicFieldType | fieldTypeAlias

  def fieldTypeAssign: Parser[Any] = "type" ~> fieldTypeAlias ~ ("=" ~> fieldType) ^^ {
    case alias ~ (aliased: String) => storage.registerTypeAlias(alias, aliased)
    case alias ~ Pair((iotype: String), (ptype: String)) => storage.registerTypeAlias(alias, iotype, ptype)
    case result => failure(result + " was not recognized as a new field type alias and what it's aliasing")
  }

  def comment = CComment |
    regex("""#[^\n\r]*""".r)

  def packetName = regex(("PACKET_" + identifier).r)

  def packetFlag = "is-info" |
    "is-game-info" |
    "force" |
    "cancel" |
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

  def flagArgument = "(" ~> identifierRegEx <~ ")"

  def fieldFlag =
    "key" |
    "add-cap" |
    "diff" |
    "remove-cap"

  def arrayFullSize = intExpr

  def fieldName = regex(identifierRegEx)

  def fieldVar: Parser[(String, List[WeakField.ArrayDeclaration])] =
    (fieldName ~ rep("[" ~> arrayFullSize ~ opt(":" ~> fieldName) <~ "]")) ^^ {
      case name ~ dimensions =>
        name -> dimensions.map({
          case maxSize ~ toTransferThisTime =>
            new WeakField.ArrayDeclaration(maxSize, toTransferThisTime.getOrElse(null))
        })
    }

  def fields = (fieldTypeAlias ~ rep1sep(fieldVar, ",") <~ ";") ~ repsep(fieldFlag ~ opt(flagArgument), ",") ^^ {
    case kind ~ variables ~ flags =>
      variables.map(variable => new WeakField(variable._1, kind, wrapFlags(flags), variable._2: _*))
  }

  def fieldList: Parser[List[WeakField]] = rep(comment) ~> rep((fields <~ rep(comment))) ^^ {_.flatten}

  def packet = packetName ~ ("=" ~> regex("""[0-9]+""".r) <~ ";") ~ repsep(packetFlag ~ opt(flagArgument), ",") ~
    fieldList <~
    "end" ^^ {
    case name ~ number ~ flags ~ fields =>
      storage.registerPacket(
        name,
        Integer.parseInt(number),
        wrapFlags(flags),
        fields)
  }

  def wrapFlags(flags: List[~[String, Option[String]]]): List[WeakFlag] =
    flags.map({
    case flag ~ Some(args) => new WeakFlag(flag, args)
    case flag ~ None => new WeakFlag(flag)
  })

  def expr: Parser[Any] = fieldTypeAssign | comment | packet

  def parsePacketsDef(input: String) = parseAll(exprs, input)

  def parsePacketsDef(input: Reader[Char]) = parseAll(exprs, input)

  protected def isNewLineIgnored(source: CharSequence, offset: Int) = true

  protected def areCommentsIgnored(source: CharSequence, offset: Int): Boolean = false
}
