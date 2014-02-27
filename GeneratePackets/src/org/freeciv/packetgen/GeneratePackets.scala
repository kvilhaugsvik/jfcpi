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
import com.kvilhaugsvik.dependency.{Dependency, UndefinedException}
import org.freeciv.packetgen.enteties.{Constant, SourceFile}
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue

class GeneratePackets(versionConfig: VersionConfig, sourceLocation: String,
                      requested: List[(String, String)], logger: String, devMode: Boolean) {
  def this(chosenVersion: String, sourceLocation: String,
           requested: List[(String, String)], logger: String, devMode: Boolean) {
    this(GeneratePackets.createVersionConfig(chosenVersion, sourceLocation),
      sourceLocation, requested, logger, devMode)
  }

  private val packetsDefRPath: String = versionConfig.inputSources("packets").head

  private val storage = new PacketsStore(versionConfig.configName, versionConfig.packetHeader, logger,
    versionConfig.enableDelta, versionConfig.enableDeltaBoolFolding)
  private val Parser = new ParsePacketsDef(storage)

  requested.filter(item => "constant".equals(item._1)).foreach(cons => storage.requestConstant(cons._2))
  requested.filter(item => "type".equals(item._1)).foreach(cons => storage.requestType(cons._2))

  private def createAndRegister(location: String, cr: String): SourceFile = {
    val src = GeneratePackets.readFileAsString(location, cr)
    storage.addSource(src)
    src
  }

  // checking that all input files are there BEFORE wasting time working on some of it is now done during reading
  println("Reading the source code")

  private val pdSource: SourceFile = GeneratePackets.readFileAsString(sourceLocation, packetsDefRPath)
  storage.addSource(pdSource)

  private val vpSource: SourceFile =
    GeneratePackets.readFileAsString(sourceLocation, versionConfig.inputSources("variables").head)
  storage.addSource(vpSource)

  private val cSources: List[SourceFile] =
    createAndRegister("GeneratePackets/", "data/constants.h") ::
      versionConfig.inputSources("C").toList.map(createAndRegister(sourceLocation, _))

  println("Adding Freeciv version information")
  VariableAssignmentsExtractor.extract(vpSource).foreach(storage.addDependency(_))

  print("Extracting from provided C code")
  cSources.map(cSource => {print("."); FromCExtractor.extract(cSource)}).flatten.foreach(storage.addDependency(_))
  println()

  println("Extracting from protocol definition")
  //TODO: Port packetDef understanding to extract system
  private val packetsDefResult =
    Parser.parsePacketsDef(StreamReader(new StringReader(pdSource.getContent)))
  if (!packetsDefResult.successful) {
    throw new IOException("Can't parse " + pdSource.getPath + "\n" + packetsDefResult.toString)
  }

  println("Applying manual changes")
  Hardcoded.applyManualChanges(storage);

  def writeToDir(path: String, includeOrigSrc: Boolean) {
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

    val genPath = path + "/" + "generated" + "/"
    storage.getJavaCode.foreach((code) => {
      val packagePath = code.getPackage.replaceAll("""\.""", "/")
      val classFile = new File(genPath + packagePath + "/" + code.getName + ".java")

      statusPrinter.printCurrent(classFile.getAbsolutePath)

      classFile.getParentFile.mkdirs()
      classFile.createNewFile
      val classWriter = new FileWriter(classFile)
      classWriter.write(code.toString)
      classWriter.close()
    })

    if (includeOrigSrc) {
      val srcLoc = path + "/" + "GPLComply" + "/"
      storage.getSource.foreach((src) => {
        val srcCopy = new File(srcLoc + src.getPath)

        statusPrinter.printCurrent(srcCopy.getAbsolutePath)

        srcCopy.getParentFile.mkdirs()
        srcCopy.createNewFile
        val writer = new FileWriter(srcCopy)
        writer.write(src.getContent)
        writer.close()
      })
    }

    statusPrinter.finished()
  }
}

object GeneratePackets {
  private val SOURCE_CODE_LOCATION = "source-code-location"
  private val VERSION_INFORMATION = "version-information"
  private val PACKETS_SHOULD_LOG_TO = "packets-should-log-to"
  private val IGNORE_PROBLEMS = "ignore-problems"
  private val GPL_SOURCE = "gpl-source"
  private val PRINT_FILES = "print-source-files"

