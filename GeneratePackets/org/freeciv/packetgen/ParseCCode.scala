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

class ParseCCode(storage: PacketsStore, lookFor: List[String]) extends ParseShared(storage) {
  def enumDefname: Parser[Any] =
    lookFor.foldRight[Parser[Any]](failure("Nothing found"))((prefer: String, ifNot: Parser[Any]) => (prefer | ifNot))

  @inline private def se(kind: String) = "#define" ~ regex(("SPECENUM_" + kind).r)

  def specEnum(kind: String) = se(kind) ~ regex("[a-zA-Z]\\w+".r)
  def specEnumOrName(kind: String) = se(kind + "NAME") ~ regex(""""[^\n\r\"]*?"""".r) | specEnum(kind)

  def specEnumDef = se("NAME") ~ enumDefname ~
    rep(
      specEnumOrName("VALUE\\d+") |
        specEnumOrName("ZERO") |
        specEnumOrName("COUNT") |
        specEnum("INVALID") |
        CComment |
        se("BITWISE")
    ) ~
    "#include" ~ "\"specenum_gen.h\""

  def enumValue = regex("""[0-9]+""".r)

  def cEnum = opt(CComment) ~> regex("""[A-Za-z]\w*""".r) ~ opt("=" ~> enumValue) <~ opt(CComment)

  def cEnumDef = "enum" ~ enumDefname ~ "{" ~ repsep(cEnum, ",") ~ "}"

  def expr = cEnumDef |
    specEnumDef |
    CComment
}
