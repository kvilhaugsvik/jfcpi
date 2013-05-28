/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

class UsingGenerator {
  @Test
  def simple() {
    UsingGenerator.simple(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER)
  }
}

object UsingGenerator {
  def simple(generated_test_source_folder: String) {
    var full = new GeneratePackets("Tests/ThatGenerateSourceCode/", "simple/simple.def", "simple/simple.var", Nil,
      List[(String, String)](),
      GeneratorDefaults.LOG_TO,
      false, PacketHeaderKinds.FC_trunk,
      true, true)
    full.writeToDir(generated_test_source_folder, true)
  }

  def main(args: Array[String]) {
    simple(if (0 == args.length) GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER else args(0))
  }
}
