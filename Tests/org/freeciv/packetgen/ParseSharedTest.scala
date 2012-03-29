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

package org.freeciv.packetgen

import org.junit.Assert._
import org.junit.Test

class ParseSharedTest {
  def parserShared = new ParseShared {
    def expr = null

    protected def isNewLineIgnored(source: CharSequence, offset: Int) = false
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Parsing of numbers
  --------------------------------------------------------------------------------------------------------------------*/
  def assertIntExpressionBecomes(expected: String, input: String) =
    assertEquals(expected,
      CParserTest.parsesCorrectly(input, parserShared, parserShared.intExpr).get.toString)

  @Test def value = assertIntExpressionBecomes("4", "4")
  @Test def constantName = assertIntExpressionBecomes("Constants.MAX_THING", "MAX_THING")

  // Parses basic unary operations
  @Test def parenParen = assertIntExpressionBecomes("4", "((4))")
  @Test def unaryAdd = assertIntExpressionBecomes("4", "+4")
  @Test def unaryMinus = assertIntExpressionBecomes("-4", "-4")
  @Test def prefixPlusPlus = assertIntExpressionBecomes("++Constants.A", "++A")
  @Test def suffixPlusPlus = assertIntExpressionBecomes("Constants.A++", "A++")
  @Test def prefixMinusMinus = assertIntExpressionBecomes("--Constants.A", "--A")
  @Test def suffixMinusMinus = assertIntExpressionBecomes("Constants.A--", "A--")

  // Parses basic binary operations
  @Test def binaryAdd = assertIntExpressionBecomes("1 + 2", "1 + 2")
  @Test def binaryMinus = assertIntExpressionBecomes("1 - 2", "1 - 2")
  @Test def binaryMul = assertIntExpressionBecomes("1 * 2", "1 * 2")
  @Test def binaryDiv = assertIntExpressionBecomes("1 / 2", "1 / 2")

  // Correct precedence where precedence is given by kind of operator
  @Test def plusTimes = assertIntExpressionBecomes("1 + (2 * 3)", "1 + 2 * 3")
  @Test def timesPlus = assertIntExpressionBecomes("(1 * 2) + 3", "1 * 2 + 3")

  // Correct precedence where precedence is given by order of operators
  @Test def divMul = assertIntExpressionBecomes("(1 / 2) * 3", "1 / 2 * 3")
  @Test def mulDiv = assertIntExpressionBecomes("(1 * 2) / 3", "1 * 2 / 3")
  @Test def addSub = assertIntExpressionBecomes("(1 + 2) - 3", "1 + 2 - 3")
  @Test def subAdd = assertIntExpressionBecomes("(1 - 2) + 3", "1 - 2 + 3")

  // Combinations
  @Test def parenPlusTimes = assertIntExpressionBecomes("(1 + 2) * 3", "(1 + 2) * 3")
  @Test def parenTimesPlus = assertIntExpressionBecomes("1 * (2 + 3)", "1 * (2 + 3)")
  @Test def uminusBinMinus = assertIntExpressionBecomes("(-2) - 3", "-2 - 3")
  @Test def binMinusUminus = assertIntExpressionBecomes("2 - (-3)", "2 - -3")
  @Test def uminusParenBinMinus = assertIntExpressionBecomes("-(2 - 3)", "-(2 - 3)")
  @Test def manyPlus = assertIntExpressionBecomes("(((((4 + 4) + 4) + 4) + 4) + 4) + 4", "4+4+4+4+4+4+4")

  /*--------------------------------------------------------------------------------------------------------------------
  Normalization of C int type declarations
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def shortIntIsShort = assertEquals(
    parserShared.normalizeCIntDeclaration(List("short", "int")),
    parserShared.normalizeCIntDeclaration(List("short"))
  )

  @Test def signedIsInt = assertEquals(
    parserShared.normalizeCIntDeclaration(List("signed")),
    parserShared.normalizeCIntDeclaration(List("int"))
  )

  @Test def signedLongIsLongInt = assertEquals(
    parserShared.normalizeCIntDeclaration(List("signed", "long")),
    parserShared.normalizeCIntDeclaration(List("long", "int"))
  )

  @Test def uintIsUnsigned = assertEquals(
    parserShared.normalizeCIntDeclaration(List("uint")),
    parserShared.normalizeCIntDeclaration(List("unsigned"))
  )

  @Test def signedLongIntIsLong = assertEquals(
    parserShared.normalizeCIntDeclaration(List("signed", "long", "int")),
    parserShared.normalizeCIntDeclaration(List("long"))
  )
}
