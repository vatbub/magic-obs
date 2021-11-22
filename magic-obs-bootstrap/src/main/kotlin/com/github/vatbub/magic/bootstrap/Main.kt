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

import javafx.application.Platform
import javafx.concurrent.Worker.State.FAILED
import javafx.concurrent.Worker.State.SUCCEEDED
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import org.apache.maven.shared.invoker.Invoker
import org.controlsfx.dialog.ProgressDialog
import java.io.StringWriter
import java.util.*
import java.util.concurrent.Executors


val strings = ResourceBundle.getBundle("com.github.vatbub.magic.bootstrap.bootstrap_strings")

fun main(vararg args: String) {
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        showException(throwable)
    }

    Configuration.allowSnapshots = args.contains("--enableSnapshots")

    Platform.startup {
        val progressDialog = ProgressDialog(UpdateAndLaunchTask)
        progressDialog.show()

        UpdateAndLaunchTask.stateProperty().addListener { _, _, newValue ->
            progressDialog.hide()
            if (newValue == FAILED) showException(UpdateAndLaunchTask.exception)
            else if (newValue == SUCCEEDED) launchApp(UpdateAndLaunchTask.value)
        }

        val executor = Executors.newSingleThreadExecutor()
        executor.submit(UpdateAndLaunchTask)
        executor.shutdown()
    }
}

private fun launchApp(invoker: Invoker) {
    val startAppRequest = createRequest(listOf("exec:java"), false)

    val startAppResult = invoker.execute(startAppRequest)
    if (startAppResult.exitCode != 0) {
        showException(IllegalStateException(strings["launch_failed"]))
    }
    MavenOutputHandler.close()
}

fun showException(exception: Throwable) {
    val alert = Alert(Alert.AlertType.ERROR)
    alert.title = strings["exception_dialog.title"]
    alert.headerText = strings["exception_dialog.header"]

    val stringWriter = StringWriter()
    stringWriter.write(exception.stackTraceToString())

    val label = Label("The stacktrace was:")
    val textArea = TextArea(stringWriter.toString())
    with(textArea) {
        isWrapText = false
        isEditable = false
        maxWidth = Double.MAX_VALUE
        maxHeight = Double.MAX_VALUE
    }
    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val expandableContent = GridPane()
    with(expandableContent) {
        maxWidth = Double.MAX_VALUE
        add(label, 0, 0)
        add(textArea, 0, 1)
    }

    alert.dialogPane.expandableContent = expandableContent

    alert.contentText = exception.message

    alert.show()
}
