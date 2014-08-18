/*
 * Copyright (c) 2011-2014. Sveinung Kvilhaugsvik
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

import java.io.File

class VersionConfig(val configName: String,
                    val packetHeader: org.freeciv.packetgen.PacketHeaderKinds,
                    val enableDelta: Boolean, val enableDeltaBoolFolding: Boolean,
                    val inputSources: Map[String, Seq[String]]) {
}

object VersionConfig {
  def fromFile(from: File): VersionConfig = {
    val versionConfiguration = GeneratePackets.readSettings(from)

    val configName = versionConfiguration.attribute("name").get.text

    val packetHeader = PacketHeaderKinds.valueOf(versionConfiguration.attribute("packetHeaderKind").get.text)

    val enableDelta = versionConfiguration.attribute("enableDelta").get.text.toBoolean
    val enableDeltaBoolFolding = versionConfiguration.attribute("enableDeltaBoolFolding").get.text.toBoolean

    val inputSources = (versionConfiguration \ "inputSource").map(elem =>
      elem.attribute("parseAs").get.text -> (elem \ "file").map(_.text)).toMap

    new VersionConfig(configName, packetHeader, enableDelta, enableDeltaBoolFolding, inputSources)
  }

  def fromFile(from: String): VersionConfig = fromFile(new File(from))
}
