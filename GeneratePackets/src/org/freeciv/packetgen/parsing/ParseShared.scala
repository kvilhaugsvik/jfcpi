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

import util.parsing.combinator._
import org.freeciv.packetgen.enteties.supporting.{DataType, IntExpression}
import com.kvilhaugsvik.dependency.{Dependency, Requirement}
import util.parsing.input.CharArrayReader
import org.freeciv.packetgen.enteties.SourceFile
import java.io.File
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn._
import com.kvilhaugsvik.javaGenerator.util.BuiltIn

abstract class ParseShared extends RegexParsers with PackratParsers {
  def expr: Parser[Any]

  def exprs: Parser[Any] = rep(expr)

  def CComment: Parser[String] = (cStyleStart.r ~> rep(cStyleMiddle.r) <~ cStyleEnd.r) ^^ {_.reduce(_ + _)} |
    regex(cStyleManyStars.r) ^^^ null |
    regex(cXXStyleComment.r)

  protected val cXXStyleComment = """//[^\n\r]*"""
  private val cStyleManyStars = """/\*+\*/"""
  private val cStyleStart = """/\*+"""
  private val cStyleMiddle = """([^*\n\r]|\*+[^/*])+"""
  private val cStyleEnd = """\*+/"""
  protected val cStyleComment = regExOr(cStyleManyStars, cStyleStart + "(" + cStyleMiddle + """[\n\r]?)*""" + cStyleEnd)

  protected val spaceBetweenWords = """[\t ]"""

  /**
    * Build a regular expression matching all strings any of the regular
    * expressions given as arguments would match.
    * @param arg all regular expressions the resulting regex should match.
    * @return the regular expressions joined in a String.
    */
  protected def regExOr(arg: String*): String =
    "(" + arg.map("(" + _ + ")").reduce(_ + "|" + _) + ")"

  def sInteger = """[+|-]*[0-9]+""".r

  def identifier = """[A-Za-z]\w*"""

  val identifierRegEx = identifier.r

  def quotedString = """\"[^"]*?\""""

  // TODO: Should concatenation be supported?
  def strExpr = quotedString.r ^^ {a => BuiltIn.toCode[AString](a)}

  /**
    * A String with regular expression that recognizes char literals.
    */
  val charLiteral: String = "'" + regExOr(".", """\\\d+""") + "'"

  /**
    * Parse a char literal
    * @return the literal marked as a char
    */
  def charExpr = charLiteral.r ^^ {ch => BuiltIn.toCode[AChar](ch)}

  private def binOpLev(operators: Parser[String]): PackratParser[(IntExpression, IntExpression) => IntExpression] =
    operators ^^ {operator => (lhs: IntExpression, rhs: IntExpression) => IntExpression.binary(operator, lhs, rhs)}

  private val intExprLevelBinAdd: PackratParser[IntExpression] = chainl1(intExprLevelBinMul, binOpLev(("+" | "-")))

  private val intExprLevelBinMul: PackratParser[IntExpression] = chainl1(intExprLevelUnary, binOpLev(("*" | "/" | "%")))

  private val intExprLevelUnary: PackratParser[IntExpression] =
    "--" ~> intExprLevelPrimary ^^ {IntExpression.unary("--", _)} |
      "++" ~> intExprLevelPrimary ^^ {IntExpression.unary("++", _)} |
      "-" ~> intExprLevelPrimary ^^ {IntExpression.unary("-", _)} |
      "+" ~> intExprLevelPrimary |
      intExprLevelPrimary

  private val intExprLevelPrimary: PackratParser[IntExpression] =
    (intExprBasic <~ "--") ^^ {IntExpression.suf(_, "--")} |
      (intExprBasic <~ "++") ^^ {IntExpression.suf(_, "++")} |
      intExprBasic

  private val intExprBasic: Parser[IntExpression] =
    """0x[0-9a-fA-F]+""".r ^^ {IntExpression.integer(_)} |
      """[0-9]+""".r ^^ {IntExpression.integer(_)} |
      "(" ~ (cTypeIntegerNumber | ("enum" ~ identifierRegEx)) ~ ")" ~> intExpr |
      identifierRegEx ^^ {IntExpression.variable(_)} |
      charExpr ^^ {IntExpression.charValue(_)} |
      "(" ~> intExpr <~ ")" |
      intExpr

  val intExpr: Parser[IntExpression] = intExprLevelBinAdd

  // TODO: if needed: support defining an anonymous enum, struct or union
  def cType: Parser[ParseType] = cTypePointer | cTypeCanStart

  def cTypeCanStart: Parser[ParseType] = cTypeComplex | cTypeIntegerNumber | cTypeName

  def cTypeComplex: Parser[Complex] = ("struct" | "union" | "enum") ~ identifierRegEx ^^ {
    found => Complex(found._1, found._2)
  }

