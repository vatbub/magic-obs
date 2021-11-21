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
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipInputStream


object MavenExtractor {
    fun extract(destination: File) {
        javaClass.getResourceAsStream("maven.zip")!!.use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var zipEntry = zipInputStream.nextEntry

                while (zipEntry != null) {
                    val fileName: String = zipEntry.name
                    val newFile = File(destination, fileName)
                    Files.createDirectories(newFile.parentFile.toPath())

                    if (zipEntry.isDirectory) {
                        Files.createDirectories(newFile.toPath())
                    } else {
                        FileOutputStream(newFile).use { fileOutputStream ->
                            fileOutputStream.write(zipInputStream.readBytes())
                        }
                    }
                    zipInputStream.closeEntry()
                    zipEntry = zipInputStream.nextEntry
                }
            }
        }
    }
}
