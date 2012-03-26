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

  private val storage = new PacketsStore(hasTwoBytePacketNumber)
  private val Parser = new ParsePacketsDef(storage)

  GeneratePackets.checkFilesCanRead(packetsDefPath :: cPaths)
  print("Extracting from provided C code")
  val extractor = new FromCExtractor()
  cPaths.map(code => {print("."); extractor.extract(GeneratePackets.readFileAsString(code))}).flatten
    .foreach(storage.addDependency(_))
  println()

  println("Extracting from protocol definition")
  private val packetsDefResult =
    Parser.parsePacketsDef(StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath))))
  if (!packetsDefResult.successful) {
    throw new IOException("Can't parse " + packetsDefPath.getAbsolutePath + "\n" + packetsDefResult.toString)
  }

  def writeToDir(path: String): Unit = writeToDir(new File(path))

  def writeToDir(path: File) {
    val notFound = storage.getUnsolvedRequirements
    if (!notFound.isEmpty) {
      if (devMode) {
        println("Some packets were not generated. These were missing or had missing dependencies:")
        notFound.foreach(println(_))
      } else {
        throw new UndefinedException("Missing dependencies: " + notFound)
      }
    }
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

  def readFileAsString(code: File): String = {
    val codeFile = new FileReader(code)
    val content = StreamReader(codeFile).source.toString
    codeFile.close()
    return content
  }
}
