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
import org.freeciv.packetgen.enteties.supporting.IntExpression
import org.freeciv.packetgen.dependency.{IDependency, Requirement}
import util.parsing.input.CharArrayReader

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
  protected val cStyleComment = "(" + cStyleManyStars + "|" + cStyleStart + cStyleMiddle + cStyleEnd + ")"

  protected val spaceBetweenWords = """[\t ]"""

  protected def regExOr(arg: String*): String = "(" + arg.reduce(_ + "|" + _) + ")"

  def sInteger = """[+|-]*[0-9]+""".r

  def identifier = """[A-Za-z]\w*"""

  val identifierRegEx = identifier.r

  def quotedString = """\"[^"]*?\""""

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
    """[0-9]+""".r ^^ {IntExpression.integer(_)} |
      identifierRegEx ^^ {IntExpression.variable(_)} |
      "(" ~> intExpr <~ ")" |
      intExpr

  val intExpr: Parser[IntExpression] = intExprLevelBinAdd

  // The literal names of the built in types are valid identifiers.
  // If a need to be more strict arises only accept identifiers in struct/union/enum and use built in type names
  // TODO: if needed: support defining an anonymous enum, struct or union
  def cType: Parser[List[String]] =
    ("struct" | "union" | "enum") ~ cType ^^ {found => found._1 :: found._2} |
      ("unsigned" | "signed") ~ cType ^^ {found => found._1 :: found._2} |
      identifierRegEx ^^ {List(_)}

  /**
   * Normalize a C integer type to easier to process.
   * In other words make "signed short" and "short" the same list, here "short int".
   * @param tokens a list of tokens that represent a C int type
   * @return the tokens in a standard form
   */
  def normalizeCIntDeclaration(tokens: List[String]): List[String] = {
    val processed = expandCIntDeclaration(tokens);
    if ("signed".equals(processed.head))
      processed.tail // signed is default so remove it
    else
      processed
  }

  /**
   * Almost normalize a C integer type. "signed" isn't normalized. Only use this if you fix "signed" your self.
   * @param tokens a list of tokens that represent a C int type
   * @return the tokens in a standard form
   */
  protected def expandCIntDeclaration(tokens: List[String]): List[String] = tokens match {
    case Nil => Nil
    case "uint" :: (tail: List[String]) => "unsigned" :: "int" :: expandCIntDeclaration(tail) // a C extension
    case "sint" :: (tail: List[String]) => "int" :: expandCIntDeclaration(tail) // a C extension. Signed is default
    case (lastToken: String) :: Nil => expandLastInt(lastToken) // last token isn't sin or uint so expand if needed
    case token :: (tail: List[String]) => token :: expandCIntDeclaration(tail)
  }

  @inline private def expandLastInt(lastToken: String): List[String] = lastToken match {
    case "short" => "short" :: "int" :: Nil
    case "long" => "long" :: "int" :: Nil
    case "unsigned" => "unsigned" :: "int" :: Nil
    case "signed" => "int" :: Nil // signed is default for int
    case _ => lastToken :: Nil
  }

  def cTypeDecsToJava(cTypeDecs: List[String]): (String, java.util.Set[Requirement]) = {
    def needAsJava(name: String): java.util.Set[Requirement] = {
      val out = new java.util.HashSet[Requirement]();
      out.add(new Requirement(name, Requirement.Kind.AS_JAVA_DATATYPE));
      out
    }

    val nativeJava = java.util.Collections.emptySet[Requirement]()

    def pickJavaInt(sizeInBytes: Int, isSigned: Boolean): String = {
      // TODO: isSigned and bits can be used to check range

      // Java don't have unsigned so something bigger is needed...
      val realSize: Int = if (isSigned) sizeInBytes else sizeInBytes + 1

      // Java really really likes int so don't use shorter values
      if (realSize <= 32)
        "Integer"
      else if (realSize <= 64)
        "Long"
      else
        throw new UnsupportedOperationException("No Java integer supports " + realSize + " bits." +
          " BigInteger may be used when users of this method can handle making a constructor for it.")
    }

    def normalizedIntSize(normalizedInt: List[String]) = normalizedInt match {
      case "char" :: Nil => 8
      case "short" :: "int" :: Nil => 16
      case "int" :: Nil => 16 // at least 16 bits. Assume 32 bits
      case "long" :: "int" :: Nil => 31 // at least 32 bits
      case "long" :: "long" :: "int" :: Nil => 64 // at least 64 bits
      case _ => throw new Exception("Could not normalize. Is " +
        normalizedInt.reduce(_ + " " + _) + " a valid C integer?");
    }

    object Intish {
      def unapply(term: List[String]): Option[List[String]] =
        if (List("unsigned", "signed", "long", "short", "int", "char", "sint", "uint").contains(term.head))
          Some(normalizeCIntDeclaration(term))
        else
          None
    }

    cTypeDecs match {
      case Intish(anInteger) => anInteger match { // signed is default for int. The compiler choose for char.
        case "unsigned" :: tail =>
          (pickJavaInt(normalizedIntSize(tail), false), nativeJava)
        case "signed" :: tail =>
          (pickJavaInt(normalizedIntSize(tail), true), nativeJava)
        case signed =>
          (pickJavaInt(normalizedIntSize(signed), true), nativeJava)
      }

      case "bool" :: Nil => ("Boolean", nativeJava)
      case "float" :: Nil => ("Float", nativeJava)
      case "double" :: Nil => ("Double", nativeJava)
      case other :: Nil => (other, needAsJava(other))

      case "enum" :: name :: Nil => (name, needAsJava("enum" + " " + name))
      case "struct" :: name :: Nil => (name, needAsJava("struct" + " " + name))
      case "union" :: name :: Nil => (name, needAsJava("union" + " " + name))

      case _ => throw new Exception("Could not find a Java type for (alleged) C type " + cTypeDecs.reduce(_ + " " + _))
    }
  }

  protected def isNewLineIgnored(source: CharSequence, offset: Int): Boolean

  protected def isLineWSIgnored(source: CharSequence, offset: Int): Boolean

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

abstract class ExtractableParser extends ParseShared {
  def startsOfExtractable : List[String]
  def exprConverted: Parser[IDependency]
}

abstract class ExtractorShared(protected val parser : ExtractableParser) {
  protected val lookFor = parser.startsOfExtractable.map("(" + _ + ")").reduce(_ + "|" + _).r

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
