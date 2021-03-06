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

/**
 * The configuration data for a Freeciv version
 * @param configName the name given to the configuration
 * @param configFile the path to the configuration file
 * @param packetHeader the kind of packet header
 * @param fieldTypeAliases field types to understand as another field type
 * @param enableDelta should the delta protocol be enabled?
 * @param enableDeltaBoolFolding should boolean variables be folded into the delta header?
 * @param inputSources the source files to extract the protocol from
 */
class VersionConfig(val configName: String,
                    val configFile: String,
                    val packetHeader: org.freeciv.packetgen.PacketHeaderKinds,
                    val fieldTypeAliases: Map[String, String],
                    val enableDelta: Boolean, val enableDeltaBoolFolding: Boolean,
                    val inputSources: Map[String, Seq[String]]) {
}

object VersionConfig {
  /**
   * Create a new VersionConfig from an xml file
   * @param from the xml file to parse
   * @return the configuration data for the Freeciv version specified in the provided xml file
   */
  def fromFile(from: File): VersionConfig = {
    val versionConfiguration = GeneratePackets.readSettings(from)

    /* Configuration name. */
    val configName = versionConfiguration.attribute("name").get.text

    /* Packet header behavior. */
    val packetHeader = PacketHeaderKinds.valueOf(versionConfiguration.attribute("packetHeaderKind").get.text)

    /* The delta protocol settings are currently hard coded in
     * common/generate_packets.py so they are a part of the version. */
    val enableDelta = versionConfiguration.attribute("enableDelta").get.text.toBoolean
    val enableDeltaBoolFolding = versionConfiguration.attribute("enableDeltaBoolFolding").get.text.toBoolean

    /* Field type aliases from the configuration */
    val fieldTypeAliases = (versionConfiguration \ "fieldTypeAlias").map(elem =>
      (elem \ "from").map(_.text).last -> (elem \ "to").map(_.text).last).toMap

    /* Freeciv source code files to find items in. */
    val inputSources = (versionConfiguration \ "inputSource").map(elem =>
      elem.attribute("parseAs").get.text -> (elem \ "file").map(_.text)).toMap

    new VersionConfig(configName, from.getPath, packetHeader, fieldTypeAliases, enableDelta, enableDeltaBoolFolding, inputSources)
  }

  /**
   * Create a new VersionConfig from the path to an xml file
   * @param from the path of the xml file to parse
   * @return the configuration data for the Freeciv version specified in the provided xml file
   */
  def fromFile(from: String): VersionConfig = fromFile(new File(from))
}
