/*
 * Copyright (c) 2015. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen

import org.junit.Test

import org.junit.Assert._

/**
 * Tests of GeneratePackets it self.
 */
class TestGeneratePackets {
  /**
   * An already released version number isn't changed.
   */
  @Test def freecivVersionReleased(): Unit = {
    assertEquals("Wrong Freeciv version number for released version",
      "2.4", GeneratePackets.getFinalFreecivVersion("2", "4", "3"))
  }

  /**
   * The development version for a new minor version has its number
   * increased.
   */
  @Test def freecivVersionMinorIncrease(): Unit = {
    assertEquals("Wrong Freeciv version number for released version",
      "2.6", GeneratePackets.getFinalFreecivVersion("2", "5", "99"))
  }

  /**
   * The development version for a new major version has its number
   * increased.
   */
  @Test def freecivVersionMajorIncrease(): Unit = {
    assertEquals("Wrong Freeciv version number for released version",
      "3.0", GeneratePackets.getFinalFreecivVersion("2", "90", "99"))
  }
}
