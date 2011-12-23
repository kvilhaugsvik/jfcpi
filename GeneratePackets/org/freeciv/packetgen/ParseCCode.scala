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

import collection.mutable.ListBuffer
import ClassWriter.EnumElement.{newEnumValue, newInvalidEnum}
import util.parsing.input.CharArrayReader

class ParseCCode(lookFor: List[String]) extends ParseShared {
  def enumDefName: String = "(" + lookFor.reduce(_ + "|" + _) + ")"

  def enumElemCode = regex("""[A-Za-z]\w*""".r)

  private final val DEFINE: String = "#define"
  private final val SPECENUM: String = "SPECENUM_"
  private final val NAME: String = "NAME"

  def startOfSpecEnum: String = DEFINE + "\\s+" + SPECENUM + NAME
  def startOfCEnum: String = "enum"

  def startsOfExtractable = List(
    startOfCEnum + "\\s+" + enumDefName,
    startOfSpecEnum + "\\s+" + enumDefName
  )

  @inline private def se(kind: String) = DEFINE ~> regex((SPECENUM + kind).r) ^^ {_.substring(9)}

  def specEnum(kind: String) = se(kind) ~ enumElemCode

  def specEnumOrName(kind: String) = se(kind + NAME) ~ regex(""""[^\n\r\"]*?"""".r) | specEnum(kind)

  def specEnumDef = regex(startOfSpecEnum.r) ~> regex(enumDefName.r) ~
    (rep((specEnumOrName("VALUE\\d+") |
      specEnumOrName("ZERO") |
      specEnumOrName("COUNT") |
      se("INVALID") ~ sInteger) ^^ {parsed => (parsed._1 -> parsed._2)} |
        CComment ^^ {comment => "comment" -> comment} |
        se("BITWISE") ^^ {bitwise => bitwise -> bitwise}
    ) ^^ {_.toMap[String, String]}) <~
    "#include" ~ "\"specenum_gen.h\""

  def specEnumDefConverted = specEnumDef ^^ {asStructures =>
    if (asStructures._2.isEmpty)
      throw new UndefinedException("No point in porting over an empty enum...")

    @inline def enumerations: Map[String, String] = asStructures._2
    val bitwise = enumerations.contains("BITWISE")

    val outEnumValues: ListBuffer[ClassWriter.EnumElement] = ListBuffer[ClassWriter.EnumElement](
      enumerations.filter((defined) => "VALUE\\d+".r.pattern.matcher(defined._1).matches()).map((element) => {
        @inline def key = element._1
        @inline def nameInCode = enumerations.get(key).get
        @inline def specenumnumber = Integer.decode(key.substring(5))
        val inCodeNumber: Int = if (bitwise)
          Integer.rotateLeft(2, specenumnumber - 1)
        else
          specenumnumber
        if (enumerations.contains(key + NAME))
          newEnumValue(nameInCode, inCodeNumber, enumerations.get(key + NAME).get)
        else
          newEnumValue(nameInCode, inCodeNumber)}).toSeq : _*)
    if (enumerations.contains("ZERO"))
      if (enumerations.contains("ZERO" + NAME))
        outEnumValues += newEnumValue(enumerations.get("ZERO").get, 0, enumerations.get("ZERO" + NAME).get)
    else
        outEnumValues += newEnumValue(enumerations.get("ZERO").get, 0)
    if (enumerations.contains("INVALID"))
      outEnumValues += newInvalidEnum(Integer.parseInt(enumerations.get("INVALID").get))
    if (enumerations.contains("COUNT"))
      if (enumerations.contains("COUNT"+NAME))
        new Enum(asStructures._1.asInstanceOf[String], enumerations.get("COUNT").get,
          enumerations.get("COUNT"+NAME).get, outEnumValues: _*)
      else
        new Enum(asStructures._1.asInstanceOf[String], enumerations.get("COUNT").get, outEnumValues: _*)
    else
      new Enum(asStructures._1.asInstanceOf[String], bitwise, outEnumValues: _*)
  }

  def enumValue = regex("""[0-9]+""".r)

  def cEnum = opt(CComment) ~> enumElemCode ~ opt("=" ~> enumValue) <~ opt(CComment)

  def cEnumDef = regex(startOfCEnum.r) ~> regex(enumDefName.r) ~ ("{" ~> repsep(cEnum, ",") <~ "}")

  def cEnumDefConverted = cEnumDef ^^ {asStructures =>
    var globalNumbers: Int = 0
    new Enum(asStructures._1.asInstanceOf[String],
      false,
      asStructures._2.map(elem => {
        if (!elem._2.isEmpty)
          globalNumbers = Integer.decode(elem._2.get)
        val number = globalNumbers
        globalNumbers += 1
        newEnumValue(elem._1, number)
      }): _*)
  }

  def exprConverted = cEnumDefConverted | specEnumDefConverted

  def expr = cEnumDef |
    specEnumDef |
    CComment
}

class FromCExtractor(toLookFor: List[String]) {
  if (Nil.eq(toLookFor))
    throw new IllegalArgumentException("Nothing looked for so nothing to extract")

  private val parser = new ParseCCode(toLookFor)
  private val lookFor = parser.startsOfExtractable.map("(" + _ + ")").reduce(_ + "|" + _).r

  def findPossibleStartPositions(lookIn: String): List[Int] =
    lookFor.findAllIn(lookIn).matchData.map(_.start).toList

  def extract(lookIn: String) = {
    val positions = findPossibleStartPositions(lookIn)
    val lookInAsReader = new CharArrayReader(lookIn.toArray)

    positions.map(positions => parser.parse(parser.exprConverted, lookInAsReader.drop(positions)).get)
  }
}
