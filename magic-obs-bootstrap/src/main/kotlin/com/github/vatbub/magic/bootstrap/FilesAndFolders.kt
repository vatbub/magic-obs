/*-
 * #%L
 * magic-obs-bootstrap
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
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
package com.github.vatbub.magic.bootstrap

import java.io.File
import java.nio.file.Files

object FilesAndFolders {
    val appDir: File = File("maven-obs-launcher-data").absoluteFile
    val repositoryLocation: File = File(appDir, "dependencies").absoluteFile
    val mavenHome: File = File(appDir, "mavenHome").absoluteFile

    fun createALlDirectories() = listOf(appDir, repositoryLocation, mavenHome)
        .forEach { Files.createDirectories(it.toPath()) }

    val pomFile = File(appDir, "pom.xml")
    val mavenLogFile = File(appDir, "mavenLog.txt")
}
