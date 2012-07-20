/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.javaGenerator.expression.willReturn._
import org.freeciv.packetgen.enteties.Constant
import org.freeciv.packetgen.javaGenerator.expression.util.WrapCodeString

object ParseVariableAssignments extends ExtractableParser {
  protected def isNewLineIgnored(source: CharSequence, offset: Int) = false
  protected def areCommentsIgnored(source: CharSequence, offset: Int) = true
  def startsOfExtractable = List(identifier + "\\s*" + "=")

  // TODO: Should concatenation be supported?
  def strExpr = quotedString.r ^^ {a => WrapCodeString.asAString(a)}

  def value: Parser[AValue] = strExpr

  // TODO: Error if next item isn't EOL to avoid subtle bugs
  def assignment = (identifierRegEx <~ "=") ~ value

  def expr = assignment

  def assignmentConverted = assignment ^^ {
    case name ~ (value: ALong) => new Constant(name, value)
    case name ~ (value: AString) => new Constant(name, value)
    case _ => throw new IllegalArgumentException("Internal error: Asked to interpret unknown type")
  }

  def exprConverted = assignmentConverted
}

object VariableAssignmentsExtractor extends ExtractorShared(ParseVariableAssignments)
