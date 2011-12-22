package org.freeciv.packetgen

import org.junit.Test
import org.junit.Assert._
import scala.inline
import util.parsing.combinator.Parsers

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
  @inline def parseTest = new ParseCCode(List("test"))

  @inline def parsesCorrectly(expression: String, parser: ParseShared) {
    parsesCorrectly(expression, parser, parser.exprs)
  }

  def parsesCorrectly[Returns](expression: String,
                               parser: ParseShared,
                               toTest: ParseShared#Parser[Returns]): ParseShared#ParseResult[Returns] = {
    val parsed = parser.parseAll(toTest.asInstanceOf[parser.Parser[Returns]], expression)
    if (!parsed.successful) {
      val notParsed = parsed.asInstanceOf[Parsers#NoSuccess]
      val lineBreakAfter = expression.indexOf("\n", notParsed.next.offset)
      val startAndFailed = expression.substring(0, lineBreakAfter)
      fail(notParsed.msg + "\n" +
        startAndFailed + "\n" +
        (" " * (notParsed.next.offset - (startAndFailed.lastIndexOf("\n") + 1))) + "^" +
        expression.substring(lineBreakAfter))
    }
    return parsed
  }

  @inline def willNotParse(expression: String, parser: ParseShared) =
    assertFalse("No failure on " + expression, parser.parseAll(parser.exprs, expression).successful)
}

class CParserSyntaxTest {
  import CParserTest._

  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testCEnum1ElementNoAssign = parsesCorrectly(cEnum1ElementNoAssign, parseTest)
  @Test def testCEnum1ElementAssign  = parsesCorrectly(cEnum1ElementAssign, parseTest)
  @Test def testCEnum3ElementsNoAssign = parsesCorrectly(cEnum3ElementsNoAssign, parseTest)
  @Test def testCEnum3ElementsAssignAll = parsesCorrectly(cEnum3ElementsAssignAll, parseTest)
  @Test def testCEnum3ElementsFirstNumbered = parsesCorrectly(cEnum3ElementsFirstNumbered, parseTest)
  @Test def testCEnum3ElementsFirstAndLastTheSame = parsesCorrectly( cEnum3ElementsFirstAndLastTheSame, parseTest)
  @Test def testCEnum1CommentCxxBefore = parsesCorrectly(commentCxxOneLine + cEnum1ElementNoAssign, parseTest)
  @Test def testCEnum1CommentCxxAfter = parsesCorrectly(cEnum1ElementNoAssign + commentCxxOneLine, parseTest)
  @Test def testCEnum1CommentCBefore = parsesCorrectly(commentCOneLine + cEnum1ElementNoAssign, parseTest)
  @Test def testCEnum1CommentCAfter = parsesCorrectly(cEnum1ElementNoAssign + commentCOneLine, parseTest)
  @Test def testCEnumCommentInside = parsesCorrectly(cEnum3ElementsCommentInside, parseTest)
  @Test def testCEnumCommentInsideBefore = parsesCorrectly(cEnum3ElementsCommentInsideBefore, parseTest)
  @Test def testCEnumCommentInsideAfter = parsesCorrectly(cEnum3ElementsCommentInsideAfter, parseTest)

  @Test def testCEnumNotLookedFor = willNotParse(cEnum3ElementsNoAssign.replace("test", "notTest"), parseTest)


  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with SPECENUM
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testSpecEnum2Elements =
    parsesCorrectly(specEnum2Elements, parseTest)
  @Test def testSpecEnumTwoNamedElements =
    parsesCorrectly(specEnumTwoNamedElements, parseTest)
  @Test def testSpecEnum3ElementsBitwise =
    parsesCorrectly(specEnum3ElementsBitwise, parseTest)
  @Test def testSpecEnum4ElementsBitwiseZero =
    parsesCorrectly(specEnum4ElementsBitwiseZero, parseTest)
  @Test def testSpecEnum2ElementsBitwiseNamedZero =
    parsesCorrectly(specEnum2ElementsBitwiseNamedZero, parseTest)
  @Test def testSpecEnum2ElementsCount =
    parsesCorrectly(specEnum2ElementsCount, parseTest)
  @Test def testSpecEnum2ElementsNamedCount =
    parsesCorrectly(specEnum2ElementsNamedCount, parseTest)
  @Test def testSpecEnum2ElementsInvalid =
    parsesCorrectly(specEnum2ElementsInvalid, parseTest)
  @Test def testSpecEnumCommentInDef =
    parsesCorrectly(specEnumTwoNamedElementsWithComments, parseTest)
  @Test def testSpecEnumCommentBeforeAndAfterDef =
    parsesCorrectly(specEnumTwoNamedElementsWithCommentBeforeAndAfter, parseTest)
}

