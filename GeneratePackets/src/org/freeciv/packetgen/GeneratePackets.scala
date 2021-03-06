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
                      requested: List[(String, String)], devMode: Boolean) {
  def this(chosenVersion: String, sourceLocation: String,
           requested: List[(String, String)], devMode: Boolean) {
    this(GeneratePackets.createVersionConfig(chosenVersion, sourceLocation),
      sourceLocation, requested, devMode)
  }

  private val packetsDefRPath: String = versionConfig.inputSources("packets").head

  private val storage = new PacketsStore(versionConfig.configName,
    versionConfig.packetHeader, versionConfig.fieldTypeAliases,
    versionConfig.enableDelta, versionConfig.enableDeltaBoolFolding)
  private val Parser = new ParsePacketsDef(storage)

  requested.filter(item => "constant".equals(item._1)).foreach(cons => storage.requestConstant(cons._2))
  requested.filter(item => "type".equals(item._1)).foreach(cons => storage.requestType(cons._2))

  private def createAndRegister(location: String, cr: String): SourceFile = {
    val src = GeneratePackets.readSourceFile(location, cr)
    storage.addSource(src)
    src
  }

  // checking that all input files are there BEFORE wasting time working on some of it is now done during reading
  println("Reading the source code")

  private val pdSource: SourceFile = GeneratePackets.readSourceFile(sourceLocation, packetsDefRPath)
  storage.addSource(pdSource)

  private val vpSource: SourceFile =
    GeneratePackets.readSourceFile(sourceLocation, versionConfig.inputSources("variables").head)
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

  println("Post processing")
  storage.doPostProcessing()

  /**
   * Write the generated code (and maybe its input files) to the given directory.
   * @param path the output folder were the files should be written.
   * @param includeOrigSrc should the input files be copied to the output folder?
   */
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

/**
 * Generates Java code that helps understanding the Freeciv protocol from the Freeciv source code.
 */
object GeneratePackets {
  /* Command line arguments */
  private val SOURCE_CODE_LOCATION = "source-code-location"
  private val VERSION_INFORMATION = "version-information"
  private val DEST_DIR_LOCATION = "dest-dir-location"
  private val IGNORE_PROBLEMS = "ignore-problems"
  private val GPL_SOURCE = "gpl-source"
  private val PRINT_FILES = "print-source-files"
  private val PRINT_VER_SER = "print-source-version-series"

  /* Special values */
  private val DETECT_VERSION: String = "detect"

  def main(args: Array[String]) {
    val settings = new ArgumentSettings(List(
      new Setting.StringSetting(SOURCE_CODE_LOCATION, GeneratorDefaults.FREECIV_SOURCE_PATH,
        "the location of the Freeciv source code to generate from"),
      new Setting.StringSetting(VERSION_INFORMATION, GeneratorDefaults.VERSIONCONFIGURATION,
        "file containing settings for the version of Freeciv."
          + " Set to \"" + DETECT_VERSION + "\" to use the default"
          + " settings for your Freeciv source version."),
      new Setting.StringSetting(DEST_DIR_LOCATION, GeneratorDefaults.GENERATED_SOURCE_FOLDER,
        "the location of the folder to write the generated packet code to"),
      new Setting.BoolSetting(IGNORE_PROBLEMS, GeneratorDefaults.IGNORE_ISSUES,
        "should problems be ignored?"),
      new Setting.BoolSetting(GPL_SOURCE, GeneratorDefaults.NOT_DISTRIBUTED_WITH_FREECIV,
        "copy the Freeciv source code used as a source to generate Java code to the generated code's location" +
          ". This makes it easy to remember including it when the generated Java code is distributed."),
      UI.HELP_SETTING,
      new Setting.BoolSetting(PRINT_FILES, false,
        "print the path of the required Freeciv source files and the file that claims they are required."
        + " Exit once done. Nothing else should be printed."),
      new Setting.BoolSetting(PRINT_VER_SER, false,
        "print the version series the Freeciv source files belongs to."
          + " Exit once done. Nothing else should be printed.")
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

      /* The location of the list of required Freeciv source code files. */
      println(conf.configFile)

      /* The required Freeciv source code files. */
      files.foreach(println(_))

      return
    } else if (settings.getSetting[Boolean](PRINT_VER_SER)) {
      /* The file fc_version contains the raw version number. */
      val vpSource: SourceFile =
        GeneratePackets.readSourceFile(settings.getSetting[String](SOURCE_CODE_LOCATION) + "/", "fc_version")

      /* Find the Freeciv version series the version belongs to. */
      val series = GeneratePackets.detectFreecivVersion(vpSource)

      /* Print the Freeciv version series */
      println(series)

      /* Nothing more to do, */
      return
    }

    val self = new GeneratePackets(
      settings.getSetting[String](VERSION_INFORMATION),
      settings.getSetting[String](SOURCE_CODE_LOCATION) + "/",
      requested,
      settings.getSetting[Boolean](IGNORE_PROBLEMS))

    self.writeToDir(settings.getSetting[String](DEST_DIR_LOCATION),
      settings.getSetting[Boolean](GPL_SOURCE))
  }

  /**
   * Check that the given file can be read,
   * @param fileToValidate the file to test if can be read.
   * @throws IOException if the file don't exist or can't be read
   */
  def checkFileCanRead(fileToValidate: File) {
    if (!fileToValidate.exists()) {
      throw new IOException(fileToValidate.getAbsolutePath + " doesn't exist.")
    } else if (!fileToValidate.canRead) {
      throw new IOException("Can't read " + fileToValidate.getAbsolutePath)
    }
  }

  /**
   * Read the given file into memory.
   * @param sourceLocation location of the file
   * @param itemRPath name of the file
   * @return a SourceFile with the content of the file and its path
   * @throws IOException if the file don't exist or can't be read
   */
  def readSourceFile(sourceLocation: String, itemRPath: String): SourceFile = {
    val code: File = new File(sourceLocation + itemRPath)

    GeneratePackets.checkFileCanRead(code)

    val codeFile = new FileReader(code)
    val content = StreamReader(codeFile).source.toString
    codeFile.close()

    return new SourceFile(itemRPath, content)
  }

  /**
   * Read the settings file.
   * @param listFile the setting file.
   * @return the settings
   * @throws IOException if the file don't exist or can't be read
   */
  def readSettings(listFile: File) = {
    checkFileCanRead(listFile)
    XML.loadFile(listFile)
  }

  /**
   * Get a string containing the major and minor version numbers this
   * version of Freeciv will have when it is released.
   * @param major_str The current major version.
   * @param minor_str The current minor version.
   * @param patch_str The current patch version. (Needed to see if this is
   *                  a development version)
   * @return The string "majorVersion.minorVersion" the released Freeciv
   *         version will have.
   */
  def getFinalFreecivVersion(major_str: String, minor_str: String, patch_str: String): String = {
    /* A patch version number of 99 means that this is a development
     * version. A minor version number of 90 means that this is the
     * development version of a Freeciv version that will increase the
     * major version number. */

    val major = if (minor_str.toInt < 90) {
      /* This is a released version or the development version of a minor
       * version upgrade. */
      major_str.toInt
    } else {
      /* This is the development version of a Freeciv version that will
       * increase the major version number. */
      major_str.toInt + 1
    }

    val minor = if (minor_str.toInt < 90) {
      /* This is a released version or the development version of a version
       * that will keep the major version number. */
      if ("99".equals(patch_str)) {
        /* This is a development version. Minor version will be higher. */
        minor_str.toInt + 1
      } else {
        /* This is a released version. */
        minor_str.toInt
      }
    } else {
      /* This is the development version of a major version increasing
       version. */
      0
    }

    major + "." + minor
  }

  /**
   * Detect the major/minor version of the Freeciv source code from the file fc_version.
   * @param fc_version the fc_version file from the root dir of the Freeciv source tree
   * @return a version string consisting of the major and minor version (eg 2.4, 2.6, etc)
   */
  def detectFreecivVersion(fc_version: SourceFile): String = {
    val fc_version_vars: List[Dependency] = VariableAssignmentsExtractor.extract(fc_version)

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
      fc_version_vars.filter((dep: Dependency) => dep.isInstanceOf[Constant[_ <: AValue]])
        .map((dep: Dependency) => dep.asInstanceOf[Constant[_ <: AValue]])
        .map((con: Constant[_ <: AValue]) => con.getName -> con).toMap

    val minor_str = getUnquotedExpr(fc_version_constants, "MINOR_VERSION")
    val patch_str = getUnquotedExpr(fc_version_constants, "PATCH_VERSION")
    val major_str = getUnquotedExpr(fc_version_constants, "MAJOR_VERSION")

    getFinalFreecivVersion(major_str, minor_str, patch_str)
  }

  private def createVersionConfig(chosenVersion: String, sourceLocation: String,
                                  silent: Boolean = false): VersionConfig = {
    if (DETECT_VERSION.equals(chosenVersion)) {
      /* Auto detect Freeciv version */

      if (!silent)
        println("Reading " + sourceLocation + "fc_version")

      val vpSource: SourceFile =
        GeneratePackets.readSourceFile(sourceLocation, "fc_version")
      val version: String = GeneratePackets.detectFreecivVersion(vpSource)

      if (!silent)
        println("Source code of Freeciv " + version + " auto detected.")

      VersionConfig.fromFile("GeneratePackets/config/" + version + ".xml")
    } else {
      VersionConfig.fromFile(chosenVersion)
    }
  }
}
