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
import com.kvilhaugsvik.dependency.{Dependency, Requirement}
import com.kvilhaugsvik.javaGenerator.ClassWriter
import scala.collection.JavaConverters.seqAsJavaListConverter
import org.freeciv.packetgen.enteties.{Struct, Enum, Constant, BitVector}
import org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.{newEnumValue, newInvalidEnum}
import util.parsing.input.CharArrayReader
import java.util.{HashSet, HashMap}
import org.freeciv.packetgen.enteties.Enum.EnumElementFC
import org.freeciv.packetgen.{GeneratorDefaults}
import java.util.AbstractMap.SimpleImmutableEntry
import org.freeciv.packetgen.enteties.supporting.{StructMaker, WeakVarDec, SimpleTypeAlias, IntExpression}
import com.kvilhaugsvik.javaGenerator.util.BuiltIn
import com.kvilhaugsvik.dependency.UndefinedException
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AString

object ParseCCode extends ExtractableParser {
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
        val oldIgnoreLineWSFlag = ignoreLineWSFlag

        // Look for the start of a define ignoring comments and newlines
        ignoreCommentsFlag = true
        ignoreLineWSFlag = false
        val beginning = regex(("\\s*" + start + "\\s+").r)(in)
        ignoreLineWSFlag = oldIgnoreLineWSFlag

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

  @inline private def se(kind: String, followedBy: Parser[String]): Parser[(String, String)] =
    se(kind, start => start ~ followedBy ^^ {case a ~ b => a -> b})

  @inline private def se(kind: String,
                         followedBy: (Parser[String] => Parser[(String, String)]) = start => start ^^ {_ -> ""}):
  Parser[(String, String)] =
    defineLine(DEFINE, followedBy(regex((SPECENUM + kind).r) ^^ {_.substring(9)}))

  def specEnumOrName(kind: String): Parser[(String, String)] = se(kind + NAME, quotedString.r) |
    se(kind, identifierRegEx)

  def specEnumDef: Parser[~[String, Map[String, String]]] = defineLine(startOfSpecEnum, regex(identifier.r)) ~
    (rep((specEnumOrName("VALUE\\d+") |
      specEnumOrName("ZERO") |
      specEnumOrName("COUNT") |
      se("INVALID", sInteger)) ^^ {parsed => (parsed._1 -> parsed._2)} |
      CComment ^^ {comment => "comment" -> comment} |
      se("NAMEOVERRIDE") ^^ {nameOverride => nameOverride._1 ->
        "override element names (probably from the ruleset)"} |
      se("BITWISE") ^^ {bitwise => bitwise._1 -> bitwise._1} |
      constantValueDef ^^^ {"constant" -> "value"}
    ) ^^ {_.toMap[String, String]}) <~
    "#include" ~ "\"specenum_gen.h\""

  def specEnumDefConverted = specEnumDef ^^ {
    asStructures =>
      if (asStructures._2.isEmpty)
        throw new UndefinedException("No point in porting over an empty enum...")

      @inline def enumerations: Map[String, String] = asStructures._2
      val bitwise = enumerations.contains("BITWISE")
      val nameOverride = enumerations.contains("NAMEOVERRIDE")

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
          Enum.specEnum(asStructures._1.asInstanceOf[String], nameOverride, enumerations.get("COUNT").get,
            enumerations.get("COUNT" + NAME).get, sortedEnumValues.asJava)
        else
          Enum.specEnum(asStructures._1.asInstanceOf[String], nameOverride, enumerations.get("COUNT").get,
            sortedEnumValues.asJava)
      else
        Enum.specEnum(asStructures._1.asInstanceOf[String], nameOverride, bitwise, sortedEnumValues.asJava)
  }

  def enumValue = intExpr

