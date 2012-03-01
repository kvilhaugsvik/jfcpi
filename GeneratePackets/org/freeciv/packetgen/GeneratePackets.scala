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
import org.freeciv.Connect

class GeneratePackets(packetsDefPath: File, cPaths: List[File], devMode: Boolean, hasTwoBytePacketNumber: Boolean) {

  def this(packetsDefPathString: String, cPathsString: List[String], devMode: Boolean, hasTwoBytePacketNumber: Boolean) = {
    this(new File(packetsDefPathString), cPathsString.map(new File(_)), devMode, hasTwoBytePacketNumber)
  }

  private val storage = new PacketsStore(devMode, hasTwoBytePacketNumber)
  private val Parser = new ParsePacketsDef(storage)

  GeneratePackets.checkFilesCanRead(packetsDefPath :: cPaths)

  private val toLookFor = (new Hardcoded).values().filter(!_.hasRequired).map(_.getPublicType).map(requirement => {
    val req = requirement.split("\\s")
    if ("enum".equals(req(0))) {
      req(1) -> requirement
    } else {
      throw new UnsupportedOperationException("No support for generating " + req(0) + " one the fly")
    }
  }: (String, String)).toMap
  if ((null != toLookFor) && !Nil.equals(toLookFor)) {
    val extractor = new FromCExtractor(toLookFor.keys.toList)
    cPaths.map(code => {
      val codeFile = new FileReader(code)
      val content = StreamReader(codeFile).source.toString
      codeFile.close()
      extractor.extract(content).foreach((requirement) =>
        storage.addRequirement(toLookFor(requirement.getName), requirement))
    })
  }

  if (!Parser.parsePacketsDef(StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath)))).successful) {
    throw new IOException("Can't parse " + packetsDefPath.getAbsolutePath)
  }

  def writeToDir(path: String): Unit = writeToDir(new File(path))

  def writeToDir(path: File) {
    storage.getJavaCode.foreach((code) => {
      val packagePath = code.getPackage.replaceAll("""\.""", "/")
      val classFile = new File(path + "/" + packagePath + "/" + code.getName + ".java")
      classFile.getParentFile.mkdirs()
      classFile.createNewFile
      val classWriter = new FileWriter(classFile)
      classWriter.write(code.toString)
      classWriter.close()
    })

    val manifest = new File(path + Connect.packetsList)
    manifest.createNewFile
    val manifestWriter = new FileWriter(manifest)
    manifestWriter.write(storage.getPacketList)
    manifestWriter.close()
  }
}

object GeneratePackets {
  def main(args: Array[String]) {
    val self = new GeneratePackets(args(0), args.tail.toList, GeneratorDefaults.DEVMODE, true)
    self.writeToDir(GeneratorDefaults.GENERATEDOUT)
  }

  def checkFilesCanRead(files: List[File]) {
    files.foreach((fileToValidate: File) => {
      if (!fileToValidate.exists()) {
        throw new IOException(fileToValidate.getAbsolutePath + " doesn't exist.")
      } else if (!fileToValidate.canRead) {
        throw new IOException("Can't read " + fileToValidate.getAbsolutePath)
      }
    })
  }
}
