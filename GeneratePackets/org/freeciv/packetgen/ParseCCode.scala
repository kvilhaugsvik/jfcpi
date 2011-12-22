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

class ParseCCode(lookFor: List[String]) extends ParseShared {
  def enumDefname =
    lookFor.foldRight[Parser[Any]](failure("Nothing found"))((prefer: String, ifNot: Parser[Any]) => (prefer | ifNot))

  def enumElemCode = regex("""[A-Za-z]\w*""".r)

  @inline private def se(kind: String) = "#define" ~> regex(("SPECENUM_" + kind).r) ^^ {_.substring(9)}

  def specEnum(kind: String) = se(kind) ~ enumElemCode

  def specEnumOrName(kind: String) = se(kind + "NAME") ~ regex(""""[^\n\r\"]*?"""".r) | specEnum(kind)

  def specEnumDef = se("NAME") ~> enumDefname ~
    (rep((specEnumOrName("VALUE\\d+") |
      specEnumOrName("ZERO") |
      specEnumOrName("COUNT") |
      specEnum("INVALID")) ^^ {parsed => (parsed._1 -> parsed._2)} |
        CComment ^^ {comment => "comment" -> comment} |
        se("BITWISE") ^^ {bitwise => bitwise -> bitwise}
    ) ^^ {_.toMap[String, String]}) <~
    "#include" ~ "\"specenum_gen.h\""

  def specEnumDefConverted = specEnumDef ^^ {asStructures =>
    if (asStructures._2.isEmpty)
      throw new UndefinedException("No point in porting over an empty enum...")

    @inline def enumerations: Map[String, String] = asStructures._2
    val bitwise = enumerations.contains("BITWISE")

    val outEnumValues = new ListBuffer[ClassWriter.EnumElement]()
    if (enumerations.contains("ZERO"))
      if (enumerations.contains("ZERO" + "NAME"))
        outEnumValues += Enum.newEnumValue(enumerations.get("ZERO").get, 0, enumerations.get("ZERO" + "NAME").get)
    else
        outEnumValues += Enum.newEnumValue(enumerations.get("ZERO").get, 0)
    val Recognizer = "(VALUE)(\\d+)".r
    enumerations.keys.foreach({
      case Recognizer(value: String, number: String) =>
        val nameInCode: String = enumerations.get(value + number).get
        val num: Int = if (bitwise)
          Integer.rotateLeft(2, Integer.decode(number) - 1)
        else
          Integer.decode(number)
        if (enumerations.contains(value + number + "NAME"))
          outEnumValues += Enum.newEnumValue(nameInCode, num, enumerations.get(value + number + "NAME").get)
        else
          outEnumValues += Enum.newEnumValue(nameInCode, num)
      case _ =>
    })
    new Enum(asStructures._1.asInstanceOf[String], bitwise, outEnumValues: _*)
  }

  def enumValue = regex("""[0-9]+""".r)

  def cEnum = opt(CComment) ~> enumElemCode ~ opt("=" ~> enumValue) <~ opt(CComment)

  def cEnumDef = "enum" ~> enumDefname ~ ("{" ~> repsep(cEnum, ",") <~ "}")

  def cEnumDefConverted = cEnumDef ^^ {asStructures =>
    var globalNumbers: Int = 0
    new Enum(asStructures._1.asInstanceOf[String],
      false,
      asStructures._2.map(elem => {
        if (!elem._2.isEmpty)
          globalNumbers = Integer.decode(elem._2.get)
        val number = globalNumbers
        globalNumbers += 1
        Enum.newEnumValue(elem._1, number)
      }): _*)
  }

  def expr = cEnumDef |
    specEnumDef |
    CComment
}
