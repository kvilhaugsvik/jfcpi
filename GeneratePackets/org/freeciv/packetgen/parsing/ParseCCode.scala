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
import org.freeciv.packetgen.enteties.{Enum, Constant, BitVector}
import Enum.EnumElementKnowsNumber.{newEnumValue, newInvalidEnum}
import org.freeciv.packetgen.enteties.supporting.{SimpleTypeAlias, IntExpression}
import util.parsing.input.CharArrayReader
import java.util.{HashSet, HashMap}
import org.freeciv.packetgen._
import enteties.Enum.EnumElementFC

class ParseCCode extends ParseShared {
  def enumElemCode = identifierRegEx

  private final val DEFINE: String = "#define"
  private final val ENDDEFINE = """(\n|\r|\z)+""".r //TODO: Don't match backslash newline
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
    startOfCEnum + "\\s+" + identifier,
    startOfSpecEnum + "\\s+" + identifier
  )

  def defineLine[Ret](start: String, followedBy: Parser[Ret]): Parser[Ret] = {
    val me = opt(ENDDEFINE) ~ start.r ~> followedBy <~ ENDDEFINE
    new Parser[Ret] {
      def apply(in: ParseCCode.this.type#Input): ParseResult[Ret] = {
        val oldIgnoreNewLinesFlag = ignoreNewLinesFlag
        ignoreNewLinesFlag = false
        val oldIgnoreCommentsFlag = ignoreCommentsFlag
        ignoreCommentsFlag = true
        val result = me(in)
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
      val (isSigned, dec) = expandCIntDeclaration(types) match {
        case "unsigned" :: tail => false -> tail
        case "signed" :: tail => true -> tail
        case all => true -> all // signed is default for int. The compiler choose for char.
      }

      val (isNative, wrappedType) = dec match {
        case "char" :: Nil => pickJavaInt(8, isSigned)
        case "short" :: "int" :: Nil => pickJavaInt(16, isSigned)
        case "int" :: Nil => pickJavaInt(32, isSigned) // at least 16 bits. Assume 32 bits
        case "long" :: "int" :: Nil => pickJavaInt(32, isSigned) // at least 32 bits
        case "long" :: "long" :: "int" :: Nil => pickJavaInt(64, isSigned) // at least 64 bits

        case "enum" :: name :: Nil => false -> name
      } // TODO: isSigned and bits can be used to check lower range on unsigned ints

      new SimpleTypeAlias(name, wrappedType, if (isNative) null else dec.reduce(_ + " " + _))
    }
  }

  def pickJavaInt(sizeInBytes: Int, isSigned: Boolean): (Boolean, String) = {
    // Java don't have unsigned so something bigger is needed...
    val realSize: Int = if (isSigned) sizeInBytes else sizeInBytes + 1

    // Java really really likes int so don't use shorter values
    if (realSize <= 32)
      true -> "Integer"
    else if (realSize <= 64)
      true -> "Long"
    else
      throw new UnsupportedOperationException("No Java integer supports " + realSize + " bits." +
        " BigInteger may be used when users of this method can handle making a constructor for it.")
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

  def constantValueDef = defineLine(startOfConstant, identifier.r ~ intExpr)

  def constantValueDefConverted = constantValueDef ^^ {variable => new Constant(variable._1, variable._2)}

  def exprConverted = cEnumDefConverted | specEnumDefConverted | bitVectorDefConverted | typedefConverted |
    constantValueDefConverted

  def expr = cEnumDef |
    specEnumDef |
    struct |
    bitVectorDef |
    typedef |
    constantValueDef |
    CComment
}

class FromCExtractor() {
  def this(toLookFor: Iterable[Requirement]) = this()

  private val parser = new ParseCCode()
  private val lookFor = parser.startsOfExtractable.map("(" + _ + ")").reduce(_ + "|" + _).r

  def findPossibleStartPositions(lookIn: String): List[Int] =
    lookFor.findAllIn(lookIn).matchData.map(_.start).toList

  def extract(lookIn: String) = {
    val positions = findPossibleStartPositions(lookIn)
    val lookInAsReader = new parser.PackratReader(new CharArrayReader(lookIn.toArray))

    positions.map(position => parser.parse(parser.exprConverted, lookInAsReader.drop(position)))
      .filter(!_.isEmpty).map(_.get)
  }

  override def toString = "Extracts(" + parser.startsOfExtractable.map("(" + _ + ")").reduce(_ + "|" + _) + ")"
}
