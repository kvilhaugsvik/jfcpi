package org.freeciv.packetgen

import org.junit.Test
import org.junit.Assert._
import scala.inline

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
    val parser = new ParseCCode(storage)
    (storage, parser)
  }

  @inline private def parsesCorrectly(expression: String, parser: ParseShared) =
    assertTrue("Unable to parse expression " + expression, parser.parseAll(parser.exprs, expression).successful)

  @Test def test1ElementNoAssign = parsesCorrectly(oneElementNoAssign, storageAndParser._2)
  @Test def test1ElementAssign  = parsesCorrectly(oneElementAssign, storageAndParser._2)
  @Test def test3ElementsNoAssign = parsesCorrectly(threeElementsNoAssign, storageAndParser._2)
  @Test def test3ElementsAssignAll = parsesCorrectly(threeElementsAssignAll, storageAndParser._2)
  @Test def test3ElementsFirstAndLastTheSame = parsesCorrectly( threeElementsFirstAndLastTheSame, storageAndParser._2)
  @Test def test1CommentCxxBefore = parsesCorrectly(commentCxxOneLine + oneElementNoAssign, storageAndParser._2)
  @Test def test1CommentCxxAfter = parsesCorrectly(oneElementNoAssign + commentCxxOneLine, storageAndParser._2)
  @Test def test1CommentCBefore = parsesCorrectly(commentCOneLine + oneElementNoAssign, storageAndParser._2)
  @Test def test1CommentCAfter = parsesCorrectly(oneElementNoAssign + commentCOneLine, storageAndParser._2)
}
