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

  @Test def parsesCommentPythonStyleAmongFieldsInPacket() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                           # comment in the line
                                           BOOL inVoice; # Voice or text?
                                         end""").successful)
  }

  @Test def parsesTwoCommentsInARowAmongFieldsInPacket() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                           # comment in the line
                                           # This is on another line and will be seen as seperate
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsesCommentPythonStyleBeforeFieldsInPacket() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           # comment in the line
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsePackageWithoutFields() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""PACKET_CONN_PING = 88;
                                      end""").successful)
    assertTrue(storage.hasPacket(88))
  }

  @Test def parsePackageNoName() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertFalse(parser.parsePacketsDef("""PACKET_ = 5;
                                      end""").successful)
  }

  @Test def parsePackageNoNumber() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertFalse(parser.parsePacketsDef("""PACKET_CONN_PING = ;
                                      end""").successful)
  }

  @Test def parsePackageWithFieldCanParse() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HI = 57;
                                           BOOL friendly;
                                         end""").successful)
  }

  @Test def parsePackageWithFieldsCanParse() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly;
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsePackageWithFlagCanParse() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""PACKET_CONN_PING = 88; sc
                                      end""").successful)
  }

  @Test def parsePackageWithFlagsCanParse() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    assertTrue(parser.parsePacketsDef("""PACKET_CONN_PONG = 89; cs, handle-per-conn
                                         end""").successful)
  }

  @Test def parsePackageWithFlagThatTakesAnArgumentCanParse() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")
    parser.parsePacketsDef("""PACKET_HI = 57; cs
                                           BOOL friendly;
                                         end""")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5; cs, cancel(PACKET_HI), handle-per-conn
                                           BOOL friendly;
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsePackageWithFlagOnField() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                         end""").successful)
  }

  @Test def parsePackageWithFlagsOnField() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key, diff
                                         end""").successful)
  }

  @Test def parsePackageWithFlagOnFieldAndFieldAfter() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsePackageWithArgumentUsingFlagOnField() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; add-cap(attitude)
                                         end""").successful)
  }

  @Test def parseFieldWithOneArrayDeclaration() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[1];
                                         end""").successful)
  }

  @Test def parseFieldWithTwoArrayDeclarations() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[1][5];
                                         end""").successful)
  }

  @Test def parseFieldWithArrayDeclarationWithConstant() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[PLAYERS];
                                         end""").successful)
  }

  @Test def parseFieldWithArrayDeclarationWithElementsToTransfer() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[20:3];
                                         end""").successful)
  }

  @Test def parseFieldWithArrayDeclarationWithOperatorInConstant() {
    val storage = new PacketsStore(false)
    val parser = new ParsePacketsDef(storage)

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[PLAYERS+1:3];
                                         end""").successful)
  }
}
