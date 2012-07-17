/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.dependency.Requirement
import org.freeciv.packetgen.enteties.Enum
import org.freeciv.packetgen.enteties.Enum.EnumElementFC
import org.freeciv.packetgen.javaGenerator.ClassWriter
import org.junit.Test
import org.junit.Assert._
import scala.inline
import util.parsing.combinator.Parsers
import util.parsing.input.CharArrayReader
import java.util.Collection
import org.freeciv.packetgen.{UndefinedException, GeneratorDefaults}

object CParserTest {
  /*--------------------------------------------------------------------------------------------------------------------
  Constants for pure parsing tests of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  def cEnum1ElementNoAssign = """enum test {
    one
  }"""

  def cEnum1ElementAssign = """enum test {
    one = 1
  }"""

  def cEnum3ElementsNoAssign = """enum test {
    one,
    two,
    three
  }"""
  def cEnum3ElementsAssignAll = """enum test {
    one = 1,
    two = 2,
    three = 3
  }"""

  def cEnum3ElementsFirstNumbered = """enum test {
    two = 2,
    three,
    four
  }"""

  def cEnum3ElementsFirstAndLastTheSame = """enum test {
    zero,
    one,
    null = 0
  }"""

  def cEnum3ElementsCommentInside = """enum test {
    one,
    /* comment */
    two,
    three
  }"""

  def cEnum3ElementsCommentInsideBefore = """enum test {
    /* comment */
    one,
    two,
    three
  }"""

  def cEnum3ElementsCommentInsideAfter = """enum test {
    one,
    two,
    three
    /* comment */
  }"""

  def cEnumHavingExternalStartValue = """
    enum refersToConstant {
      ONE = START_VALUE,
      TWO
    }"""

  def cEnumStartValueIsAnExpressionInvolvingExternal = """
    enum refersToConstant {
      FIRST = START_VALUE * 16,
      NEXT
    }"""

  def cEnumHavingExternalStartValueRefersBack = """
    enum refersToConstant {
      FIRST = 1,
      NEXT = FIRST * 3
    }"""

  def commentCxxOneLine = "// A comment" + "\n"
  def commentCOneLine = "/* A comment */" + "\n"


  /*--------------------------------------------------------------------------------------------------------------------
  Constants for pure parsing tests of enums declared with SPECENUM
  --------------------------------------------------------------------------------------------------------------------*/
  def specEnum2Elements = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO
  #define SPECENUM_VALUE1 ONE
  #include "specenum_gen.h"
  """

  def specEnumTwoNamedElements = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO
  #define SPECENUM_VALUE0NAME "nothing"
  #define SPECENUM_VALUE1 ONE
  #define SPECENUM_VALUE1NAME "something"
  #include "specenum_gen.h"
  """

  def specEnumNamedElementWithSpace = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO
  #define SPECENUM_VALUE1 ONE
  #define SPECENUM_VALUE1NAME "this should work"
  #include "specenum_gen.h"
  """

  def specEnum3ElementsBitwise = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #define SPECENUM_VALUE2 THREE
  #include "specenum_gen.h"
  """

  def specEnum4ElementsBitwiseZero = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_ZERO ZERO
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #define SPECENUM_VALUE2 THREE
  #include "specenum_gen.h"
  """

  def specEnum2ElementsBitwiseNamedZero = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_ZERO ZERO
  #define SPECENUM_ZERONAME "nothing"
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  def specEnum2ElementsCount = """
  #define SPECENUM_NAME test
  #define SPECENUM_COUNT NUMBER_OF
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  def specEnum2ElementsNamedCount = """
  #define SPECENUM_NAME test
  #define SPECENUM_COUNT NUMBER_OF
  #define SPECENUM_COUNTNAME "last"
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  def specEnum2ElementsInvalid = """
  #define SPECENUM_NAME test
  #define SPECENUM_INVALID -2
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  def specEnumTwoNamedElementsWithComments = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO // C++ style comment
  #define SPECENUM_VALUE0NAME "nothing"
  #define SPECENUM_VALUE1 ONE /* C style comment */
  #define SPECENUM_VALUE1NAME "something"
  #include "specenum_gen.h"
  """

  def specEnumTwoNamedElementsWithCommentBeforeAndAfter = """
  #define SPECENUM_NAME test
  // C++ style comment
  #define SPECENUM_VALUE0 ZERO
  #define SPECENUM_VALUE0NAME "nothing"
  #define SPECENUM_VALUE1 ONE
  #define SPECENUM_VALUE1NAME "something"
  /* C style comment */
  #include "specenum_gen.h"
  """


  /*--------------------------------------------------------------------------------------------------------------------
  Common helper methods
  --------------------------------------------------------------------------------------------------------------------*/
  @inline def parseEnumTest = ParseCCode

  @inline def parsesCorrectly(expression: String, parser: ParseShared) {
    parsesCorrectly(expression, parser, parser.exprs)
  }

  def assertParesSuccess[Returns](expression: String, parsed: ParseShared#ParseResult[Returns]) = {
    if (!parsed.successful) {
      val notParsed = parsed.asInstanceOf[Parsers#NoSuccess]
      val lineBreakAfterIfExist = expression.indexOf("\n", notParsed.next.offset)
      val lineBreakAfter = if (-1 < lineBreakAfterIfExist)
        lineBreakAfterIfExist
      else
        expression.length()
      val startAndFailed = expression.substring(0, lineBreakAfter)
      fail(notParsed.msg + "\n" +
        startAndFailed + "\n" +
        (" " * (notParsed.next.offset - (startAndFailed.lastIndexOf("\n") + 1))) + "^" +
        expression.substring(lineBreakAfter))
    }
  }

  def parsesCorrectly[Returns](expression: String,
                               parser: ParseShared,
                               toTest: ParseShared#Parser[Returns]): ParseShared#ParseResult[Returns] = {
    val parsed = parser.parseAll(toTest.asInstanceOf[parser.Parser[Returns]], expression)
    assertParesSuccess(expression, parsed)
    return parsed
  }

  @inline def assertPrefixWillNotParse(expression: String, parser: ParseShared) =
    assertFalse("No failure on " + expression,
      parser.parse(parser.expr, new parser.PackratReader(new CharArrayReader(expression.toArray))).successful)
}

