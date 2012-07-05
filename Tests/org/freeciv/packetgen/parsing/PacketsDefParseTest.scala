package org.freeciv.packetgen.parsing

/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * of those (if copyrightable) belong to their respective copyright
 * holders.
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

import org.junit.Test
import org.junit.Assert._
import collection.JavaConversions._
import util.parsing.input.CharArrayReader
import org.freeciv.packetgen.PacketsStore
import org.freeciv.packetgen.enteties.supporting.{WeakField, Field}

class PacketsDefParseTest {
  @inline def storePars = {
    val storage = new PacketsStore(2)
    val parser = new ParsePacketsDef(storage)
    (storage, parser)
  }

  def testBooleanFieldsFriendlyAndInvoiceInPacket(fieldDec: String) {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8", "bool")

    assertTrue("Couldn't parse", parser.parsePacketsDef("PACKET_HELLO = 5;\n" + fieldDec + "\nend").successful)
    assertTrue("Didn't store packet", storage.hasPacket(5))

    val storedFields = storage.getPacket("PACKET_HELLO").getFields.map(_.getFieldName)
    assertTrue("Didn't add field to packet", storedFields.contains("inVoice"))
    assertTrue("Didn't add field to packet", storedFields.contains("friendly"))
  }

  @Test def parsesTypeDefSrc() {
    val (storage, parser) = storePars

    parser.parsePacketsDef("type BOOL               = bool8(bool)")

    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesTypeDefAlias() {
    val (storage, parser) = storePars

    parser.parsePacketsDef("type BOOL               = bool8(bool)")
    parser.parsePacketsDef("type BOOL2              = BOOL")

    assertTrue(storage.hasTypeAlias("BOOL2"))
  }

  @Test def parsesTypeDefTwoLines() {
    val (storage, parser) = storePars

    parser.parsePacketsDef("""type BOOL = bool8(bool)
                              type BOOL2 = BOOL""")

    assertTrue(storage.hasTypeAlias("BOOL2"))
  }

  @Test def parsesTypeDefAsStream() {
    val (storage, parser) = storePars

    val stream = new CharArrayReader("type BOOL = bool8(bool)\ntype BOOL2 = BOOL".toCharArray)
    parser.parsePacketsDef(stream)

    assertTrue(storage.hasTypeAlias("BOOL2"))
  }

  @Test def parsesCommentCStyleBefore() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("/*comment*/ type BOOL               = bool8(bool)").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleAfter() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool) /*comment*/").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleBeforeAndAfter() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("/*comment*/ type BOOL               = bool8(bool) /*comment*/").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleOnTwoLines() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""/* comment
    on two lines */
    type BOOL               = bool8(bool)""").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnBothSidesOfTextNoSpace() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/***Text***/

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnBothSidesOfTextSpace() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/*** Text ***/

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnLeftSideOfTextNoSpace() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/***Text*/

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnLeftSideOfTextSpace() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/*** Text */

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnRightSideOfTextNoSpace() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/*Text***/

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnRightSideOfTextSpace() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/* Text ***/

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsOnRightSideOfTextSpaceEvenNumber() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""/* Text ****/

    type BOOL               = bool8(bool)
    /* Will this take away bool? */
    """).successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleExtraStarsAmongText() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""
     /* Text
      * More text
      */

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleTwoExtraStarsInARowAmongText() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""
     /* Text
      ** More text
      */

    type BOOL               = bool8(bool)""").successful)
    assertTrue("Didn't store type", storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleBeginEnd() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("/**/").successful)
  }

  @Test def parsesCommentCStyleBeginStarsEnd() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("/*****/").successful)
  }

  @Test def parsesCommentCStyleCommentOut() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("/*type BOOL               = bool8(bool)*/").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCStyleAmongFieldsInPacket() {
    testBooleanFieldsFriendlyAndInvoiceInPacket("""
                                                   BOOL friendly;
                                                   /* comment for commenting */
                                                   BOOL inVoice;
                                                   """)
  }

  @Test def parsesCommentCStyleBeforeFieldsInPacket() {
    testBooleanFieldsFriendlyAndInvoiceInPacket("""
                                                   /* comment for commenting */
                                                   BOOL friendly;
                                                   BOOL inVoice;
                                                   """)
  }

  @Test def parsesCommentCStyleAfterFieldsInPacket() {
    testBooleanFieldsFriendlyAndInvoiceInPacket("""
                                                   BOOL friendly;
                                                   BOOL inVoice;
                                                   /* comment for commenting */
                                                   """)
  }

  @Test def parsesCommentCStyleBeforeAndAfterFieldsInPacket() {
    testBooleanFieldsFriendlyAndInvoiceInPacket("""
                                                   /* comment for commenting */
                                                   BOOL friendly;
                                                   BOOL inVoice;
                                                   /* comment for commenting */
                                                   """)
  }

  @Test def parsesCommentCStyleOnSameLineAsAFieldInPacket() {
    testBooleanFieldsFriendlyAndInvoiceInPacket("""
                                                   BOOL friendly; /* comment for commenting */
                                                   BOOL inVoice;
                                                   """)
  }

  @Test def parsesCommentCStyleOnlyCommentInPacket() {
    val (storage, parser) = storePars

    assertTrue("Couldn't parse", parser.parsePacketsDef("""PACKET_HELLO = 5;
                                         /* comment for commenting */
                                         end""").successful)
    assertTrue("Didn't store packet", storage.hasPacket(5))
  }

  @Test def parsesCommentCxxStyleBefore() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""// comment
      type BOOL               = bool8(bool)
      """).successful)

    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleAfter() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""type BOOL               = bool8(bool)
      // comment
      """).successful)

    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleAfterOnSameLine() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool) // comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleAfterOnSameLineNoSpace() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool)// comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleCommentsOut() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("//type BOOL               = bool8(bool)\n").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentCxxStyleOnLastLine() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("// comment").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleBefore() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""# comment
      type BOOL               = bool8(bool)
      """).successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAfter() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""type BOOL               = bool8(bool)
      # comment
      """).successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAfterOnSameLine() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool) # comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAfterOnSameLineNoSpace() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("type BOOL               = bool8(bool)# comment\n").successful)
    assertTrue(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleCommentsOut() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("#type BOOL               = bool8(bool)\n").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleOnLastLine() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("# comment").successful)
    assertFalse(storage.hasTypeAlias("BOOL"))
  }

  @Test def parsesCommentPythonStyleAmongFieldsInPacket() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                           # comment in the line
                                           BOOL inVoice; # Voice or text?
                                         end""").successful)
  }

  @Test def parsesTwoCommentsInARowAmongFieldsInPacket() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                           # comment in the line
                                           # This is on another line and will be seen as seperate
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsesCommentPythonStyleBeforeFieldsInPacket() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           # comment in the line
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsePackageWithoutFields() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""PACKET_CONN_PING = 88;
                                      end""").successful)
    assertTrue(storage.hasPacket(88))
  }

  @Test def parsePackageNoName() {
    val (storage, parser) = storePars

    assertFalse(parser.parsePacketsDef("""PACKET_ = 5;
                                      end""").successful)
  }

  @Test def parsePackageNoNumber() {
    val (storage, parser) = storePars

    assertFalse(parser.parsePacketsDef("""PACKET_CONN_PING = ;
                                      end""").successful)
  }

  @Test def parsePackageWithFieldCanParse() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HI = 57;
                                           BOOL friendly;
                                         end""").successful)
  }

  @Test def parsePackageWithFieldsCanParse() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly;
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parseAFieldIsNotANumber() {
    val (storage, parser) = storePars

    assertFalse(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL 1;
                                         end""").successful)
  }

  @Test def parsePackageWithFlagCanParse() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""PACKET_CONN_PING = 88; sc
                                      end""").successful)
  }

  @Test def parsePackageWithFlagsCanParse() {
    val (storage, parser) = storePars

    assertTrue(parser.parsePacketsDef("""PACKET_CONN_PONG = 89; cs, handle-per-conn
                                         end""").successful)
  }

  @Test def parsePackageWithFlagThatTakesAnArgumentCanParse() {
    val (storage, parser) = storePars

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
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                         end""").successful)
  }

  @Test def parsePackageWithFlagsOnField() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key, diff
                                         end""").successful)
  }

  @Test def parsePackageWithFlagOnFieldAndFieldAfter() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; key
                                           BOOL inVoice;
                                         end""").successful)
  }

  @Test def parsePackageWithArgumentUsingFlagOnField() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly; add-cap(attitude)
                                         end""").successful)
  }

  @Test def parseFieldWithOneArrayDeclaration() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[1];
                                         end""").successful)
  }

  @Test def parseFieldWithTwoArrayDeclarations() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[1][5];
                                         end""").successful)
  }

  @Test def parseFieldWithArrayDeclarationWithConstant() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[PLAYERS];
                                         end""").successful)
  }

  @Test def parseFieldWithArrayDeclarationWithElementsToTransfer() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")
    storage.registerTypeAlias("UINT8", "uint8(int)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           UINT8 waysToBeFriendly;
                                           BOOL friendly[20:waysToBeFriendly];
                                         end""").successful)
  }

  @Test def parseElementsToTransferIsNotANumber() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertFalse(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly[20:3];
                                         end""").successful)
  }

  @Test def parseFieldWithArrayDeclarationWithOperatorInConstant() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")
    storage.registerTypeAlias("UINT8", "uint8(int)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           UINT8 waysToBeFriendly;
                                           BOOL friendly[PLAYERS+1:waysToBeFriendly];
                                         end""").successful)
  }

  @Test def parseTwoFieldsInOneDefine() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly, inVoice;
                                         end""").successful)
  }

  @Test def parseManyFieldsInOneDefine() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8(bool)")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL a, b, c;
                                         end""").successful)
  }

  @Test def storesTwoFieldsInOneDefine() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8", "bool")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL friendly, inVoice;
                                         end""").successful)
    assertTrue(storage.hasPacket(5))

    val storedFields = storage.getPacket("PACKET_HELLO").getFields.map(_.getFieldName)
    assertTrue(storedFields.contains("friendly"))
    assertTrue(storedFields.contains("inVoice"))
  }

  @Test def storesManyFieldsInOneDefine() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("BOOL", "bool8", "bool")

    assertTrue(parser.parsePacketsDef("""PACKET_HELLO = 5;
                                           BOOL a, b, c;
                                         end""").successful)
    assertTrue(storage.hasPacket(5))

    val storedFields = storage.getPacket("PACKET_HELLO").getFields.map(_.getFieldName)
    assertTrue(storedFields.contains("a"))
    assertTrue(storedFields.contains("b"))
    assertTrue(storedFields.contains("c"))
  }

  private def assertWeakFieldIs(message: String, expected: Array[String], actual: WeakField) {
    assertEquals(message, expected(0), actual.getType)
    assertEquals(message, expected(1), actual.getName)
    assertEquals(message, (expected.size - 2) / 2, actual.getDeclarations.size)

    val decs: Array[WeakField.ArrayDeclaration] = actual.getDeclarations
    var decNum = 0
    while (decNum < decs.size) {
      val declaration = decs(decNum)
      assertEquals(message, expected(2 + decNum * 2), declaration.maxSize.evaluate().toString)
      assertEquals(message, expected(1 + 2 + decNum * 2), declaration.elementsToTransfer)
      decNum += 1
    }
  }

  private val manyFieldsInOneDefineSomeWithArrayDeclarations = """
  UINT8 maxB;
  UINT8 a, b[7], c[8:maxB], d[7][8:maxB];"""

  @Test def formatsManyFieldsInOneDefineSomeWithArrayDeclarations() {
    val (storage, parser) = storePars

    val result = parser.parseAll(parser.fieldList, manyFieldsInOneDefineSomeWithArrayDeclarations)
    assertTrue(result.toString, result.successful)

    val results: List[WeakField] = result.get

    storage.registerTypeAlias("UINT8", "uint8", "int") // TODO: Kill with fire in a refactoring
    storage.registerPacket("JUST_FOR_SIDE_EFFECTS", 42, results) // TODO: Kill with fire in a refactoring

    assertWeakFieldIs("Field parsed in wrong format", Array("UINT8", "maxB"), results(0))
    assertWeakFieldIs("Field parsed in wrong format", Array("UINT8", "a"), results(1))
    assertWeakFieldIs("Field parsed in wrong format", Array("UINT8", "b", "7", null), results(2))
    assertWeakFieldIs("Field parsed in wrong format", Array("UINT8", "c", "8", "maxB"), results(3))
    assertWeakFieldIs("Field parsed in wrong format",
                      Array("UINT8", "d", "7", null, "8", "maxB"),
                      results(4))
  }

  @Test def parsesManyFieldsInOneDefineSomeWithArrayDeclarations() {
    val (storage, parser) = storePars

    storage.registerTypeAlias("UINT8", "uint8(int)")

    val result = parser.parsePacketsDef("PACKET_TEST = 42;\n" +
      manyFieldsInOneDefineSomeWithArrayDeclarations +
      "end")
    assertTrue(result.toString, result.successful)
  }
}
