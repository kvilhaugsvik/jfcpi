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


  /*--------------------------------------------------------------------------------------------------------------------
  Common helper methods
  --------------------------------------------------------------------------------------------------------------------*/
  @inline private def storageAndParser = {
    val storage = new PacketsStore(false, true)
    val parser = new ParseCCode(storage, List("test"))
    (storage, parser)
  }

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
  @Test def testCEnum1ElementNoAssign = parsesCorrectly(oneElementNoAssign, storageAndParser._2)
  @Test def testCEnum1ElementAssign  = parsesCorrectly(oneElementAssign, storageAndParser._2)
  @Test def testCEnum3ElementsNoAssign = parsesCorrectly(threeElementsNoAssign, storageAndParser._2)
  @Test def testCEnum3ElementsAssignAll = parsesCorrectly(threeElementsAssignAll, storageAndParser._2)
  @Test def testCEnum3ElementsFirstAndLastTheSame = parsesCorrectly( threeElementsFirstAndLastTheSame, storageAndParser._2)
  @Test def testCEnum1CommentCxxBefore = parsesCorrectly(commentCxxOneLine + oneElementNoAssign, storageAndParser._2)
  @Test def testCEnum1CommentCxxAfter = parsesCorrectly(oneElementNoAssign + commentCxxOneLine, storageAndParser._2)
  @Test def testCEnum1CommentCBefore = parsesCorrectly(commentCOneLine + oneElementNoAssign, storageAndParser._2)
  @Test def testCEnum1CommentCAfter = parsesCorrectly(oneElementNoAssign + commentCOneLine, storageAndParser._2)

  @Test def testCEnumNotLookedFor = willNotParse(threeElementsNoAssign.replace("test", "notTest"), storageAndParser._2)


  /*--------------------------------------------------------------------------------------------------------------------
  Test pure parsing of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testSpecEnumTwoElements =
    parsesCorrectly(specEnumTwoElements, storageAndParser._2)
  @Test def testSpecEnumTwoNamedElements =
    parsesCorrectly(specEnumTwoNamedElements, storageAndParser._2)
  @Test def testSpecEnumTwoElementsBitwise =
    parsesCorrectly(specEnumTwoElementsBitwise, storageAndParser._2)
  @Test def testSpecEnumTwoElementsBitwiseZero =
    parsesCorrectly(specEnumTwoElementsBitwiseZero, storageAndParser._2)
  @Test def testSpecEnumTwoElementsBitwiseNamedZero =
    parsesCorrectly(specEnumTwoElementsBitwiseNamedZero, storageAndParser._2)
  @Test def testSpecEnumTwoElementsCount =
    parsesCorrectly(specEnumTwoElementsCount, storageAndParser._2)
  @Test def testSpecEnumTwoElementsNamedCount =
    parsesCorrectly(specEnumTwoElementsNamedCount, storageAndParser._2)
  @Test def testSpecEnumTwoElementsInvalid =
    parsesCorrectly(specEnumTwoElementsInvalid, storageAndParser._2)
}
