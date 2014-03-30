package org.freeciv.packetgen.parsing

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

import org.junit.Assert._
import org.junit.Test
import util.parsing.input.CharArrayReader
import org.freeciv.packetgen.enteties.supporting.IntExpression
import com.kvilhaugsvik.javaGenerator.representation.{CodeAtoms, HasAtoms}
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom
import org.freeciv.utility.Util

class ParseSharedTest {
  def parserShared = new ParseShared {
    def expr = null

    protected def isNewLineIgnored(source: CharSequence, offset: Int) = true

    protected def isLineWSIgnored(source: CharSequence, offset: Int) = true

    protected def areCommentsIgnored(source: CharSequence, offset: Int) = false
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Parsing of numbers
  --------------------------------------------------------------------------------------------------------------------*/
  def assertIntExpressionBecomes(expected: String, input: String) =
    assertEquals(expected,
      CParserTest.parsesCorrectly(input, parserShared, parserShared.intExpr).get.toString)

  @Test def value = assertIntExpressionBecomes("4", "4")

  @Test def hexValue = assertIntExpressionBecomes("0xFA53", "0xFA53")

  @Test def lowerCaseHexValue = assertIntExpressionBecomes("0xfa53", "0xfa53")

  @Test def constantName = assertIntExpressionBecomes(Util.VERSION_DATA_CLASS + ".MAX_THING", "MAX_THING")

  // Parses basic unary operations
  @Test def parenParen = assertIntExpressionBecomes("4", "((4))")

  @Test def unaryAdd = assertIntExpressionBecomes("4", "+4")

  // Code style problem workaround
  @Test def unaryMinus = assertIntExpressionBecomes("- 4", "-4")

  @Test def prefixPlusPlus = assertIntExpressionBecomes("++" + Util.VERSION_DATA_CLASS + ".A", "++A")

  @Test def suffixPlusPlus = assertIntExpressionBecomes(Util.VERSION_DATA_CLASS + ".A++", "A++")

  @Test def prefixMinusMinus = assertIntExpressionBecomes("--" + Util.VERSION_DATA_CLASS + ".A", "--A")

  @Test def suffixMinusMinus = assertIntExpressionBecomes(Util.VERSION_DATA_CLASS + ".A--", "A--")

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

   // Code style problem workaround
  @Test def uminusBinMinus = assertIntExpressionBecomes("(- 2) - 3", "-2 - 3")

  // Code style problem workaround
  @Test def binMinusUminus = assertIntExpressionBecomes("2 -(- 3)", "2 - -3")

  @Test def uminusParenBinMinus = assertIntExpressionBecomes("-(2 - 3)", "-(2 - 3)")

  @Test def manyPlus = assertIntExpressionBecomes("(((((4 + 4) + 4) + 4) + 4) + 4) + 4", "4+4+4+4+4+4+4")

  // Semantic: hasNoVariables
  @Test def numberIsNumber = assertTrue("The number 4 should be a number",
    CParserTest.parsesCorrectly("4", parserShared, parserShared.intExpr).get.hasNoVariables)

  @Test def constantIsConstant = assertFalse("The constant CONS should be a constant",
    CParserTest.parsesCorrectly("CONS", parserShared, parserShared.intExpr).get.hasNoVariables)

  @Test def negativeNumberIsNumber = assertTrue("An expression of numbers should be a number",
    CParserTest.parsesCorrectly("-4", parserShared, parserShared.intExpr).get.hasNoVariables)

  @Test def numberExpressionIsNumber = assertTrue("An expression of numbers should be a number",
    CParserTest.parsesCorrectly("4 + 5 * (2 - 3 % 4) / 7", parserShared, parserShared.intExpr).get.hasNoVariables)

  @Test def expressionWithConsIsNotANumber = assertFalse("An expression that has a constant is no pure number",
    CParserTest.parsesCorrectly("4 + 5 * (2 - (3 % CONS))", parserShared, parserShared.intExpr).get.hasNoVariables)

  // TODO: Should probably be tested another place
  // Mapping
  @Test def mapParsedNumbersToTheirNames {
    val input: String = "1 + -2 * 3++"
    // Code style problem workaround
    val result: String = "ONE + ((- TWO) * (THREE++))"
    val transformer: (IntExpression) => HasAtoms = leaf => {
      if ("1".equals(leaf.toString))
        new CodeAtom("ONE")
      else if ("2".equals(leaf.toString))
        new CodeAtom("TWO")
      else if ("3".equals(leaf.toString))
        new CodeAtom("THREE")
      else
        new CodeAtom("UNKNOWN")
    }

    assertEquals("Failed to map the values of an IntExpression",
      result,
      CParserTest.parsesCorrectly(input, parserShared, parserShared.intExpr).get.valueMap(transformer).toString)
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Normalization of C int type declarations
  --------------------------------------------------------------------------------------------------------------------*/
  implicit def stringIsReader(input: String): scala.util.parsing.input.Reader[Char] =
    new CharArrayReader(input.toCharArray)

  def failIfFailed(result: scala.util.parsing.combinator.Parsers#ParseResult[ParseType]) {
    if (!result.successful)
      fail(result.asInstanceOf[scala.util.parsing.combinator.Parsers#NoSuccess].msg)
  }

  @Test def shortIntIsShort = {
    val shortInt = parserShared.cTypeIntegerNumber("short int")
    failIfFailed(shortInt)
    val short = parserShared.cTypeIntegerNumber("short")
    failIfFailed(short)
    assertEquals(shortInt.get, short.get)
  }

  @Test def signedIsInt = {
    val signed = parserShared.cTypeIntegerNumber("signed")
    failIfFailed(signed)
    val int = parserShared.cTypeIntegerNumber("int")
    failIfFailed(int)
    assertEquals(signed.get, int.get)
  }

  @Test def signedLongIsLongInt = {
    val signedLong = parserShared.cTypeIntegerNumber("signed long")
    failIfFailed(signedLong)
    val longInt = parserShared.cTypeIntegerNumber("long int")
    failIfFailed(longInt)
    assertEquals(signedLong.get, longInt.get)
  }

  @Test def uintIsUnsigned = {
    val uint = parserShared.cTypeIntegerNumber("uint")
    failIfFailed(uint)
    val unsigned = parserShared.cTypeIntegerNumber("unsigned")
    failIfFailed(unsigned)
    assertEquals(uint.get, unsigned.get)
  }

  @Test def signedLongIntIsLong = {
    val signedLongInt = parserShared.cTypeIntegerNumber("signed long int")
    failIfFailed(signedLongInt)
    val long = parserShared.cTypeIntegerNumber("long")
    failIfFailed(long)
    assertEquals(signedLongInt.get, long.get)
  }
}
