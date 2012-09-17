/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

abstract class ParseType(val parts: List[String]) {
  override def toString() = parts.reduce(_ + " " + _)
}

case class Intish(normalizedType: List[String]) extends ParseType(normalizedType)
case class Simple(name: String) extends ParseType(name :: Nil)
case class Complex(kind: String, name: String) extends ParseType(kind :: name :: Nil)
case class Pointer(toType: ParseType) extends ParseType(toType.parts :+ "*")