  // The literal names of the built in types (and other types like the bit vectors) are valid identifiers.
  def cTypeName: Parser[Simple] = identifierRegEx ^^ {Simple(_)}

  def cTypePointer: Parser[Pointer] = cTypeCanStart <~ "*" ^^ {Pointer(_)}

  def cTypeIsSigned: Parser[Boolean] = "unsigned" ^^^ false | "signed" ^^^ true

  /**
   * Parse a C integer type and normalize it to be easier to process.
   * In other words make "signed short" and "short" the same list, here "short int".
   */
  def cTypeIntegerNumber: Parser[Intish] = (
    "sint" ^^^ List("int") | // C extension. Signed is default so remove it
      "uint" ^^^ List("unsigned", "int") | // C extension
      opt(cTypeIsSigned) ~ rep1("long" | "short" | "int" | "char") ^^ {found =>
        val normalized = found._2.lastOption match {
          case Some("short") => found._2 :+ "int"
          case Some("long") => found._2 :+ "int"
          case _ => found._2
        }
        if (found._1.getOrElse(true))
          normalized  // Signed is default so remove it
        else
          "unsigned" :: normalized  // signed is default so remove it
      } |
      cTypeIsSigned ^^ {sign =>
        if (sign)
          List("int")  // Signed is default so remove it
        else
          List("unsigned", "int")
      }
    ) ^^ {Intish(_)}

  def cTypeDecsToJava(cTypeDecs: ParseType): Requirement = {
    def needAsJava(name: String): Requirement = {
      new Requirement(name, classOf[DataType])
    }

    def normalizedIntSize(normalizedInt: List[String]) = normalizedInt match {
      case "char" :: Nil => 8
      case "short" :: "int" :: Nil => 16
      case "int" :: Nil => 16 // at least 16 bits. Assume 32 bits?
      case "long" :: "int" :: Nil => 32 // at least 32 bits
      case "long" :: "long" :: "int" :: Nil => 64 // at least 64 bits
      case _ => throw new Exception("Could not normalize. Is " +
        normalizedInt.reduce(_ + " " + _) + " a valid C integer?");
    }

    cTypeDecs match {
      case Pointer(Intish("char" :: Nil)) => needAsJava("string")
      case ArrayOf(Intish("char" :: Nil), dim) if (0 < dim) => needAsJava("string")

      case Intish(anInteger) => anInteger match { // signed is default for int. The compiler choose for char.
        case "unsigned" :: tail =>
          needAsJava("uint" + normalizedIntSize(tail))
        case signed =>
          needAsJava("int" + normalizedIntSize(signed))
      }

      case Simple("bool") => needAsJava("bool")
      case Simple("float") => needAsJava("float")
      case Simple("double") => needAsJava("double")
      case Simple(other) => needAsJava(other)

      case Complex("enum", name) => needAsJava("enum" + " " + name)
      case Complex("struct", name) => needAsJava("struct" + " " + name)
      case Complex("union", name) => needAsJava("union" + " " + name)

      case Pointer(targetType) => {
        val targetJ = cTypeDecsToJava(targetType)
        needAsJava(targetJ.getName + "*")
      }

      // TODO: Handle more of Array here
      // for now just pass through
      case ArrayOf(targetType, _) => {
        cTypeDecsToJava(targetType)
      }

      case _ => throw new Exception("Could not find a Java type for (alleged) C type " + cTypeDecs)
    }
  }

  /**
   * Are line breaks insignificant?
   * @param source text being parsed
   * @param offset position in the text
   * @return true if line break should be ignored
   */
  protected def isNewLineIgnored(source: CharSequence, offset: Int): Boolean

  /**
   * Is non line break white space insignificant?
   * @param source text being parsed
   * @param offset position in the text
   * @return true if non line break white space should be ignored
   */
  protected def isLineWSIgnored(source: CharSequence, offset: Int): Boolean

  /**
   * Is comments insignificant?
   * @param source text being parsed
   * @param offset position in the text
   * @return true if comments should be ignored
   */
  protected def areCommentsIgnored(source: CharSequence, offset: Int): Boolean

  private val space = (spaceBetweenWords + "+").r

  private val matchNothing = """$^""".r

  private val matchComment = regExOr(cStyleComment, cXXStyleComment).r

  private val matchSpaceComment: String =
    regExOr(regExOr(spaceBetweenWords, cStyleComment) + "+" + "(" + cXXStyleComment + ")?", cXXStyleComment)

  private val spaceOrComment = matchSpaceComment.r

  private val commentOrNewLine = (regExOr(cStyleComment, cXXStyleComment + "\n",  cXXStyleComment + "\r",
    "\n", "\r") + "+").r

