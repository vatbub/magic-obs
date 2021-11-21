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

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import java.io.File
import java.net.URL
import java.nio.file.Files
import kotlin.system.exitProcess


fun main() {
    val groupId = "com.github.vatbub"
    val artifactId = "magic-obs"
    val initialVersion = "1.0-SNAPSHOT"
    val mainClass = "com.github.vatbub.magic.MainKt"

    val snapshotUrl = URL("https://oss.sonatype.org/content/repositories/snapshots/")
    val releasesUrl = URL("https://repo1.maven.org/maven2/")

    val appDir = File("maven-obs-launcher-data").absoluteFile
    Files.createDirectories(appDir.toPath())

    val repositoryLocation = File(appDir, "dependencies")
    Files.createDirectories(repositoryLocation.toPath())

    val pomFile = File(appDir, "pom.xml")
    Files.deleteIfExists(pomFile.toPath())
    pomFile.writeText(createDummyPom(groupId, artifactId, initialVersion, mainClass, releasesUrl, snapshotUrl))

    val mavenHome = File(appDir, "mavenHome").absoluteFile
    Files.createDirectories(mavenHome.toPath())
    mavenHome.deleteAllChildren()
    MavenExtractor.extract(mavenHome)

    val invoker = DefaultInvoker()
    invoker.localRepositoryDirectory = repositoryLocation
    invoker.mavenHome = mavenHome

    val updateVersionsRequest = DefaultInvocationRequest()
    updateVersionsRequest.pomFile = pomFile
    // request.goals = listOf("dependency:copy-dependencies")
    updateVersionsRequest.goals = listOf("versions:use-latest-versions")
    updateVersionsRequest.baseDirectory = appDir
    updateVersionsRequest.isUpdateSnapshots = true

    val updateVersionsResult = invoker.execute(updateVersionsRequest)
    if (updateVersionsResult.exitCode != 0) {
        exitProcess(updateVersionsResult.exitCode)
    }

    val updateAppRequest = DefaultInvocationRequest()
    updateAppRequest.pomFile = pomFile
    updateAppRequest.goals = listOf("dependency:resolve")
    updateAppRequest.baseDirectory = appDir
    updateAppRequest.isUpdateSnapshots = true

    val updateAppResult = invoker.execute(updateAppRequest)
    if (updateAppResult.exitCode != 0) {
        exitProcess(updateAppResult.exitCode)
    }

    val startAppRequest = DefaultInvocationRequest()
    startAppRequest.pomFile = pomFile
    startAppRequest.goals = listOf("exec:java")
    startAppRequest.baseDirectory = appDir
    startAppRequest.isUpdateSnapshots = false

    val startAppResult = invoker.execute(startAppRequest)
    if (startAppResult.exitCode != 0) {
        exitProcess(startAppResult.exitCode)
    }
}

private fun File.deleteAllChildren() = listFiles()!!
    .map { it.listAllChildrenForDeletion() }
    .flatten()
    .forEach { Files.delete(it.toPath()) }

private fun File.listAllChildrenForDeletion(): List<File> =
    if (isFile) listOf(this)
    else listFiles()!!
        .map { it.listAllChildrenForDeletion() }
        .flatten() + this
