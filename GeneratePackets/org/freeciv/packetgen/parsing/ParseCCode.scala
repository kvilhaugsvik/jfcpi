/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.parsing

import collection.mutable.ListBuffer
import org.freeciv.packetgen.dependency.Requirement
import org.freeciv.packetgen.javaGenerator.ClassWriter
import scala.collection.JavaConverters.seqAsJavaListConverter
import org.freeciv.packetgen.enteties.{Struct, Enum, Constant, BitVector}
import org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.{newEnumValue, newInvalidEnum}
import org.freeciv.packetgen.enteties.supporting.{SimpleTypeAlias, IntExpression}
import util.parsing.input.CharArrayReader
import java.util.{HashSet, HashMap}
import org.freeciv.packetgen.enteties.Enum.EnumElementFC
import org.freeciv.packetgen.UndefinedException
import java.util.AbstractMap.SimpleImmutableEntry

object ParseCCode extends ExtractableParser {
  def enumElemCode = identifierRegEx

  private final val DEFINE: String = "#define"
  private final val SPECENUM: String = "SPECENUM_"
  private final val NAME: String = "NAME"

  def startOfSpecEnum: String = DEFINE + "\\s+" + SPECENUM + NAME

  def startOfCEnum: String = "enum"

  def startOfConstant: String = DEFINE

  def startOfTypeDefinition: String = "typedef"

  def startOfBitVector: String = "BV_DEFINE"

  def startOfStruct: String = "struct"

  def startsOfExtractable = List(
    startOfConstant + "\\s+" + identifier,
    startOfTypeDefinition + "\\s+",
    startOfBitVector,
    startOfStruct + "\\s+" + identifier,
    startOfCEnum + "\\s+" + identifier,
    startOfSpecEnum + "\\s+" + identifier
  )

  def defineLine[Ret](start: String, followedBy: Parser[Ret]): Parser[Ret] = {
    val me = followedBy <~ """(\n|\r|\z)""".r
    new Parser[Ret] {
      def apply(in: ParseCCode.this.type#Input): ParseResult[Ret] = {
        // Save old state
        val oldIgnoreCommentsFlag = ignoreCommentsFlag
        val oldIgnoreNewLinesFlag = ignoreNewLinesFlag

        // Look for the start of a define ignoring comments and newlines
        ignoreCommentsFlag = true
        ignoreNewLinesFlag = true
        val beginning = regex(start.r)(in)

        val result = if (beginning.successful) {
          ignoreNewLinesFlag = false
          me(beginning.next)
        } else {
          (regex(start.r) ~> me)(in) // TODO: fail in a cheaper way
        }

        // restore old state
        ignoreNewLinesFlag = oldIgnoreNewLinesFlag
        ignoreCommentsFlag = oldIgnoreCommentsFlag
        return result
      }
    }
  }

  @inline private def se(kind: String) =
    defineLine(DEFINE, (regex((SPECENUM + kind).r) ^^ {_.substring(9)}))

  //TODO: join the se above and the one below. Only difference is taking followBy as a parameter and use it
  @inline private def se[Ret](kind: String, followedBy: Parser[Ret]) =
    defineLine(DEFINE, (regex((SPECENUM + kind).r) ^^ {_.substring(9)}) ~ followedBy)

  def specEnumOrName(kind: String) = se(kind + NAME, quotedString.r) |
    se(kind, enumElemCode)

  def specEnumDef = defineLine(startOfSpecEnum, regex(identifier.r)) ~
    (rep((specEnumOrName("VALUE\\d+") |
      specEnumOrName("ZERO") |
      specEnumOrName("COUNT") |
      se("INVALID", sInteger)) ^^ {parsed => (parsed._1 -> parsed._2)} |
      CComment ^^ {comment => "comment" -> comment} |
      se("BITWISE") ^^ {bitwise => bitwise -> bitwise}
    ) ^^ {_.toMap[String, String]}) <~
    "#include" ~ "\"specenum_gen.h\""

  def specEnumDefConverted = specEnumDef ^^ {
    asStructures =>
      if (asStructures._2.isEmpty)
        throw new UndefinedException("No point in porting over an empty enum...")

      @inline def enumerations: Map[String, String] = asStructures._2
      val bitwise = enumerations.contains("BITWISE")

      val outEnumValues: ListBuffer[Enum.EnumElementKnowsNumber] = ListBuffer[Enum.EnumElementKnowsNumber](
        enumerations.filter((defined) => "VALUE\\d+".r.pattern.matcher(defined._1).matches()).map((element) => {
          @inline def key = element._1
          @inline def nameInCode = enumerations.get(key).get
          @inline def specenumnumber = key.substring(5).toInt
          val inCodeNumber: Int = if (bitwise)
            Integer.rotateLeft(2, specenumnumber - 1)
          else
            specenumnumber
          if (enumerations.contains(key + NAME))
            newEnumValue(nameInCode, inCodeNumber, enumerations.get(key + NAME).get)
          else
            newEnumValue(nameInCode, inCodeNumber)
        }).toSeq: _*)
      if (enumerations.contains("ZERO"))
        if (enumerations.contains("ZERO" + NAME))
          outEnumValues += newEnumValue(enumerations.get("ZERO").get, 0, enumerations.get("ZERO" + NAME).get)
        else
          outEnumValues += newEnumValue(enumerations.get("ZERO").get, 0)
      if (enumerations.contains("INVALID"))
        outEnumValues += newInvalidEnum(Integer.parseInt(enumerations.get("INVALID").get))
      else
        outEnumValues += newInvalidEnum(-1) // All spec enums have an invalid. Default value is -1
      val sortedEnumValues: List[EnumElementFC] = outEnumValues.sortWith(_.getNumber < _.getNumber).toList
      if (enumerations.contains("COUNT"))
        if (enumerations.contains("COUNT" + NAME))
          new Enum(asStructures._1.asInstanceOf[String], enumerations.get("COUNT").get,
            enumerations.get("COUNT" + NAME).get, sortedEnumValues.asJava)
        else
          new Enum(asStructures._1.asInstanceOf[String], enumerations.get("COUNT").get, sortedEnumValues.asJava)
      else
        new Enum(asStructures._1.asInstanceOf[String], bitwise, sortedEnumValues.asJava)
  }