  private val spaceCommentOrNewLine = ("(?m)" + regExOr(matchSpaceComment, "\\s+",
    cStyleStart + regExOr("\\s+", cStyleMiddle) + "+" + cStyleEnd) + "+").r

  override protected def handleWhiteSpace(source: CharSequence, offset: Int): Int = {
    if (0 == source.length())
      offset

    if (isNewLineIgnored(source, offset) && !areCommentsIgnored(source, offset))
      super.handleWhiteSpace(source, offset)
    else {
      val found =
        (if (!isNewLineIgnored(source, offset) && areCommentsIgnored(source, offset)) {
          if (isLineWSIgnored(source, offset))
            spaceOrComment
          else
            matchComment
        } else if (!isNewLineIgnored(source, offset) && !areCommentsIgnored(source, offset)) {
          if (isLineWSIgnored(source, offset))
            space
          else
            matchNothing
        } else {
          if (isLineWSIgnored(source, offset))
            spaceCommentOrNewLine
          else
            commentOrNewLine
        }).findPrefixMatchOf(source.subSequence(offset, source.length()))
      if (found.isEmpty)
        return offset
      else
        return offset + found.get.end
    }
  }
}

/**
 * A parser meant to be used with an [[org.freeciv.packetgen.parsing.ExtractorShared]].
 * Has a list of patterns that recognizes possible prefixes of wanted elements.
 * Can return zero (nothing found) one or more Dependency when parsing an element.
 */
abstract class ExtractableParser extends ParseShared {
  /**
   * Wrap a parser that returns a single Dependency object so it returns a List containing the single Dependency object.
   * Helper function for parser functions that return a Dependency but should be used in
   * [[org.freeciv.packetgen.parsing.ExtractableParser.exprConverted]].
   * @param orig a parser function that returns a single Dependency object.
   * @return a parser that use the original parser but return a List containing the Dependency object.
   */
  def oneAsMany(orig : ExtractableParser.this.Parser[Dependency]) : ExtractableParser.this.Parser[List[Dependency]] = {
    new Parser[List[Dependency]] {
      def apply(in : ExtractableParser.this.type#Input): ParseResult[List[Dependency]] = {
        val orig_result = orig(in)
        orig_result match {
          case Success(result, next) => new Success(List(result), next)
          case Failure(msg, input) => new Failure(msg, input)
          case Error(msg, input) => new Error(msg, input)
        }
      }
    }
  }

  /**
   * List of patterns that recognize locations the parser should try to extract from. When found an attempt to parse
   * from the found position will be made. If something is extracted it is added. If not it will be ignored.
   * @return a list of patterns that recognize possible extractable prefixes.
   */
  def startsOfExtractable : List[String]

  /**
   * A parser that can parse and convert any Dependency the ExtractableParser can extract. A single conversion may
   * result in multiple Dependency objects. Should return a list of zero objects if the parsing fails. (The parsing may
   * fail because the location is a false positive)
   * @return the parser that can parse and convert any supported Dependency.
   */
  def exprConverted: Parser[List[Dependency]]
}

/**
 * Extract elements without parsing, or even knowing how to parse, all of it.
 *
 * This is done in two steps. The first step uses a regular expression to identify positions that may contain
 * interesting elements. The second step tries to parse the interesting locations. A parsed location may result in zero,
 * one or more Dependency. If an interesting location is a false positive a list of zero Dependency should be returned.
 *
 * It is recommended to make a singleton object that extends ExtractorShared for each
 * [[org.freeciv.packetgen.parsing.ExtractableParser]] if the ExtractableParser is a singleton object.
 *
 * @param parser A parser that knows what prefix patterns indicates the beginning of an element it should try to parse.
 */
class ExtractorShared(protected val parser : ExtractableParser) {
  protected val lookFor = parser.startsOfExtractable.map("(" + _ + ")").reduce(_ + "|" + _).r

  /**
   * Search the supplied String for positions that may contain something interesting
   * @param lookIn the String to search
   * @return a list of possible locations that may be the start of a Dependency
   */
  def findPossibleStartPositions(lookIn: String): List[Int] =
    lookFor.findAllIn(lookIn).matchData.map(_.start).toList

  /**
   * Extract all Dependency in the supplied SourceFile
   * @param lookIn The SourceFile to extract from
   * @return a list of every Dependency generated while parsing the interesting locations of the document.
   */
  def extract(lookIn: SourceFile): List[Dependency] = {
    val positions = findPossibleStartPositions(lookIn.getContent)
    val lookInAsReader = new parser.PackratReader(new CharArrayReader(lookIn.getContent.toArray))

    positions.map(position => parser.parse(parser.exprConverted, lookInAsReader.drop(position)))
      .filter(!_.isEmpty).map(_.get).flatten
  }

  override def toString: String = "Extracts(" + parser.startsOfExtractable.map("(" + _ + ")").reduce(_ + "|" + _) + ")"
}
