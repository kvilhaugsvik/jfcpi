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
  def variableName = """[A-Za-z]\w*""".r

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
    variableName ^^ {IntExpression.variable(_)} |
    "(" ~> intExpr <~ ")" |
    intExpr

  val intExpr: Parser[IntExpression] = intExprLevelBinAdd
}