class CParserSyntaxTest {
  import CParserTest._

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testCEnum1ElementNoAssign = parsesCorrectly(cEnum1ElementNoAssign, parseEnumTest)
  @Test def testCEnum1ElementAssign  = parsesCorrectly(cEnum1ElementAssign, parseEnumTest)
  @Test def testCEnum3ElementsNoAssign = parsesCorrectly(cEnum3ElementsNoAssign, parseEnumTest)
  @Test def testCEnum3ElementsAssignAll = parsesCorrectly(cEnum3ElementsAssignAll, parseEnumTest)
  @Test def testCEnum3ElementsFirstNumbered = parsesCorrectly(cEnum3ElementsFirstNumbered, parseEnumTest)
  @Test def testCEnum3ElementsFirstAndLastTheSame = parsesCorrectly( cEnum3ElementsFirstAndLastTheSame, parseEnumTest)
  @Test def testCEnum1CommentCxxBefore = parsesCorrectly(commentCxxOneLine + cEnum1ElementNoAssign, parseEnumTest)
  @Test def testCEnum1CommentCxxAfter = parsesCorrectly(cEnum1ElementNoAssign + commentCxxOneLine, parseEnumTest)
  @Test def testCEnum1CommentCBefore = parsesCorrectly(commentCOneLine + cEnum1ElementNoAssign, parseEnumTest)
  @Test def testCEnum1CommentCAfter = parsesCorrectly(cEnum1ElementNoAssign + commentCOneLine, parseEnumTest)
  @Test def testCEnumCommentInside = parsesCorrectly(cEnum3ElementsCommentInside, parseEnumTest)
  @Test def testCEnumCommentInsideBefore = parsesCorrectly(cEnum3ElementsCommentInsideBefore, parseEnumTest)
  @Test def testCEnumCommentInsideAfter = parsesCorrectly(cEnum3ElementsCommentInsideAfter, parseEnumTest)
  @Test def testCEnumCommaAfterLast = parsesCorrectly("enum test {element, iEndInAComma,}", parseEnumTest)


  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with SPECENUM
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testSpecEnum2Elements =
    parsesCorrectly(specEnum2Elements, parseEnumTest)
  @Test def testSpecEnumTwoNamedElements =
    parsesCorrectly(specEnumTwoNamedElements, parseEnumTest)
  @Test def testSpecEnum3ElementsBitwise =
    parsesCorrectly(specEnum3ElementsBitwise, parseEnumTest)
  @Test def testSpecEnum4ElementsBitwiseZero =
    parsesCorrectly(specEnum4ElementsBitwiseZero, parseEnumTest)
  @Test def testSpecEnum2ElementsBitwiseNamedZero =
    parsesCorrectly(specEnum2ElementsBitwiseNamedZero, parseEnumTest)
  @Test def testSpecEnum2ElementsCount =
    parsesCorrectly(specEnum2ElementsCount, parseEnumTest)
  @Test def testSpecEnum2ElementsNamedCount =
    parsesCorrectly(specEnum2ElementsNamedCount, parseEnumTest)
  @Test def testSpecEnum2ElementsInvalid =
    parsesCorrectly(specEnum2ElementsInvalid, parseEnumTest)
  @Test def testSpecEnumCommentInDef =
    parsesCorrectly(specEnumTwoNamedElementsWithComments, parseEnumTest)
  @Test def testSpecEnumCommentBeforeAndAfterDef =
    parsesCorrectly(specEnumTwoNamedElementsWithCommentBeforeAndAfter, parseEnumTest)
  @Test def testSpecEnumNamedElementWithSpace =
    parsesCorrectly(specEnumNamedElementWithSpace, parseEnumTest)
  @Test def specEnumTwoCommentsInARowBetweenElements =
    parsesCorrectly("""
#define SPECENUM_NAME hasTwoComments
#define SPECENUM_VALUE0 FIRST
#define SPECENUM_VALUE0NAME "alpha"
/* Put more between */
/* not just one more */
#define SPECENUM_VALUE1 LAST
#define SPECENUM_VALUE1NAME "omega"
#include "specenum_gen.h"
    """, ParseCCode)
  @Test def specEnumMultiLineCommentAtTheEnd =
    parsesCorrectly("""
#define SPECENUM_NAME hasTwoComments
#define SPECENUM_VALUE0 ELEMENT
/* Multi
 * line
 * comment */
#define SPECENUM_VALUE1 ELEMENT2
#include "specenum_gen.h"
    """, ParseCCode)

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of constants
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def constantDefinedSimleNumber =
    parsesCorrectly("#define SIMPLE 5", ParseCCode)
  @Test def constantDefinedSimleNumberWhitspaceDifferent =
    parsesCorrectly("#define  SIMPLE  5\n", ParseCCode)
  @Test def constantWrongValueFails =
    assertPrefixWillNotParse("#define WRONG 5 +\n10\n", ParseCCode)
  @Test def constantDefinedTwoSimleNumbers =
    parsesCorrectly("#define SIMPLE 5\n#define OTHER 7",
      ParseCCode)
  @Test def constantDefinedTwoSimpleNumbersComments =
    parsesCorrectly("#define SIMPLE 5//comment C++ style\n#define OTHER /* Comment C style */ 7",
      ParseCCode)
  @Test def constantDefinedAddition =
    parsesCorrectly("#define SIMPLE 2 + 5", ParseCCode)
  @Test def constantDefinedOther =
    parsesCorrectly("#define SIMPLE WRONG", ParseCCode)
  @Test def constantDefinedOtherTimes =
    parsesCorrectly("#define SIMPLE WRONG * 2", ParseCCode)
  @Test def constantDefinedManyParts =
    parsesCorrectly("#define COMPLEX WRONG * 2 + SIMPLE", ParseCCode)
  @Test def constantAfterTwoNewLinesWithFollowingItem {
    val parser = ParseCCode
    val input = """

#define CONS 5
#define NotLookedFor
"""
    assertParesSuccess(input, parser.parse(parser.expr, new parser.PackratReader(new CharArrayReader(input.toArray))))
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of typedefs
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def typedefSimpleInt =
    parsesCorrectly("typedef int not_complicated;", ParseCCode)

  @Test def typedefUnsignedInt =
    parsesCorrectly("typedef unsigned int more_complicated;", ParseCCode)

  @Test def typedefEnum =
    parsesCorrectly("typedef enum test more_complicated;", ParseCCode)

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of typedefs
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def bvIntegerLong = parsesCorrectly("BV_DEFINE(bv_test, 8);", ParseCCode)
  @Test def bvAConstantLong = parsesCorrectly("BV_DEFINE(bv_test, CONSTANT);", ParseCCode)
  @Test def bvAConstantAddIntLong = parsesCorrectly("BV_DEFINE(bv_test, CONSTANT + 1);", ParseCCode)

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of structs
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def structOneFieldPrimitive =
    parsesCorrectly("""struct justOne {bool value;};""",
      ParseCCode)

  @Test def structOneFieldEnum =
    parsesCorrectly("""struct justOne {enum test value;};""",
      ParseCCode)

  @Test def structTwoFieldsPrimitive =
    parsesCorrectly("""struct two {bool value1; int value2;};""",
      ParseCCode)

  @Test def structTwoFieldsEnum =
    parsesCorrectly("""
struct two {
  enum test value1;
  enum bitwise value2;
};
    """,
      ParseCCode)

  @Test def structTwoFieldsCommented =
    parsesCorrectly("""
struct two {
  int value1; // C++ comment
  enum bitwise value2; /* C style comment */
};
    """,
      ParseCCode)
}

class CParserSemanticTest {
  import CParserTest._