class CParserSemanticTest {
  import CParserTest._

  /*--------------------------------------------------------------------------------------------------------------------
  Common helper methods
  --------------------------------------------------------------------------------------------------------------------*/
  @inline private def checkElement(element: ClassWriter.EnumElement, nameInCode: String, number: Int, toStringName: String) {
    assertNotNull("Element " + nameInCode + " don't exist", element)
    assertEquals("Wrong name in code for element " + nameInCode, nameInCode, element.getEnumValueName)
    assertEquals("Wrong number for element " + nameInCode, number, element.getNumber)
    assertEquals("Wrong toString() value for element " + nameInCode, toStringName, element.getToStringName)
    assertTrue("Element " + nameInCode + " should be valid", element.isValid)
  }

  @inline def parseEnumCorrectly(expression: String,
                                     parser: ParseShared,
                                     converter: ParseShared#Parser[Enum],
                                     isBitWise: Boolean,
                                     values: (String, Int,  String)*): Enum = {
    val result = parsesCorrectly(expression, parser, converter).get

    assertEquals("Wrong name for enumeration class", "test", result.getEnumClassName)
    assertTrue("Wrong bitwise for enumeration class", result.isBitwise == isBitWise)

    values.foreach({
      case (nameInCode: String, number: Int, toStringName: String) =>
        val element = result.getEnumValue(nameInCode)
        checkElement(element, nameInCode, number, toStringName)
      case _ => throw new IllegalArgumentException("Method signature updated without fixing element testing?")
    })

    return result
  }

  /*--------------------------------------------------------------------------------------------------------------------
  Test semantics of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @inline def parsesCEnumCorrectly(expression: String, parser: ParseCCode, values: (String, Int,  String)*): Enum  = {
    return parseEnumCorrectly(expression, parser, parser.cEnumDefConverted, false, values: _*)
  }

  @Test def testCEnum1ElementNoAssign: Unit = {
    parsesCEnumCorrectly(cEnum1ElementNoAssign, parseTest, ("one", 0, "\"one\""))
  }

  @Test def testCEnum1ElementAssign: Unit  = {
    parsesCEnumCorrectly(cEnum1ElementAssign, parseTest, ("one", 1, "\"one\""))
  }

  @Test def testCEnum3ElementsNoAssign: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsNoAssign, parseTest,
      ("one", 0, "\"one\""),
      ("two", 1, "\"two\""),
      ("three", 2, "\"three\""))
  }

  @Test def testCEnum3ElementsAssignAll: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsAssignAll, parseTest,
      ("one", 1, "\"one\""),
      ("two", 2, "\"two\""),
      ("three", 3, "\"three\""))
  }

  @Test def testCEnum3ElementsFirstNumbered: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsFirstNumbered, parseTest,
      ("two", 2, "\"two\""),
      ("three", 3, "\"three\""),
      ("four", 4, "\"four\""))
  }

  @Test def testCEnum3ElementsFirstAndLastTheSame: Unit = {
    parsesCEnumCorrectly(cEnum3ElementsFirstAndLastTheSame, parseTest,
      ("zero", 0, "\"zero\""),
      ("one", 1, "\"one\""),
      ("null", 0, "\"null\""))
  }


  /*--------------------------------------------------------------------------------------------------------------------
  Test semantics of enums declared with SPECENUM
  --------------------------------------------------------------------------------------------------------------------*/
  @inline def parsesSpecEnumCorrectly(expression: String, parser: ParseCCode, isBitWise: Boolean, values: (String, Int,  String)*): Enum  = {
    return parseEnumCorrectly(expression, parser, parser.specEnumDefConverted, isBitWise, values: _*)
  }

  @Test(expected = classOf[UndefinedException])
  def testSpecEnumEmpty: Unit = {
    parsesSpecEnumCorrectly("""
  #define SPECENUM_NAME test
  #include "specenum_gen.h"
  """, parseTest, false)
  }

