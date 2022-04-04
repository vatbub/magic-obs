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
package com.github.vatbub.magic.view

import com.github.vatbub.magic.common.CommonPreferenceKeys.UIStyle
import com.github.vatbub.magic.common.preferences
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.util.LicenseInfo
import com.github.vatbub.magic.util.LicenseReader
import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.get
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.TitledPane
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region.USE_COMPUTED_SIZE
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javafx.stage.Modality
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import java.io.File
import java.util.*

class AboutView {
    companion object {
        fun show(appName: String, appVersion: String, author: String, classToResolveLicenseInfo: Class<*>): AboutView {
            val fxmlLoader = FXMLLoader(AboutView::class.java.getResource("AboutView.fxml"), resourceBundle)
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<AboutView>()
            val stage = Stage()
            stage.initModality(Modality.APPLICATION_MODAL)
            controllerInstance.stage = stage
            controllerInstance.labelAppName.text = appName
            controllerInstance.labelAppVersion.text = appVersion
            controllerInstance.labelAuthor.text = author
            controllerInstance.loadLicenseInfo(classToResolveLicenseInfo)

            val scene = Scene(root)
            controllerInstance.jMetro = JMetro(scene, preferences[UIStyle])
            controllerInstance.jMetro.styleProperty().bind(DataHolder.uiStyle)
            stage.title = resourceBundle["windowTitle"]!!.format(appName)
            stage.icons.add(Image(AboutView::class.java.getResourceAsStream("icon.png")))

            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()

            return controllerInstance
        }

        val resourceBundle: ResourceBundle by lazy {
            ResourceBundle.getBundle("com.github.vatbub.magic.AboutViewStrings")
        }
    }

    lateinit var stage: Stage
        private set

    private lateinit var jMetro: JMetro

    @FXML
    private lateinit var labelAppName: Label

    @FXML
    private lateinit var labelAppVersion: Label

    @FXML
    private lateinit var labelAuthor: Label

    @FXML
    private lateinit var vboxThirdPartyLicenses: VBox

    @FXML
    private lateinit var textAreaProjectLicense: TextArea

    @FXML
    private lateinit var gridPane: GridPane

    @FXML
    private lateinit var scrollPane: ScrollPane

    @FXML
    fun initialize() {
        scrollPane.styleClass.add(JMetroStyleClass.BACKGROUND)
        gridPane.prefWidthProperty().bindAndMap(scrollPane.viewportBoundsProperty()) { it.width }
    }

    private fun loadLicenseInfo(classToResolveLicenseInfo: Class<*>) {
        loadProjectLicense(classToResolveLicenseInfo)
        loadThirdPartyLicenseInfo(classToResolveLicenseInfo)
    }

    private fun loadProjectLicense(classToResolveLicenseInfo: Class<*>) = Thread {
        val licenseText = LicenseReader.readProjectLicense(classToResolveLicenseInfo)

        if (licenseText.isNullOrBlank()) {
            Platform.runLater { textAreaProjectLicense.promptText = resourceBundle["noProjectLicenseFound"] }
            return@Thread
        }

        Platform.runLater { textAreaProjectLicense.text = licenseText }
    }.apply { name = "LoadProjectLicense" }.start()

    private fun loadThirdPartyLicenseInfo(classToResolveLicenseInfo: Class<*>) = Thread {
        val dependencyInfos = LicenseReader.readDependencyLicenseInfo(classToResolveLicenseInfo)

        if (dependencyInfos.isEmpty()) {
            Platform.runLater {
                vboxThirdPartyLicenses.children.clear()
                vboxThirdPartyLicenses.children.add(Label(resourceBundle["noThirdPartyLicensesFound"]))
            }
            return@Thread
        }

        val licenseFiles = dependencyInfos
            .flatMap { it.licenses }
            .associateWith {
                classToResolveLicenseInfo.getResourceAsStream("licenses/${it.fileName}")!!
                    .reader()
                    .readText()
            }

        Platform.runLater {
            val titledPanes = dependencyInfos.flatMap { dependencyInfo ->
                dependencyInfo.licenses.map { license ->
                    TitledPane(
                        "${dependencyInfo.groupId} - ${dependencyInfo.artifactId} version ${dependencyInfo.version}",
                        license.toView(licenseFiles[license]!!)
                    )
                }
            }

            titledPanes.forEach { pane ->
                pane.isExpanded = false
                pane.minWidth = USE_PREF_SIZE
                pane.minHeight = USE_PREF_SIZE
                pane.prefWidth = USE_COMPUTED_SIZE
                pane.prefHeight = USE_COMPUTED_SIZE
                pane.maxWidthProperty().bind(gridPane.prefWidthProperty())
            }

            vboxThirdPartyLicenses.children.clear()
            vboxThirdPartyLicenses.children.addAll(titledPanes)
        }
    }.apply { name = "LoadDependencyLicenses" }.start()

    private fun LicenseInfo.toView(fileContents: String) = when (File(fileName).extension.uppercase()) {
        "HTML" -> {
            WebView().also {
                it.engine.loadContent(fileContents)
                it.maxWidth = Double.POSITIVE_INFINITY
                it.minWidth = USE_PREF_SIZE
                it.maxWidthProperty()?.bind(gridPane.prefWidthProperty().subtract(10))
            }
        }
        else -> {
            TextArea(fileContents).also {
                it.isEditable = false
            }
        }
    }
}
