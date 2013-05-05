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
import org.freeciv.utility.{UI, ChangingConsoleLine, ArgumentSettings, Setting}
import com.kvilhaugsvik.dependency.UndefinedException

class GeneratePackets(sourceLocation: String, packetsDefRPath: String, versionRPath: String, cRPaths: List[String],
                      requested: List[(String, String)], logger: String,
                      devMode: Boolean, packetHeader: PacketHeaderKinds, enableDelta: Boolean, enableDeltaBoolFolding: Boolean) {
  private val packetsDefPath: File = new File(sourceLocation + packetsDefRPath)
  private val versionPath: File = new File(sourceLocation + versionRPath)
  private val cPaths: List[File] = cRPaths.map(cr => new File(sourceLocation + cr))

  private val storage = new PacketsStore(packetHeader, logger, enableDelta, enableDeltaBoolFolding)
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

    val notFound = storage.explainMissing()
    if (!notFound.isEmpty) {
      if (devMode) {
        println("Some packets were not generated. These were missing or had missing dependencies:")
        notFound.foreach(println(_))
      } else {
        throw new UndefinedException("Missing dependencies:\n" + notFound.map(_.toString).reduce(_ + "\n" + _))
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
      new Setting.StringSetting(SOURCE_CODE_LOCATION, GeneratorDefaults.FREECIV_SOURCE_PATH,
        "the location of the Freeciv source code to generate from"),
      new Setting.StringSetting(VERSION_INFORMATION, GeneratorDefaults.VERSIONCONFIGURATION,
        "file containing settings for the version of Freeciv"),
      new Setting.StringSetting(PACKETS_SHOULD_LOG_TO, GeneratorDefaults.LOG_TO,
        "the logger the generated code should use"),
      new Setting.BoolSetting(IGNORE_PROBLEMS, GeneratorDefaults.DEVMODE.toBoolean,
        "should problems be ignored?"),
      UI.HELP_SETTING
    ), args: _*)

    UI.printAndExitOnHelp(settings, classOf[GeneratePackets])

    val versionConfiguration = readVersionParameters(new File(settings.getSetting[String](VERSION_INFORMATION)))

    val packetHeader = PacketHeaderKinds.valueOf(versionConfiguration.attribute("packetHeaderKind").get.text)

    val enableDelta = versionConfiguration.attribute("enableDelta").get.text.toBoolean
    val enableDeltaBoolFolding = versionConfiguration.attribute("enableDeltaBoolFolding").get.text.toBoolean

    val inputSources = (versionConfiguration \ "inputSource").map(elem =>
      elem.attribute("parseAs").get.text -> (elem \ "file").map(_.text)).toMap

    val requested: List[(String, String)] =
      ((versionConfiguration \ "requested") \ "_").map(item => item.label -> item.text).toList

    val self = new GeneratePackets(
      settings.getSetting[String](SOURCE_CODE_LOCATION) + "/",
      inputSources("packets").head,
      inputSources("variables").head,
      inputSources("C").toList,
      requested,
      settings.getSetting[String](PACKETS_SHOULD_LOG_TO),
      settings.getSetting[Boolean](IGNORE_PROBLEMS),
      packetHeader,
      enableDelta,
      enableDeltaBoolFolding)

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