  def main(args: Array[String]) {
    val settings = new ArgumentSettings(List(
      new Setting.StringSetting(SOURCE_CODE_LOCATION, GeneratorDefaults.FREECIV_SOURCE_PATH,
        "the location of the Freeciv source code to generate from"),
      new Setting.StringSetting(VERSION_INFORMATION, GeneratorDefaults.VERSIONCONFIGURATION,
        "file containing settings for the version of Freeciv"),
      new Setting.StringSetting(PACKETS_SHOULD_LOG_TO, GeneratorDefaults.LOG_TO,
        "the logger the generated code should use"),
      new Setting.BoolSetting(IGNORE_PROBLEMS, GeneratorDefaults.IGNORE_ISSUES,
        "should problems be ignored?"),
      new Setting.BoolSetting(GPL_SOURCE, GeneratorDefaults.NOT_DISTRIBUTED_WITH_FREECIV,
        "copy the Freeciv source code used as a source to generate Java code to the generated code's location" +
          ". This makes it easy to remember including it when the generated Java code is distributed."),
      UI.HELP_SETTING,
      new Setting.BoolSetting(PRINT_FILES, false, "print the path of the needed Freeciv source files and exit."
        + " Nothing else is printed.")
    ), args: _*)

    UI.printAndExitOnHelp(settings, classOf[GeneratePackets])

    val needed = readSettings(new File("GeneratePackets/data/core_needs.xml"))

    val requested: List[(String, String)] =
      ((needed \ "requested") \ "_").map(item => item.label -> item.text).toList

    if (settings.getSetting[Boolean](PRINT_FILES)) {
      val conf = GeneratePackets.createVersionConfig(
        settings.getSetting[String](VERSION_INFORMATION),
        settings.getSetting[String](SOURCE_CODE_LOCATION) + "/",
        true)

      val files = conf.inputSources.flatMap(_._2)
        .map((file: String) => settings.getSetting[String](SOURCE_CODE_LOCATION) + "/" + file)

      files.foreach(println(_))

      return
    }

    val self = new GeneratePackets(
      settings.getSetting[String](VERSION_INFORMATION),
      settings.getSetting[String](SOURCE_CODE_LOCATION) + "/",
      requested,
      settings.getSetting[String](PACKETS_SHOULD_LOG_TO),
      settings.getSetting[Boolean](IGNORE_PROBLEMS))

    self.writeToDir(GeneratorDefaults.GENERATED_SOURCE_FOLDER, settings.getSetting[Boolean](GPL_SOURCE))
  }

  def checkFileCanRead(fileToValidate: File) {
    if (!fileToValidate.exists()) {
      throw new IOException(fileToValidate.getAbsolutePath + " doesn't exist.")
    } else if (!fileToValidate.canRead) {
      throw new IOException("Can't read " + fileToValidate.getAbsolutePath)
    }
  }

  def readFileAsString(sourceLocation: String, itemRPath: String): SourceFile = {
    val code: File = new File(sourceLocation + itemRPath)

    GeneratePackets.checkFileCanRead(code)

    val codeFile = new FileReader(code)
    val content = StreamReader(codeFile).source.toString
    codeFile.close()

    return new SourceFile(itemRPath, content)
  }

  def readSettings(listFile: File) = {
    checkFileCanRead(listFile)
    XML.loadFile(listFile)
  }

  def detectFreecivVersion(fc_version: List[Dependency]): String = {
    def stripQuotes(orig: String) = orig.substring(1, orig.length - 1)

    def getExpr(constants: Map[String, Constant[_ <: AValue]], name: String) = {
      if (!constants.contains(name)) {
        throw new Exception("Unable to detect Freeciv version. No " + name + " found")
      }
      constants.get(name).get.getExpression
    }

    def getUnquotedExpr(constants: Map[String, Constant[_ <: AValue]], name: String) =
      stripQuotes(getExpr(constants, name))

    val fc_version_constants: Map[String, Constant[_ <: AValue]] =
      fc_version.filter((dep: Dependency) => dep.isInstanceOf[Constant[_ <: AValue]])
        .map((dep: Dependency) => dep.asInstanceOf[Constant[_ <: AValue]])
        .map((con: Constant[_ <: AValue]) => con.getName -> con).toMap

    val minor_str = getUnquotedExpr(fc_version_constants, "MINOR_VERSION")
    val patch_str = getUnquotedExpr(fc_version_constants, "PATCH_VERSION")
    val major_str = getUnquotedExpr(fc_version_constants, "MAJOR_VERSION")

    val minor = minor_str.toInt + (if ("99".equals(patch_str)) 1 else 0)
    major_str + "." + minor
  }

  private def createVersionConfig(chosenVersion: String, sourceLocation: String,
                                  silent: Boolean = false): VersionConfig = {
    if ("detect".equals(chosenVersion)) {
      /* Auto detect Freeciv version */

      if (!silent)
        println("Reading " + sourceLocation + "fc_version")

      val vpSource: SourceFile =
        GeneratePackets.readFileAsString(sourceLocation, "fc_version")
      val fc_version: List[Dependency] = VariableAssignmentsExtractor.extract(vpSource)

      val version: String = GeneratePackets.detectFreecivVersion(fc_version)

      if (!silent)
        println("Source code of Freeciv " + version + " auto detected.")

      VersionConfig.fromFile("GeneratePackets/config/" + version + ".xml")
    } else {
      VersionConfig.fromFile(chosenVersion)
    }
  }
}

class VersionConfig(val configName: String,
                    val packetHeader: org.freeciv.packetgen.PacketHeaderKinds,
                    val enableDelta: Boolean, val enableDeltaBoolFolding: Boolean,
                    val inputSources: Map[String, Seq[String]]) {
}

object VersionConfig {
  def fromFile(from: File): VersionConfig = {
    val versionConfiguration = GeneratePackets.readSettings(from)

    val configName = versionConfiguration.attribute("name").get.text

    val packetHeader = PacketHeaderKinds.valueOf(versionConfiguration.attribute("packetHeaderKind").get.text)

    val enableDelta = versionConfiguration.attribute("enableDelta").get.text.toBoolean
    val enableDeltaBoolFolding = versionConfiguration.attribute("enableDeltaBoolFolding").get.text.toBoolean

    val inputSources = (versionConfiguration \ "inputSource").map(elem =>
      elem.attribute("parseAs").get.text -> (elem \ "file").map(_.text)).toMap

    new VersionConfig(configName, packetHeader, enableDelta, enableDeltaBoolFolding, inputSources)
  }

  def fromFile(from: String): VersionConfig = fromFile(new File(from))
}
