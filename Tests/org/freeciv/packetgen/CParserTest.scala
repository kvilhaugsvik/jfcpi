package org.freeciv.packetgen

import org.junit.Test
import org.junit.Assert._
import scala.inline
import util.parsing.combinator.Parsers

class CParserTest {
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


  @Test def test1ElementNoAssign = parsesCorrectly(oneElementNoAssign, storageAndParser._2)
  @Test def test1ElementAssign  = parsesCorrectly(oneElementAssign, storageAndParser._2)
  @Test def test3ElementsNoAssign = parsesCorrectly(threeElementsNoAssign, storageAndParser._2)
  @Test def test3ElementsAssignAll = parsesCorrectly(threeElementsAssignAll, storageAndParser._2)
  @Test def test3ElementsFirstAndLastTheSame = parsesCorrectly( threeElementsFirstAndLastTheSame, storageAndParser._2)
  @Test def test1CommentCxxBefore = parsesCorrectly(commentCxxOneLine + oneElementNoAssign, storageAndParser._2)
  @Test def test1CommentCxxAfter = parsesCorrectly(oneElementNoAssign + commentCxxOneLine, storageAndParser._2)
  @Test def test1CommentCBefore = parsesCorrectly(commentCOneLine + oneElementNoAssign, storageAndParser._2)
  @Test def test1CommentCAfter = parsesCorrectly(oneElementNoAssign + commentCOneLine, storageAndParser._2)

  @Test def testEnumNotLookedFor = willNotParse(threeElementsNoAssign.replace("test", "notTest"), storageAndParser._2)
}
