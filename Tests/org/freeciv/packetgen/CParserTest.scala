package org.freeciv.packetgen

import org.junit.Test
import org.junit.Assert._
import scala.inline
import util.parsing.combinator.Parsers

class CParserTest {
  /*--------------------------------------------------------------------------------------------------------------------
  Constants for pure parsing tests of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  private def oneElementNoAssign = """enum test {
    one
  }"""

  private def oneElementAssign = """enum test {
    one = 1
  }"""

  private def threeElementsNoAssign = """enum test {
    one,
    two,
    three
  }"""
  private def threeElementsAssignAll = """enum test {
    one = 1,
    two = 2,
    three = 3
  }"""

  private def threeElementsFirstAndLastTheSame = """enum test {
    null = 0,
    zero,
    one
  }"""

  private def threeElementsCommentInside = """enum test {
    one,
    /* comment */
    two,
    three
  }"""

  private def threeElementsCommentInsideBefore = """enum test {
    /* comment */
    one,
    two,
    three
  }"""

  private def threeElementsCommentInsideAfter = """enum test {
    one,
    two,
    three
    /* comment */
  }"""

  private def commentCxxOneLine = "// A comment" + "\n"
  private def commentCOneLine = "/* A comment */" + "\n"


  /*--------------------------------------------------------------------------------------------------------------------
  Constants for pure parsing tests of enums declared with SPECENUM
  --------------------------------------------------------------------------------------------------------------------*/
  private def specEnumTwoElements = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO
  #define SPECENUM_VALUE1 ONE
  #include "specenum_gen.h"
  """

  private def specEnumTwoNamedElements = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO
  #define SPECENUM_VALUE0NAME "nothing"
  #define SPECENUM_VALUE1 ONE
  #define SPECENUM_VALUE1NAME "something"
  #include "specenum_gen.h"
  """

  private def specEnumTwoElementsBitwise = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnumTwoElementsBitwiseZero = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_ZERO ZERO
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnumTwoElementsBitwiseNamedZero = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_ZERO ZERO
  #define SPECENUM_ZERONAME "nothing"
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnumTwoElementsCount = """
  #define SPECENUM_NAME test
  #define SPECENUM_COUNT NUMBER_OF
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnumTwoElementsNamedCount = """
  #define SPECENUM_NAME test
  #define SPECENUM_COUNT NUMBER_OF
  #define SPECENUM_COUNTNAME "last"
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnumTwoElementsInvalid = """
  #define SPECENUM_NAME test
  #define SPECENUM_INVALID FAIL
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnumTwoNamedElementsWithComments = """
  #define SPECENUM_NAME test
  #define SPECENUM_VALUE0 ZERO // C++ style comment
  #define SPECENUM_VALUE0NAME "nothing"
  #define SPECENUM_VALUE1 ONE /* C style comment */
  #define SPECENUM_VALUE1NAME "something"
  #include "specenum_gen.h"
  """

  private def specEnumTwoNamedElementsWithCommentBeforeAndAfter = """
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
  @inline private def parseTest = new ParseCCode(List("test"))

  @inline private def parsesCorrectly(expression: String, parser: ParseShared) {
    val parsed = parser.parseAll(parser.exprs, expression)
    if (!parsed.successful) {
      val notParsed = parsed.asInstanceOf[Parsers#NoSuccess]
      val lineBreakAfter = expression.indexOf("\n", notParsed.next.offset)
      val startAndFailed = expression.substring(0, lineBreakAfter)
      fail(notParsed.msg + "\n" +
        startAndFailed + "\n" +
        (" " * (notParsed.next.offset - (startAndFailed.lastIndexOf("\n") + 1))) + "^" +
        expression.substring(lineBreakAfter))
    }
  }

  @inline private def willNotParse(expression: String, parser: ParseShared) =
    assertFalse("No failure on " + expression, parser.parseAll(parser.exprs, expression).successful)


  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testCEnum1ElementNoAssign = parsesCorrectly(oneElementNoAssign, parseTest)
  @Test def testCEnum1ElementAssign  = parsesCorrectly(oneElementAssign, parseTest)
  @Test def testCEnum3ElementsNoAssign = parsesCorrectly(threeElementsNoAssign, parseTest)
  @Test def testCEnum3ElementsAssignAll = parsesCorrectly(threeElementsAssignAll, parseTest)
  @Test def testCEnum3ElementsFirstAndLastTheSame = parsesCorrectly( threeElementsFirstAndLastTheSame, parseTest)
  @Test def testCEnum1CommentCxxBefore = parsesCorrectly(commentCxxOneLine + oneElementNoAssign, parseTest)
  @Test def testCEnum1CommentCxxAfter = parsesCorrectly(oneElementNoAssign + commentCxxOneLine, parseTest)
  @Test def testCEnum1CommentCBefore = parsesCorrectly(commentCOneLine + oneElementNoAssign, parseTest)
  @Test def testCEnum1CommentCAfter = parsesCorrectly(oneElementNoAssign + commentCOneLine, parseTest)
  @Test def testCEnumCommentInside = parsesCorrectly(threeElementsCommentInside, parseTest)
  @Test def testCEnumCommentInsideBefore = parsesCorrectly(threeElementsCommentInsideBefore, parseTest)
  @Test def testCEnumCommentInsideAfter = parsesCorrectly(threeElementsCommentInsideAfter, parseTest)

  @Test def testCEnumNotLookedFor = willNotParse(threeElementsNoAssign.replace("test", "notTest"), parseTest)


  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testSpecEnumTwoElements =
    parsesCorrectly(specEnumTwoElements, parseTest)
  @Test def testSpecEnumTwoNamedElements =
    parsesCorrectly(specEnumTwoNamedElements, parseTest)
  @Test def testSpecEnumTwoElementsBitwise =
    parsesCorrectly(specEnumTwoElementsBitwise, parseTest)
  @Test def testSpecEnumTwoElementsBitwiseZero =
    parsesCorrectly(specEnumTwoElementsBitwiseZero, parseTest)
  @Test def testSpecEnumTwoElementsBitwiseNamedZero =
    parsesCorrectly(specEnumTwoElementsBitwiseNamedZero, parseTest)
  @Test def testSpecEnumTwoElementsCount =
    parsesCorrectly(specEnumTwoElementsCount, parseTest)
  @Test def testSpecEnumTwoElementsNamedCount =
    parsesCorrectly(specEnumTwoElementsNamedCount, parseTest)
  @Test def testSpecEnumTwoElementsInvalid =
    parsesCorrectly(specEnumTwoElementsInvalid, parseTest)
  @Test def testSpecEnumCommentInDef =
    parsesCorrectly(specEnumTwoNamedElementsWithComments, parseTest)
  @Test def testSpecEnumCommentBeforeAndAfterDef =
    parsesCorrectly(specEnumTwoNamedElementsWithCommentBeforeAndAfter, parseTest)
}