  def enumValue = intExpr

  def cEnum = opt(CComment) ~> enumElemCode ~ opt("=" ~> enumValue) <~ opt(CComment) ^^ {
    case element ~ value => (element -> value)
  }

  def cEnumDef = regex(startOfCEnum.r) ~> regex(identifier.r) ~ ("{" ~> repsep(cEnum, ",") <~ opt(",") ~ "}")

  def cEnumDefConverted = cEnumDef ^^ {
    asStructures => {
      var iRequire = new HashSet[Requirement]()

      def countedCEnumElements(elements: List[(String, Option[IntExpression])]) = {
        var globalNumberExpression: IntExpression = IntExpression.integer("0")
        val alreadyReadExpression = new HashMap[String, EnumElementFC]()

        @inline def isAnInterpretedConstantOnThis(value: IntExpression): Boolean =
          alreadyReadExpression.containsKey(value.toStringNotJava)

        def countParanoid(name: String, registeredValue: Option[IntExpression]): EnumElementFC = {
          if (!registeredValue.isEmpty) { // Value is specified
            if (registeredValue.get.hasNoVariables)
              globalNumberExpression = registeredValue.get
            else {
              globalNumberExpression = registeredValue.get.valueMap(value => {
                if (isAnInterpretedConstantOnThis(value)) {
                  value.toStringNotJava + ".getNumber()"
                } else {
                  iRequire.addAll(value.getReqs)
                  value.toString
                }
              })
            }
          }
          val number = globalNumberExpression
          globalNumberExpression = IntExpression.binary("+",
            IntExpression.integer("1"),
            IntExpression.handled(name + ".getNumber()"))
          val enumVal = EnumElementFC.newEnumValue(name, number.toString)
          alreadyReadExpression.put(name, enumVal)
          enumVal
        }

        elements.map(elem => countParanoid(elem._1, elem._2))
      }

      new Enum(asStructures._1.asInstanceOf[String],
        iRequire,
        countedCEnumElements(asStructures._2).asJava)
    }
  }

  private var ignoreNewLinesFlag = true

  private var ignoreCommentsFlag = false

  protected def isNewLineIgnored(source: CharSequence, offset: Int): Boolean = ignoreNewLinesFlag

  protected def areCommentsIgnored(source: CharSequence, offset: Int): Boolean = ignoreCommentsFlag

  def typedef = startOfTypeDefinition ~> cType ~ identifierRegEx <~ ";"

  // TODO: if needed: support defining a anonymous enum, struct or union instead of refering to an existing definition
  // in that case throw away typedef and return the anon
  def typedefConverted = typedef ^^ {
    case types ~ name => {
      val translatedTypes = cTypeDecsToJava(types)
      new SimpleTypeAlias(name, translatedTypes._1, translatedTypes._2)
    }
  }

  def bitVectorDef = startOfBitVector ~ "(" ~> identifierRegEx ~ ("," ~> intExpr) <~ ")" ~ ";"

  def bitVectorDefConverted = bitVectorDef ^^ {vec => new BitVector(vec._1, vec._2)}

  def struct: Parser[(String, List[(List[String], String)])] = {
    val me = startOfStruct ~> identifierRegEx ~ ("{" ~>
      rep1(cType ~ identifierRegEx <~ ";" ^^ {element => element._1 -> element._2}) <~
      "}" ~ ";") ^^ {struct => struct._1 -> struct._2}

    new Parser[(String, List[(List[String], String)])] {
      def apply(in: ParseCCode.this.type#Input): ParseResult[(String, List[(List[String], String)])] = {
        val oldIgnoreCommentsFlag = ignoreCommentsFlag
        ignoreCommentsFlag = true
        val result: ParseResult[(String, List[(List[String], String)])] = me(in)
        ignoreCommentsFlag = oldIgnoreCommentsFlag
        return result
      }
    }
  }

  def structConverted = struct ^^ {
    val willRequire = new java.util.HashSet[Requirement]()
    struct => new Struct(struct._1, struct._2.map(entry => {
      val fieldType = cTypeDecsToJava(entry._1)
      willRequire.addAll(fieldType._2)
      new SimpleImmutableEntry[String, String](fieldType._1,
        entry._2): java.util.Map.Entry[String, String]
    }).asJava,
      willRequire)
  }

  def constantValueDef = defineLine(startOfConstant, identifier.r ~ intExpr)

  def constantValueDefConverted = constantValueDef ^^ {variable => new Constant(variable._1, variable._2)}

  def exprConverted = cEnumDefConverted |
    specEnumDefConverted |
    structConverted |
    bitVectorDefConverted |
    typedefConverted |
    constantValueDefConverted

  def expr = cEnumDef |
    specEnumDef |
    struct |
    bitVectorDef |
    typedef |
    constantValueDef |
    CComment
}

object FromCExtractor extends ExtractorShared(ParseCCode)