  /*--------------------------------------------------------------------------------------------------------------------
  Common helper methods
  --------------------------------------------------------------------------------------------------------------------*/
  @inline private def checkElement(element: EnumElementFC, nameInCode: String, number: String, toStringName: String) {
    assertNotNull("Element " + nameInCode + " don't exist", element)
    assertEquals("Wrong name in code for element " + nameInCode, nameInCode, element.getEnumValueName)
    assertEquals("Wrong number for element " + nameInCode, number, element.getValueGenerator)
    assertEquals("Wrong toString() value for element " + nameInCode, toStringName, element.getToStringName)
    assertTrue("Element " + nameInCode + " should be valid", element.isValid)
  }

  @inline def parseEnumCorrectly(expression: String,
                                     parser: ParseShared,
                                     converter: ParseShared#Parser[Enum],
                                     isBitWise: Boolean,
                                     values: (String, String, String)*): Enum = {
    val result = parsesCorrectly(expression, parser, converter).get

    assertEquals("Wrong name for enumeration class", "test", result.getName)
    assertTrue("Wrong bitwise for enumeration class", result.isBitwise == isBitWise)

    values.foreach({
      case (nameInCode: String, number: String, toStringName: String) =>
        val element = result.getEnumValue(nameInCode)
        checkElement(element, nameInCode, number, toStringName)
      case _ => throw new IllegalArgumentException("Method signature updated without fixing element testing?")
    })

    return result
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test semantics of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @inline def parsesCEnumCorrectly(expression: String, values: (String, String,  String)*): Enum  = {
    return parseEnumCorrectly(expression, ParseCCode, ParseCCode.cEnumDefConverted, false, values: _*)
  }

  @Test def testCEnum1ElementNoAssign: Unit = {
    parsesCEnumCorrectly(cEnum1ElementNoAssign, ("one", "0", "\"one\""))
  }

  @Test def testCEnum1ElementAssign: Unit  = {
    parsesCEnumCorrectly(cEnum1ElementAssign, ("one", "1", "\"one\""))
  }

  @Test def testCEnum3ElementsNoAssign: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsNoAssign,
      ("one", "0", "\"one\""),
      ("two", "1 + one.getNumber()", "\"two\""),
      ("three", "1 + two.getNumber()", "\"three\""))
  }

