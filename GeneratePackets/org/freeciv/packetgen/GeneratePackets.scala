package org.freeciv.packetgen

import util.parsing.combinator._

class ParsePacketsDef(storage: PacketsStore) extends RegexParsers {
  def fieldType = regex("""[A-Z](\w|_)*""".r)
  def fieldTypeDef = fieldType | regex("""\w*\((\w|\s)*\)""".r)

  def fieldTypeAssign: Parser[Any] = "type" ~ fieldType ~ "=" ~ fieldTypeDef ^^ {
    case theType~alias~is~aliased => storage.registerTypeAlias(alias, aliased)
  }

  def expr: Parser[Any] = fieldTypeAssign

  def parsePacketsDef(input: String) = parseAll(rep(expr), input)
}