  @Test def testSpecEnum2Elements: Unit = {
    parsesSpecEnumCorrectly(specEnum2Elements, parseTest, false,
      ("ZERO", 0, "\"ZERO\""),
      ("ONE", 1, "\"ONE\""))
  }

  @Test def testSpecEnum2NamedElements: Unit = {
    parsesSpecEnumCorrectly(specEnumTwoNamedElements, parseTest, false,
      ("ZERO", 0, "\"nothing\""),
      ("ONE", 1, "\"something\""))
  }

  @Test def testSpecEnum3ElementsBitwise: Unit = {
    parsesSpecEnumCorrectly(specEnum3ElementsBitwise, parseTest, true,
      ("ONE", 1, "\"ONE\""),
      ("TWO", 2, "\"TWO\""),
      ("THREE", 4, "\"THREE\""))
  }

  @Test def testSpecEnum4ElementsBitwiseZero: Unit = {
    parsesSpecEnumCorrectly(specEnum4ElementsBitwiseZero, parseTest, true,
      ("ZERO", 0, "\"ZERO\""),
      ("ONE", 1, "\"ONE\""),
      ("TWO", 2, "\"TWO\""),
      ("THREE", 4, "\"THREE\""))
  }

  @Test def testSpecEnum3ElementsBitwiseNamedZero: Unit = {
    parsesSpecEnumCorrectly(specEnum2ElementsBitwiseNamedZero, parseTest, true,
      ("ZERO", 0, "\"nothing\""),
      ("ONE", 1, "\"ONE\""),
      ("TWO", 2, "\"TWO\""))
  }

  @Test def testSpecEnum2ElementsInvalid: Unit = {
    val enum = parsesSpecEnumCorrectly(specEnum2ElementsInvalid, parseTest, false,
      ("ONE", 0, "\"ONE\""),
      ("TWO", 1, "\"TWO\""))
    @inline val invalid = enum.getInvalidDefault
    assertNotNull("No invalid element found", invalid)
    assertFalse("The invalid element should be invalid", invalid.isValid)
    assertEquals("Wrong invalid number", -2, invalid.getNumber)
  }

  @Test def testSpecEnum2ElementsCount: Unit = {
    val enum = parsesSpecEnumCorrectly(specEnum2ElementsCount, parseTest, false,
      ("ONE", 0, "\"ONE\""),
      ("TWO", 1, "\"TWO\""))
    assertNotNull("No count element found", enum.getCount)
    assertEquals("Wrong name in code for count element", "NUMBER_OF", enum.getCount.getEnumValueName)
    assertEquals("Wrong number for count element", 2, enum.getCount.getNumber)
    assertEquals("Wrong toString() value for count element", "\"NUMBER_OF\"", enum.getCount.getToStringName)
    assertFalse("The count element should be invalid", enum.getCount.isValid)
  }

  @Test def testSpecEnum2ElementsNamedCount: Unit = {
    val enum = parsesSpecEnumCorrectly(specEnum2ElementsNamedCount, parseTest, false,
      ("ONE", 0, "\"ONE\""),
      ("TWO", 1, "\"TWO\""))
    assertNotNull("No count element found", enum.getCount)
    assertEquals("Wrong name in code for count element", "NUMBER_OF", enum.getCount.getEnumValueName)
    assertEquals("Wrong number for count element", 2, enum.getCount.getNumber)
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
  """, parseTest, false,
      ("ONE", 0, "\"ONE\""),
      ("TWO", 1, "\"TWO\""))

    assertNotNull("No invalid element found", enum.getInvalidDefault)
    assertFalse("The invalid element should be invalid", enum.getInvalidDefault.isValid)
    assertEquals("Wrong invalid number", -2, enum.getInvalidDefault.getNumber)

    assertNotNull("No count element found", enum.getCount)
    assertEquals("Wrong name in code for count element", "ELEMENTS", enum.getCount.getEnumValueName)
    assertEquals("Wrong number for count element,", 2, enum.getCount.getNumber)
    assertEquals("Wrong toString() value for count element", "\"ELEMENTS\"", enum.getCount.getToStringName)
    assertFalse("The count element should be invalid", enum.getCount.isValid)
  }
}