  @Test def testCEnum3ElementsAssignAll: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsAssignAll,
      ("one", "1", "\"one\""),
      ("two", "2", "\"two\""),
      ("three", "3", "\"three\""))
  }

  @Test def testCEnum3ElementsFirstNumbered: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsFirstNumbered,
      ("two", "2", "\"two\""),
      ("three", "1 + two.getNumber()", "\"three\""),
      ("four", "1 + three.getNumber()", "\"four\""))
  }

  @Test def testCEnum3ElementsFirstAndLastTheSame: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsFirstAndLastTheSame,
      ("zero", "0", "\"zero\""),
      ("one", "1 + zero.getNumber()", "\"one\""),
      ("null", "0", "\"null\""))
  }

  @Test def testCDefineElementAsEqualPreviouslyDefined: Unit = {
    parsesCEnumCorrectly("enum test {zero, one, null = zero}",
      ("zero", "0", "\"zero\""),
      ("one", "1 + zero.getNumber()", "\"one\""),
      ("null", "zero.getNumber()", "\"null\""))
  }

  @Test def cEnumNeedingConstant {
    val parser = ParseCCode
    val result = parsesCorrectly(cEnumHavingExternalStartValue, parser, parser.exprConverted)
    assertTrue("C enum based on external constant should depend on it",
      result.get.getReqs.contains(new Requirement("START_VALUE", Requirement.Kind.VALUE)))
  }

  @Test def cEnumElementParanoidValueGeneratorSimple {
    val parser = ParseCCode
    val result = parsesCorrectly(cEnumHavingExternalStartValue, parser, parser.exprConverted)

    assertTrue("C enum based on external constant should depend on it",
      result.get.getReqs.contains(new Requirement("START_VALUE", Requirement.Kind.VALUE)))

    assertEquals("Should get value of constant",
      GeneratorDefaults.CONSTANT_LOCATION + ".START_VALUE",
      result.get.asInstanceOf[Enum].getEnumValue("ONE").getValueGenerator)

    assertEquals("Should get value of constant",
      "1 + ONE.getNumber()",
      result.get.asInstanceOf[Enum].getEnumValue("TWO").getValueGenerator)
  }

  @Test def cEnumElementParanoidValueGeneratorExpression {
    val parser = ParseCCode
    val result = parsesCorrectly(cEnumStartValueIsAnExpressionInvolvingExternal, parser, parser.exprConverted)

    assertTrue("C enum based on external constant should depend on it",
      result.get.getReqs.contains(new Requirement("START_VALUE", Requirement.Kind.VALUE)))

    assertEquals("Should contain calculation",
      GeneratorDefaults.CONSTANT_LOCATION + ".START_VALUE * 16",
      result.get.asInstanceOf[Enum].getEnumValue("FIRST").getValueGenerator)

    assertEquals("Should get value of constant",
      "1 + FIRST.getNumber()",
      result.get.asInstanceOf[Enum].getEnumValue("NEXT").getValueGenerator)
  }

  @Test def cEnumElementParanoidValueGeneratorPreviousExpression {
    val parser = ParseCCode
    val result = parsesCorrectly(cEnumHavingExternalStartValueRefersBack, parser, parser.exprConverted)
    assertTrue("Shouldn't depends on anything as all is numbers or internal constants",
      result.get.getReqs.isEmpty)

    assertEquals("Wrong value",
      "1",
      result.get.asInstanceOf[Enum].getEnumValue("FIRST").getValueGenerator)

    assertEquals("Should get value of constant",
      "FIRST.getNumber() * 3",
      result.get.asInstanceOf[Enum].getEnumValue("NEXT").getValueGenerator)
  }

  @Test def cEnumElementParanoidValueGeneratorFirstIsSane {
    val parser = ParseCCode
    val result = parsesCorrectly("""
enum implicitFirst {
  FIRST,
  NEXT = FIRST + 32
}
    """, parser, parser.exprConverted)
    assertTrue("Shouldn't depends on anything as all is numbers or internal constants",
      result.get.getReqs.isEmpty)

    assertEquals("Wrong value",
      "0",
      result.get.asInstanceOf[Enum].getEnumValue("FIRST").getValueGenerator)

    assertEquals("Should get value of constant",
      "FIRST.getNumber() + 32",
      result.get.asInstanceOf[Enum].getEnumValue("NEXT").getValueGenerator)
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test semantics of enums declared with SPECENUM
  --------------------------------------------------------------------------------------------------------------------*/
  @inline def parsesSpecEnumCorrectly(expression: String, isBitWise: Boolean, values: (String, String, String)*): Enum  = {
    return parseEnumCorrectly(expression, ParseCCode, ParseCCode.specEnumDefConverted, isBitWise, values: _*)
  }

  @Test(expected = classOf[UndefinedException])
  def testSpecEnumEmpty: Unit = {
    parsesSpecEnumCorrectly("""
  #define SPECENUM_NAME test
  #include "specenum_gen.h"
  """, false)
  }

  @Test def testSpecEnum2Elements: Unit = {
    parsesSpecEnumCorrectly(specEnum2Elements, false,
      ("ZERO", "0", "\"ZERO\""),
      ("ONE", "1", "\"ONE\""))
  }

  @Test def testSpecEnum2NamedElements: Unit = {
    parsesSpecEnumCorrectly(specEnumTwoNamedElements, false,
      ("ZERO", "0", "\"nothing\""),
      ("ONE", "1", "\"something\""))
  }

  @Test def testSpecEnum3ElementsBitwise: Unit = {
    parsesSpecEnumCorrectly(specEnum3ElementsBitwise, true,
      ("ONE", "1", "\"ONE\""),
      ("TWO", "2", "\"TWO\""),
      ("THREE", "4", "\"THREE\""))
  }

  @Test def testSpecEnum4ElementsBitwiseZero: Unit = {
    parsesSpecEnumCorrectly(specEnum4ElementsBitwiseZero, true,
      ("ZERO", "0", "\"ZERO\""),
      ("ONE", "1", "\"ONE\""),
      ("TWO", "2", "\"TWO\""),
      ("THREE", "4", "\"THREE\""))
  }

  @Test def testSpecEnum3ElementsBitwiseNamedZero: Unit = {
    parsesSpecEnumCorrectly(specEnum2ElementsBitwiseNamedZero, true,
      ("ZERO", "0", "\"nothing\""),
      ("ONE", "1", "\"ONE\""),
      ("TWO", "2", "\"TWO\""))
  }

  @Test def testSpecEnum2ElementsInvalid: Unit = {
    val enum = parsesSpecEnumCorrectly(specEnum2ElementsInvalid, false,
      ("ONE", "0", "\"ONE\""),
      ("TWO", "1", "\"TWO\""))
    @inline val invalid = enum.getInvalidDefault
    assertNotNull("No invalid element found", invalid)
    assertFalse("The invalid element should be invalid", invalid.isValid)
    assertEquals("Wrong invalid number", "-2", invalid.getValueGenerator)
  }

  @Test def testSpecEnum2ElementsCount: Unit = {
    val enum = parsesSpecEnumCorrectly(specEnum2ElementsCount, false,
      ("ONE", "0", "\"ONE\""),
      ("TWO", "1", "\"TWO\""))
    assertNotNull("No count element found", enum.getCount)
    assertEquals("Wrong name in code for count element", "NUMBER_OF", enum.getCount.getEnumValueName)
    assertEquals("Wrong number for count element", "2", enum.getCount.getValueGenerator)
    assertEquals("Wrong toString() value for count element", "\"NUMBER_OF\"", enum.getCount.getToStringName)
    assertFalse("The count element should be invalid", enum.getCount.isValid)
  }

  @Test def testSpecEnum2ElementsNamedCount: Unit = {
    val enum = parsesSpecEnumCorrectly(specEnum2ElementsNamedCount, false,
      ("ONE", "0", "\"ONE\""),
      ("TWO", "1", "\"TWO\""))
    assertNotNull("No count element found", enum.getCount)
    assertEquals("Wrong name in code for count element", "NUMBER_OF", enum.getCount.getEnumValueName)
    assertEquals("Wrong number for count element", "2", enum.getCount.getValueGenerator)
    assertEquals("Wrong toString() value for count element", "\"last\"", enum.getCount.getToStringName)
    assertFalse("The count element should be invalid", enum.getCount.isValid)
  }

  @Test def testSpecEnum2ElementsCountInvalid: Unit = {
    val enum = parsesSpecEnumCorrectly("""
  #define SPECENUM_NAME test
  #define SPECENUM_INVALID -2
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #define SPECENUM_COUNT ELEMENTS
  #include "specenum_gen.h"
  """, false,
      ("ONE", "0", "\"ONE\""),
      ("TWO", "1", "\"TWO\""))

    assertNotNull("No invalid element found", enum.getInvalidDefault)
    assertFalse("The invalid element should be invalid", enum.getInvalidDefault.isValid)
    assertEquals("Wrong invalid number", "-2", enum.getInvalidDefault.getValueGenerator)

    assertNotNull("No count element found", enum.getCount)
    assertEquals("Wrong name in code for count element", "ELEMENTS", enum.getCount.getEnumValueName)
    assertEquals("Wrong number for count element,", "2", enum.getCount.getValueGenerator)
    assertEquals("Wrong toString() value for count element", "\"ELEMENTS\"", enum.getCount.getToStringName)
    assertFalse("The count element should be invalid", enum.getCount.isValid)
  }

  @Test def testSpecEnumCountInvalidSortedRight: Unit = {
    val enum = parsesSpecEnumCorrectly("""
  #define SPECENUM_NAME test
  #define SPECENUM_INVALID -2
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #define SPECENUM_VALUE2 THREE
  #define SPECENUM_COUNT ELEMENTS
  #include "specenum_gen.h"
  """, false,
      ("ONE", "0", "\"ONE\""),
      ("TWO", "1", "\"TWO\""))

    //  Wrong sorting (what is tested here) or just a format change?
    assertEquals("Generated enum code not as expected.",
      """package org.freeciv.types;

// This code was auto generated from Freeciv C code
public enum test implements FCEnum {
	ONE (0, "ONE"),
	TWO (1, "TWO"),
	THREE (2, "THREE"),
	ELEMENTS (3, "ELEMENTS", false),
	INVALID (-2, "INVALID", false);

	private final int number;
	private final boolean valid;
	private final String toStringName;

	private test(int number, String toStringName) {
		this(number, toStringName, true);
	}

	private test(int number, String toStringName, boolean valid) {
		this.number = number;
		this.toStringName = toStringName;
		this.valid = valid;
	}

	public int getNumber() {
		return number;
	}

	public boolean isValid() {
		return valid;
	}

	public String toString() {
		return toStringName;
	}

	/**
	 * Is the enum bitwise? An enum is bitwise if it's number increase by two's
	 * exponent.
	 * @return true if the enum is bitwise
	 */
	public static boolean isBitWise() {
		return false;
	}

""" + "\t" // Work around bug adding a tab on a blank line
+ """
	public static test valueOf(int number) {
		for (test element: values()) {
			if (element.getNumber() == number) {
				return element;
			}
		}
		return INVALID;
	}
}
""", enum.toString)
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test semantics of constants
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def constantDefinedSimpleNumber {
    val toParse = "#define SIMPLE 5"
    val parser = ParseCCode
    val result = CParserTest.parsesCorrectly(toParse, parser, parser.constantValueDefConverted)
    assertEquals("Wrong name", "SIMPLE", result.get.getName)
    assertEquals("Wrong value generation expression", "5", result.get.getExpression)
  }

  @Test def constantDefinedOther {
    val toParse = "#define SIMPLE WRONG"
    val parser = ParseCCode
    val result = CParserTest.parsesCorrectly(toParse, parser, parser.constantValueDefConverted)

    assertEquals("Wrong name", "SIMPLE", result.get.getName)
    assertEquals("Wrong value generation expression", GeneratorDefaults.CONSTANT_LOCATION + ".WRONG", result.get.getExpression)

    val reqs: Collection[Requirement] = result.get.getReqs
    assertNotNull("Didn't even generate requirements...", reqs)
    assertFalse("Should depend on WRONG", reqs.isEmpty)
    assertTrue("Should depend on WRONG", reqs.contains(new Requirement("WRONG", Requirement.Kind.VALUE)))
  }

  @Test def constantDefinedAddition {
    val toParse = "#define SIMPLE 2 + 5"
    val parser = ParseCCode
    val result = CParserTest.parsesCorrectly(toParse, parser, parser.constantValueDefConverted)

    assertEquals("Wrong name", "SIMPLE", result.get.getName)
    assertEquals("Wrong value generation expression", "2 + 5", result.get.getExpression)

    val reqs: Collection[Requirement] = result.get.getReqs
    assertNotNull("Didn't even generate requirements...", reqs)
    assertTrue("Numbers and operators should not be required", reqs.isEmpty)
  }

  @Test def constantDefinedManyParts {
    val toParse = "#define COMPLEX WRONG * 2 + SIMPLE"
    val parser = ParseCCode
    val result = CParserTest.parsesCorrectly(toParse, parser, parser.constantValueDefConverted)

    assertEquals("Wrong name", "COMPLEX", result.get.getName)
    assertEquals("Wrong value generation expression", "(" + GeneratorDefaults.CONSTANT_LOCATION + ".WRONG * 2) + " + GeneratorDefaults.CONSTANT_LOCATION + ".SIMPLE", result.get.getExpression)

    val reqs: Collection[Requirement] = result.get.getReqs
    assertNotNull("Didn't even generate requirements...", reqs)
    assertFalse("Should depend on two other values", reqs.isEmpty)
    assertTrue("Should depend on WRONG", reqs.contains(new Requirement("WRONG", Requirement.Kind.VALUE)))
    assertTrue("Should depend on WRONG", reqs.contains(new Requirement("SIMPLE", Requirement.Kind.VALUE)))
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of typedefs
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def bvInteger = {
    val parser = ParseCCode
    val result = parsesCorrectly("BV_DEFINE(bv_test, 8);", parser, parser.exprConverted).get

    assertTrue("No need for any constant", result.getReqs.isEmpty)
    assertEquals("Should provide it self",
      new Requirement("bv_test", Requirement.Kind.AS_JAVA_DATATYPE),
      result.getIFulfillReq)
  }

  @Test def bvConstant = {
    val parser = ParseCCode
    val result = parsesCorrectly("BV_DEFINE(bv_test, CONSTANT);", parser, parser.exprConverted).get

    assertTrue("Should need CONSTANT", result.getReqs.contains(
      new Requirement("CONSTANT", Requirement.Kind.VALUE)))
    assertEquals("Should provide it self",
      new Requirement("bv_test", Requirement.Kind.AS_JAVA_DATATYPE),
      result.getIFulfillReq)
  }

  @Test def bvConstantAddInteger = {
    val parser = ParseCCode
    val result = parsesCorrectly("BV_DEFINE(bv_test, CONSTANT + 1);", parser, parser.exprConverted).get

    assertTrue("Should need CONSTANT", result.getReqs.contains(
      new Requirement("CONSTANT", Requirement.Kind.VALUE)))
    assertEquals("Should provide it self",
      new Requirement("bv_test", Requirement.Kind.AS_JAVA_DATATYPE),
      result.getIFulfillReq)
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test semantics of structs
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def structOneFieldPrimitiveBoolean {
    val parser = ParseCCode
    val result = parsesCorrectly("""struct justOne {bool value;};""", parser, parser.structConverted)
    assertTrue("The primitive bool should not depend on anything", result.get.getReqs.isEmpty)
  }

  @Test def structOneFieldEnum {
    val parser = ParseCCode
    val result = parsesCorrectly("""struct justOne {enum test value;};""", parser, parser.structConverted)
    assertTrue("The enum test should be needed here",
      result.get.getReqs.contains(new Requirement("enum test", Requirement.Kind.AS_JAVA_DATATYPE)))
  }

  @Test def structTwoFieldsPrimitive {
    val parser = ParseCCode
    val result = parsesCorrectly("""struct two {bool value1; int value2;};""", parser, parser.structConverted)
    assertTrue("The primitives bool and int should not depend on anything", result.get.getReqs.isEmpty)
  }

  @Test def structTwoFieldsEnum {
    val parser = ParseCCode
    val result = parsesCorrectly("""
struct two {
  enum test value1;
  enum bitwise value2;
};
    """, parser, parser.structConverted)
    assertTrue("The enum test should be needed here",
      result.get.getReqs.contains(new Requirement("enum test", Requirement.Kind.AS_JAVA_DATATYPE)))
    assertTrue("The enum bitwise should be needed here",
      result.get.getReqs.contains(new Requirement("enum bitwise", Requirement.Kind.AS_JAVA_DATATYPE)))
  }
}

class FromCExtractorTest {
  @Test def initialize {
    val extractor = FromCExtractor
  }

  private final val test123NotingElse = """
#define SPECENUM_NAME test1
#define SPECENUM_VALUE0 ZERO
#define SPECENUM_VALUE1 ONE
#include "specenum_gen.h"

enum test2 {
  nothing,
  one,
  two,
  three
}

enum test3 {
  toBe,
  notToBe
}
"""

  @Test def findsPositionsAllExist {
    val positions = FromCExtractor
      .findPossibleStartPositions(test123NotingElse)
    assertNotNull("Positions don't exist", positions)
    assertTrue("Position missing", positions.contains(1))
    assertTrue("Position missing", positions.contains(113))
    assertTrue("Position missing", positions.contains(162))
  }

  @Test def findsEnumsAllExist {
    val enums = FromCExtractor
      .extract(test123NotingElse)
    assertNotNull("Enums not found", enums)
    assertFalse("Enums not found", enums.isEmpty)

    val enumsAsMap = enums.map(_.getIFulfillReq.getName)
    assertTrue("Specenum test1 not found", enumsAsMap.contains("enum test1"))
    assertTrue("C style enum test2 not found", enumsAsMap.contains("enum test2"))
    assertTrue("C style enum test3 not found", enumsAsMap.contains("enum test3"))
  }

  @Test def findsEnumsOneMissingExtract {
    val enums = FromCExtractor
      .extract(test123NotingElse)
    assertNotNull("Enums not found", enums)
    assertFalse("Enums not found", enums.isEmpty)

    val enumsAsMap = enums.map(_.getIFulfillReq.getName)
    assertTrue("Specenum test1 not found", enumsAsMap.contains("enum test1"))
    assertTrue("C style enum test2 not found", enumsAsMap.contains("enum test2"))
    assertTrue("C style enum test3 not found", enumsAsMap.contains("enum test3"))
  }

  private final val test123OtherCodeAsWell = """
#define SPECENUM_NAME test1
#define SPECENUM_VALUE0 ZERO
#define SPECENUM_VALUE1 ONE
#include "specenum_gen.h"

int randomVariableInTheVay = 5;

enum test2 {
  nothing,
  one,
  two,
  three
}

enum thisIsNotLookedFor {
  irrelevant,
  notRelevant
}

enum test3 {
  toBe,
  notToBe
}
"""

  @Test def findsPositionsOtherCodeAsWell {
    val positions = FromCExtractor
      .findPossibleStartPositions(test123OtherCodeAsWell)
    assertNotNull("Positions don't exist", positions)

    assertTrue("Position missing", positions.contains(1))
    assertTrue("Position missing", positions.contains(146))
    assertTrue("Position missing", positions.contains(252))
  }

  @Test def findsEnumsOtherCodeAsWell {
    val enums = FromCExtractor.extract(test123OtherCodeAsWell)
    assertNotNull("Enums not found", enums)
    assertFalse("Enums not found", enums.isEmpty)

    val enumsAsMap = enums.map(_.getIFulfillReq.getName)
    assertTrue("Specenum test1 not found", enumsAsMap.contains("enum test1"))
    assertTrue("C style enum test2 not found", enumsAsMap.contains("enum test2"))
    assertTrue("C style enum test3 not found", enumsAsMap.contains("enum test3"))
  }

  private final val test123EnumsUsed = """
#define SPECENUM_NAME test1
#define SPECENUM_VALUE0 ZERO
#define SPECENUM_VALUE1 ONE
#include "specenum_gen.h"

enum test1 randomVariableInTheVay = 1;

enum test2 {
  nothing,
  one,
  two,
  three
};

typedef enum test2 Of3;

enum test3 {
  toBe,
  notToBe
};
"""

  @Test def findsPositionsEnumsUsed {
    val positions = FromCExtractor
      .findPossibleStartPositions(test123EnumsUsed)
    assertNotNull("Positions don't exist", positions)
  }

  @Test def findsEnumsEnumsUsed {
    val enums = FromCExtractor
      .extract(test123EnumsUsed)
    assertNotNull("Enums not found", enums)
    assertFalse("Enums not found", enums.isEmpty)

    val enumsNames = enums.map(_.getIFulfillReq.getName)
    assertTrue("Specenum test1 not found", enumsNames.contains("enum test1"))
    assertTrue("C style enum test2 not found", enumsNames.contains("enum test2"))
    assertTrue("C style enum test3 not found", enumsNames.contains("enum test3"))
  }

  @Test def findConstantAfterTwoNewLinesWithFollowingUnusedConstant {
    val extractor = FromCExtractor
    val input = """

#define CONS 5
#define NotLookedFor
"""
    val found = extractor.extract(input)

    assertNotNull("CONS not found", found)
    assertFalse("CONS not found", found.isEmpty)

    val foundNames = found.map(_.getIFulfillReq.getName)
    assertTrue("CONS not found but found something else", foundNames.contains("CONS"))
  }
}
