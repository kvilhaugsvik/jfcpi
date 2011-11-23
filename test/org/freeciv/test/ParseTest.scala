package org.freeciv.test

import org.junit.Test
import junit.framework.Assert._
import org.freeciv.packetgen.{PacketsStore, ParsePacketsDef}
import util.parsing.input.CharArrayReader

class ParseTest {
  @Test def parsesTypeDefSrc() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    parser.parsePacketsDef("type BOOL               = bool8(bool)")

    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesTypeDefAlias() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    parser.parsePacketsDef("type BOOL               = bool8(bool)")
    parser.parsePacketsDef("type BOOL2              = BOOL")

    assertTrue(storage.hasTypeAlias("BOOL2"))
  }

  @Test def parsesTypeDefTwoLines() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    parser.parsePacketsDef("""type BOOL = bool8(bool)
                              type BOOL2 = BOOL""")

    assertTrue(storage.hasTypeAlias("BOOL2"))
  }

  @Test def parsesTypeDefAsStream() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    val stream = new CharArrayReader("type BOOL = bool8(bool)\ntype BOOL2 = BOOL".toCharArray)
    parser.parsePacketsDef(stream)

    assertTrue(storage.hasTypeAlias("BOOL2"))
  }

  @Test def parsesCommentCStyleBefore() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("/*comment*/ type BOOL               = bool8(bool)").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleAfter() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool) /*comment*/").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleBeforeAndAfter() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("/*comment*/ type BOOL               = bool8(bool) /*comment*/").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleOnTwoLines() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""/* comment
    on two lines */
    type BOOL               = bool8(bool)""").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleCommentOut() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("/*type BOOL               = bool8(bool)*/").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleBefore() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""// comment
      type BOOL               = bool8(bool)
      """).successful)

    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleAfter() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""type BOOL               = bool8(bool)
      // comment
      """).successful)

    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleAfterOnSameLine() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool) // comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleAfterOnSameLineNoSpace() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool)// comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleCommentsOut() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("//type BOOL               = bool8(bool)\n").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleOnLastLine() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("// comment").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleBefore() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""# comment
      type BOOL               = bool8(bool)
      """).successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAfter() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""type BOOL               = bool8(bool)
      # comment
      """).successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAfterOnSameLine() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool) # comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAfterOnSameLineNoSpace() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool)# comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleCommentsOut() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("#type BOOL               = bool8(bool)\n").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleOnLastLine() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("# comment").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }
}
