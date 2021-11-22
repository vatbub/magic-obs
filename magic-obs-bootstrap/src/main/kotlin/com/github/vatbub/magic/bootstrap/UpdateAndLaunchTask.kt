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

import com.github.vatbub.magic.bootstrap.FilesAndFolders.appDir
import com.github.vatbub.magic.bootstrap.FilesAndFolders.mavenHome
import com.github.vatbub.magic.bootstrap.FilesAndFolders.pomFile
import com.github.vatbub.magic.bootstrap.FilesAndFolders.repositoryLocation
import javafx.concurrent.Task
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.Invoker
import java.io.File
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileSystemException
import java.nio.file.Files

object UpdateAndLaunchTask : Task<Invoker>() {
    override fun call(): Invoker {
        updateProgress(-1, 0)
        updateMessage(strings["preparing"])

        FilesAndFolders.createALlDirectories()

        Files.deleteIfExists(pomFile.toPath())
        pomFile.writeText(
            createDummyPom(
                Configuration.groupId,
                Configuration.artifactId,
                Configuration.initialVersion,
                Configuration.mainClass,
                Configuration.allowSnapshots,
                Configuration.releasesUrl,
                Configuration.snapshotUrl
            )
        )

        mavenHome.deleteAllChildrenIfPossible()
        MavenExtractor.extract(mavenHome)

        val invoker = DefaultInvoker()
        invoker.localRepositoryDirectory = repositoryLocation
        invoker.mavenHome = mavenHome
        invoker.setOutputHandler(MavenOutputHandler)

        updateMessage(strings["checking_for_updates"])

        val updateVersionsRequest = createRequest(listOf("versions:use-latest-versions"), true)

        val updateVersionsResult = invoker.execute(updateVersionsRequest)
        if (updateVersionsResult.exitCode != 0) {
            throw IllegalStateException(strings["update_check_failed"])
        }

        updateMessage(strings["downloading_updates"])

        val updateAppRequest = DefaultInvocationRequest()
        updateAppRequest.pomFile = pomFile
        updateAppRequest.goals = listOf("dependency:resolve")
        updateAppRequest.baseDirectory = appDir
        updateAppRequest.isUpdateSnapshots = true

        val updateAppResult = invoker.execute(updateAppRequest)
        if (updateAppResult.exitCode != 0) {
            throw IllegalStateException(strings["download_failed"])
        }

        return invoker
    }
}

fun createRequest(goals: List<String>, isUpdateSnapshots: Boolean) = DefaultInvocationRequest().also {
    it.pomFile = pomFile
    it.goals = goals
    it.baseDirectory = appDir
    it.isUpdateSnapshots = isUpdateSnapshots
}

private fun File.deleteAllChildrenIfPossible() = listFiles()!!
    .map { it.listAllChildrenForDeletion() }
    .flatten()
    .forEach {
        try {
            Files.delete(it.toPath())
        } catch (e: FileSystemException) {
            e.printStackTrace()
        } catch (e: DirectoryNotEmptyException) {
            e.printStackTrace()
        }
    }

private fun File.listAllChildrenForDeletion(): List<File> =
    if (isFile) listOf(this)
    else listFiles()!!
        .map { it.listAllChildrenForDeletion() }
        .flatten() + this