  def cEnum = opt(CComment) ~> identifierRegEx ~ opt("=" ~> enumValue) <~ opt(CComment) ^^ {
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
          value.mayBeConstant && alreadyReadExpression.containsKey(value.nameOfConstant())

        def countParanoid(name: String, registeredValue: Option[IntExpression]): EnumElementFC = {
          if (!registeredValue.isEmpty) { // Value is specified
            if (registeredValue.get.hasNoVariables)
              globalNumberExpression = registeredValue.get
            else {
              globalNumberExpression = registeredValue.get.valueMap(value => {
                if (isAnInterpretedConstantOnThis(value)) {
                  BuiltIn.toCode(value.nameOfConstant() + ".getNumber()")
                } else {
                  iRequire.addAll(value.getReqs)
                  value
                }
              })
            }
          }
          val number = globalNumberExpression
          globalNumberExpression = IntExpression.binary("+",
            IntExpression.integer("1"),
            IntExpression.handled(BuiltIn.toCode(name + ".getNumber()")))
          val enumVal = EnumElementFC.newEnumValue(name, number)
          alreadyReadExpression.put(name, enumVal)
          enumVal
        }

        elements.map(elem => countParanoid(elem._1, elem._2))
      }

      Enum.cEnum(asStructures._1.asInstanceOf[String],
        iRequire,
        countedCEnumElements(asStructures._2).asJava)
    }
  }

  private var ignoreNewLinesFlag = true

  private var ignoreLineWSFlag = true

  private var ignoreCommentsFlag = false

  protected def isNewLineIgnored(source: CharSequence, offset: Int): Boolean = ignoreNewLinesFlag

  protected def isLineWSIgnored(source: CharSequence, offset: Int): Boolean = ignoreLineWSFlag

  protected def areCommentsIgnored(source: CharSequence, offset: Int): Boolean = ignoreCommentsFlag

  def typedef = startOfTypeDefinition ~> cType ~ identifierRegEx <~ ";"

  // TODO: if needed: support defining a anonymous enum, struct or union instead of refering to an existing definition
  // in that case throw away typedef and return the anon
  def typedefConverted = typedef ^^ {
    case types ~ name => {
      val translatedTypes = cTypeDecsToJava(types)
      new SimpleTypeAlias.Incomplete(name, translatedTypes)
    }
  }

  def bitVectorDef = startOfBitVector ~ "(" ~> identifierRegEx ~ ("," ~> intExpr) <~ ")" ~ ";"

  def bitVectorDefConverted = bitVectorDef ^^ {vec => new BitVector(vec._1, vec._2)}

  def varDec: Parser[WeakVarDec] =
    (cType ~ identifierRegEx ~ rep("[" ~> intExpr <~ "]") <~ ";") ^^ {
      case cTypeDecs ~ name ~ arrayDecs =>
        val typeNotArray = cTypeDecsToJava(ArrayOf(cTypeDecs, arrayDecs.size))
        new WeakVarDec(typeNotArray, name, arrayDecs.map(new WeakVarDec.ArrayDeclaration(_)):_*)
  }

  def struct: Parser[(String, List[WeakVarDec])] = {
    val me = (startOfStruct ~> identifierRegEx ~ ("{" ~> rep1(varDec)) <~ "}" ~ ";") ^^ {id => id._1 -> id._2}

    new Parser[(String, List[WeakVarDec])] {
      def apply(in: ParseCCode.this.type#Input): ParseResult[(String, List[WeakVarDec])] = {
        val oldIgnoreCommentsFlag = ignoreCommentsFlag
        ignoreCommentsFlag = true
        val result: ParseResult[(String, List[WeakVarDec])] = me(in)
        ignoreCommentsFlag = oldIgnoreCommentsFlag
        return result
      }
    }
  }

  def structConverted = struct ^^ {
    struct => new StructMaker(struct._1, struct._2.asJava)
  }

  def constantValueDef = defineLine(startOfConstant, identifier.r ~ (intExpr | strExpr))

  def constantValueDefConverted = constantValueDef ^^ {
    case (name: String) ~ (value: IntExpression) => Constant.isInt(name, value)
    case (name: String) ~ (value: Typed[AString]) => Constant.isString(name, value)
    case _ => throw new UnsupportedOperationException("Can't handle this kind of constant.")
  }

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
