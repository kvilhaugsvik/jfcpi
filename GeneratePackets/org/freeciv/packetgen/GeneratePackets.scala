/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

import util.parsing.input.StreamReader
import java.io._
import collection.JavaConversions._

class GeneratePackets(packetsDefPath: File, devMode: Boolean, hasTwoBytePacketNumber: Boolean) {

  def this(packetsDefPath: String, devMode: Boolean, hasTwoBytePacketNumber: Boolean) =
    this(new File(packetsDefPath), devMode, hasTwoBytePacketNumber)

  private val storage = new PacketsStore(devMode, hasTwoBytePacketNumber)
  private val Parser = new ParsePacketsDef(storage)

  if (!packetsDefPath.exists()) {
    throw new IOException(packetsDefPath.getAbsolutePath + " doesn't exist.")
  } else if (!packetsDefPath.canRead) {
    throw new IOException("Can't read " + packetsDefPath.getAbsolutePath)
  }

  if (!Parser.parsePacketsDef(StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath)))).successful) {
    throw new IOException("Can't parse " + packetsDefPath.getAbsolutePath)
  }

  def writeToDir(path: String): Unit = writeToDir(new File(path))

  def writeToDir(path: File) {
    (new File(path + "/org/freeciv/packet/fieldtype")).mkdirs()
    storage.getJavaCode.foreach((code) => {
      val packagePath = code.getPackage.replaceAll("""\.""", "/")
      val classFile = new File(path + "/" + packagePath + "/" + code.getName + ".java")
      classFile.createNewFile
      val classWriter = new FileWriter(classFile)
      classWriter.write(code.toString)
      classWriter.close()
    })

    val manifest = new File(path + "/org/freeciv/packet/" + "packets.txt")
    manifest.createNewFile
    val manifestWriter = new FileWriter(manifest)
    manifestWriter.write(storage.getPacketList)
    manifestWriter.close()
  }
}

object GeneratePackets {
  def main(args: Array[String]) {
    val self = new GeneratePackets(args(0), GeneratorDefaults.DEVMODE, true)
    self.writeToDir(GeneratorDefaults.GENERATEDOUT)
  }
}
