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
}
