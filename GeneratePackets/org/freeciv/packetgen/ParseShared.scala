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

package org.freeciv.packetgen

import util.parsing.combinator._

abstract class ParseShared extends RegexParsers with PackratParsers {
  def expr: Parser[Any]

  def exprs: Parser[Any] = rep(expr)

  def CComment: Parser[String] = ("""/\*+""".r ~> rep("""([^*\n\r]|\*+[^/*])+""".r) <~ """\*+/""".r) ^^ {_.reduce(_+_)} |
    regex("""/\*+\*/""".r) ^^^ null |
    "//" ~> regex("""[^\n\r]*""".r)

  protected val cXXStyleComment = """//[^\n\r]*"""
  protected val cStyleComment = "(" + """/\*+\*/""" + "|" + """/\*+""" + """([^*\n\r]|\*+[^/*])+""" + """\*+/""" + ")"
  protected val spaceBetweenWords = """[\t ]"""

  protected def regExOr(arg: String*): String = "(" + arg.reduce(_ + "|" + _) + ")"

  def sInteger = """[+|-]*[0-9]+""".r
  def identifier = """[A-Za-z]\w*"""
  val identifierRegEx = identifier.r

  private def binOpLev(operators: Parser[String]): PackratParser[(IntExpression,  IntExpression) => IntExpression] =
    operators ^^ {operator => (lhs: IntExpression, rhs: IntExpression) => IntExpression.binary(operator, lhs, rhs)}

  private val intExprLevelBinAdd: PackratParser[IntExpression] = chainl1(intExprLevelBinMul, binOpLev(("+"|"-")))

  private val intExprLevelBinMul: PackratParser[IntExpression] = chainl1(intExprLevelUnary, binOpLev(("*"|"/"|"%")))

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
  def cType: Parser[List[String]] =
    ("struct"|"union"|"enum") ~ cType ^^ {found => found._1 :: found._2} |
    ("unsigned"|"signed") ~ cType ^^ {found => found._1 :: found._2} |
    identifierRegEx ^^ {List(_)}

  protected def isNewLineIgnored(source: CharSequence, offset: Int): Boolean

  private val spaceOrComment =
    regExOr(regExOr(spaceBetweenWords, cStyleComment)+"+" + "("+cXXStyleComment+")?", cXXStyleComment).r
  override protected def handleWhiteSpace(source: CharSequence, offset: Int): Int = {
    if (0 == source.length())
      offset

    if (isNewLineIgnored(source, offset))
      super.handleWhiteSpace(source, offset)
    else {
      val found = spaceOrComment
        .findPrefixMatchOf(source.subSequence(offset, source.length()))
      if (found.isEmpty)
        return offset
      else
        return offset + found.get.end
    }
  }
}
