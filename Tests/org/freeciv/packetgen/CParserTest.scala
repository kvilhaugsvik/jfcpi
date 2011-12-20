package org.freeciv.packetgen

import org.junit.Test
import org.junit.Assert._
import scala.inline
import util.parsing.combinator.Parsers

class CParserTest {
  /*--------------------------------------------------------------------------------------------------------------------
  Constants for pure parsing tests of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  private def cEnum1ElementNoAssign = """enum test {
    one
  }"""

  private def cEnum1ElementAssign = """enum test {
    one = 1
  }"""

  private def cEnum3ElementsNoAssign = """enum test {
    one,
    two,
    three
  }"""
  private def cEnum3ElementsAssignAll = """enum test {
    one = 1,
    two = 2,
    three = 3
  }"""

  private def cEnum3ElementsFirstNumbered = """enum test {
    two = 2,
    three,
    four
  }"""

  private def cEnum3ElementsFirstAndLastTheSame = """enum test {
    zero,
    one,
    null = 0
  }"""

  private def cEnum3ElementsCommentInside = """enum test {
    one,
    /* comment */
    two,
    three
  }"""

  private def cEnum3ElementsCommentInsideBefore = """enum test {
    /* comment */
    one,
    two,
    three
  }"""

  private def cEnum3ElementsCommentInsideAfter = """enum test {
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
  private def specEnum2Elements = """
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

  private def specEnum2ElementsBitwise = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnum2ElementsBitwiseZero = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_ZERO ZERO
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnum2ElementsBitwiseNamedZero = """
  #define SPECENUM_NAME test
  #define SPECENUM_BITWISE
  #define SPECENUM_ZERO ZERO
  #define SPECENUM_ZERONAME "nothing"
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnum2ElementsCount = """
  #define SPECENUM_NAME test
  #define SPECENUM_COUNT NUMBER_OF
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnum2ElementsNamedCount = """
  #define SPECENUM_NAME test
  #define SPECENUM_COUNT NUMBER_OF
  #define SPECENUM_COUNTNAME "last"
  #define SPECENUM_VALUE0 ONE
  #define SPECENUM_VALUE1 TWO
  #include "specenum_gen.h"
  """

  private def specEnum2ElementsInvalid = """
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
  Test pure parsing of enums declared with the enum name {element, element} syntax
  --------------------------------------------------------------------------------------------------------------------*/
  @Test def testSpecEnum2Elements =
    parsesCorrectly(specEnum2Elements, parseTest)
  @Test def testSpecEnumTwoNamedElements =
    parsesCorrectly(specEnumTwoNamedElements, parseTest)
  @Test def testSpecEnum2ElementsBitwise =
    parsesCorrectly(specEnum2ElementsBitwise, parseTest)
  @Test def testSpecEnum2ElementsBitwiseZero =
    parsesCorrectly(specEnum2ElementsBitwiseZero, parseTest)
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
