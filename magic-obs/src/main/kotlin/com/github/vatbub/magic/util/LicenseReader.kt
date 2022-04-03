/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2019 - 2022 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.magic.util

import org.jdom2.input.SAXBuilder
import java.net.URL

object LicenseReader {
    fun readProjectLicense(classToResolveLicenseInfo: Class<*>) = classToResolveLicenseInfo
        .getResourceAsStream("LICENSE.txt")
        ?.reader()
        ?.readText()

    fun readDependencyLicenseInfo(classToResolveLicenseInfo: Class<*>): List<DependencyLicenseInfo> {
        val licensesXml = classToResolveLicenseInfo
            .getResourceAsStream("licenses.xml")
            ?.let { SAXBuilder().build(it) }
            ?: return listOf()

        val children = licensesXml
            .rootElement
            .getChild("dependencies")
            .getChildren("dependency")

        return children
            .map { dependencyNode ->
                DependencyLicenseInfo(
                    groupId = dependencyNode.getChildText("groupId"),
                    artifactId = dependencyNode.getChildText("artifactId"),
                    version = dependencyNode.getChildText("version"),
                    licenses = dependencyNode.getChild("licenses")
                        .getChildren("license")
                        .map { licenseNode ->
                            LicenseInfo(
                                licenseNode.getChildText("name"),
                                URL(licenseNode.getChildText("url")),
                                licenseNode.getChildText("file"),
                            )
                        }
                )
            }
    }
}

data class DependencyLicenseInfo(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val licenses: List<LicenseInfo>
)

data class LicenseInfo(val name: String, val url: URL, val fileName: String)
