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

import com.github.vatbub.magic.common.CommonPreferenceKeys.UIStyle
import com.github.vatbub.magic.common.preferenceFolder
import com.github.vatbub.magic.common.preferences
import javafx.application.Platform
import javafx.concurrent.Worker.State.FAILED
import javafx.concurrent.Worker.State.SUCCEEDED
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass.BACKGROUND
import org.apache.maven.shared.invoker.Invoker
import org.controlsfx.dialog.ProgressDialog
import java.io.StringWriter
import java.util.*
import java.util.concurrent.Executors


val strings = ResourceBundle.getBundle("com.github.vatbub.magic.bootstrap.bootstrap_strings")

fun main() {
    preferenceFolder = FilesAndFolders.appDir
    val jMetro = JMetro(preferences[UIStyle])

    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        showException(throwable)
    }

    Platform.startup {
        val progressDialog = ProgressDialog(UpdateAndLaunchTask)
        jMetro.scene = progressDialog.dialogPane.scene
        jMetro.scene.root.styleClass.add(BACKGROUND)
        val stage = progressDialog.dialogPane.scene.window as? Stage
        stage?.icons?.add(Image(Configuration::class.java.getResourceAsStream("icon.png")))
        progressDialog.title = strings["windowTitle"]
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
    val jMetro = JMetro(preferences[UIStyle])
    val alert = Alert(Alert.AlertType.ERROR)
    jMetro.parent = alert.dialogPane
    alert.dialogPane.styleClass.add(BACKGROUND)
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
