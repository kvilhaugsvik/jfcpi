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

import parsing._
import util.parsing.input.StreamReader
import java.io._
import collection.JavaConversions._
import xml.XML
import org.freeciv.utility.{ChangingConsoleLine, ArgumentSettings, Setting}

class GeneratePackets(packetsDefPath: File, versionPath: File, cPaths: List[File],
                      requested: List[(String, String)], logger: String,
                      devMode: Boolean, bytesInPacketNumber: Int) {

  def this(packetsDefPathString: String, versionPath: String, cPathsString: List[String],
           requested: List[(String, String)], logger: String,
           devMode: Boolean, bytesInPacketNumber: Int) = {
    this(new File(packetsDefPathString), new File(versionPath), cPathsString.map(new File(_)),
      requested, logger,
      devMode, bytesInPacketNumber)
  }

  private val storage = new PacketsStore(bytesInPacketNumber, logger)
  private val Parser = new ParsePacketsDef(storage)

  requested.filter(item => "constant".equals(item._1)).foreach(cons => storage.requestConstant(cons._2))
  requested.filter(item => "type".equals(item._1)).foreach(cons => storage.requestType(cons._2))

  GeneratePackets.checkFilesCanRead(packetsDefPath :: versionPath :: cPaths)

  println("Reading Freeciv version information")
  VariableAssignmentsExtractor.extract(GeneratePackets.readFileAsString(versionPath)).foreach(storage.addDependency(_))

  print("Extracting from provided C code")
  cPaths.map(code => {print("."); FromCExtractor.extract(GeneratePackets.readFileAsString(code))}).flatten
    .foreach(storage.addDependency(_))
  println()

  println("Extracting from protocol definition")
  private val packetsDefResult =
    Parser.parsePacketsDef(StreamReader(new InputStreamReader(new FileInputStream(packetsDefPath))))
  if (!packetsDefResult.successful) {
    throw new IOException("Can't parse " + packetsDefPath.getAbsolutePath + "\n" + packetsDefResult.toString)
  }

  println("Applying manual changes")
  Hardcoded.applyManualChanges(storage);

  def writeToDir(path: String): Unit = writeToDir(new File(path))

  def writeToDir(path: File) {
    val statusPrinter = new ChangingConsoleLine("Writing the file ", System.out)

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

      statusPrinter.printCurrent(classFile.getAbsolutePath)

      classFile.getParentFile.mkdirs()
      classFile.createNewFile
      val classWriter = new FileWriter(classFile)
      classWriter.write(code.toString)
      classWriter.close()
    })

    statusPrinter.finished()
  }
}

object GeneratePackets {
  private val SOURCE_CODE_LOCATION = "source-code-location"
  private val VERSION_INFORMATION = "version-information"
  private val PACKETS_SHOULD_LOG_TO = "packets-should-log-to"
  private val IGNORE_PROBLEMS = "ignore-problems"

  def main(args: Array[String]) {
    val settings = new ArgumentSettings(List(
      new Setting.StringSetting(SOURCE_CODE_LOCATION, GeneratorDefaults.FREECIV_SOURCE_PATH),
      new Setting.StringSetting(VERSION_INFORMATION, GeneratorDefaults.VERSIONCONFIGURATION),
      new Setting.StringSetting(PACKETS_SHOULD_LOG_TO, GeneratorDefaults.LOG_TO),
      new Setting.BoolSetting(IGNORE_PROBLEMS, GeneratorDefaults.DEVMODE.toBoolean)
    ), args: _*)

    val versionConfiguration = readVersionParameters(new File(settings.getSetting[String](VERSION_INFORMATION)))

    val bytesInPacketNumber = versionConfiguration.attribute("packetNumberSize").get.text.toInt

    val inputSources = (versionConfiguration \ "inputSource").map(elem =>
      elem.attribute("parseAs").get.text -> (elem \ "file").map(settings.getSetting[String](SOURCE_CODE_LOCATION) + "/" + _.text)).toMap

    val requested: List[(String, String)] =
      ((versionConfiguration \ "requested") \ "_").map(item => item.label -> item.text).toList

    val self = new GeneratePackets(inputSources("packets").head,
      inputSources("variables").head,
      inputSources("C").toList,
      requested,
      settings.getSetting[String](PACKETS_SHOULD_LOG_TO),
      settings.getSetting[Boolean](IGNORE_PROBLEMS),
      bytesInPacketNumber)

    self.writeToDir(GeneratorDefaults.GENERATED_SOURCE_FOLDER)
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

  def readVersionParameters(listFile: File) = {
    checkFilesCanRead(listFile :: Nil)
    XML.loadFile(listFile)
  }
}
