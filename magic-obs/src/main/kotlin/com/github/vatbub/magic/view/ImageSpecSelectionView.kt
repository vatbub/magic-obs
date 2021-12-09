/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2016 - 2021 Frederik Kammel
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

import com.github.vatbub.magic.App
import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.get
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.StringConverter
import java.io.File
import kotlin.properties.Delegates


class ImageSpecSelectionView {
    companion object {
        fun show(initialSpec: ImageSpec?, onFontSelectedCallback: (ImageSpec) -> Unit): ImageSpecSelectionView {
            val fxmlLoader =
                FXMLLoader(
                    ImageSpecSelectionView::class.java.getResource("ImageSpecSelectionView.fxml"),
                    App.resourceBundle
                )
            val root = fxmlLoader.load<Parent>()
            with(fxmlLoader.getController<ImageSpecSelectionView>()) {
                if (initialSpec != null)
                    this.initialSpec = initialSpec
                this.onFontSelectedCallback = onFontSelectedCallback

                val scene = Scene(root)
                stage.minWidth = root.minWidth(0.0) + 70
                stage.minHeight = root.minHeight(0.0) + 70

                stage.scene = scene
                stage.icons.add(Image(FontSpecSelectionView::class.java.getResourceAsStream("icon.png")))

                stage.show()
                return this
            }
        }
    }

    @FXML
    private lateinit var builtInImageSpecDropDown: ComboBox<BuiltInImageSpecs>

    @FXML
    private lateinit var tabPane: TabPane

    @FXML
    private lateinit var builtInTab: Tab

    @FXML
    private lateinit var customTab: Tab

    @FXML
    private lateinit var customFileLocationField: TextField


    val stage: Stage = Stage(StageStyle.UNIFIED).apply {
        initModality(Modality.APPLICATION_MODAL)
        title = App.resourceBundle["imageSpecSelectionView.title"]
        // stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
    }

    private var initialSpec: ImageSpec by Delegates.observable(BuiltInImageSpecs.GreenRing.imageSpec) { _, _, newValue ->
        when (newValue) {
            is ImageSpec.BuiltIn -> {
                tabPane.selectionModel.select(builtInTab)
                builtInImageSpecDropDown.selectionModel.select(BuiltInImageSpecs.forSpec(newValue))
            }
            is ImageSpec.Custom -> {
                tabPane.selectionModel.select(customTab)
                customFileProperty.value = newValue.file
            }
        }
    }

    private var customFileProperty = SimpleObjectProperty<File>()

    private lateinit var onFontSelectedCallback: (ImageSpec) -> Unit

    @FXML
    fun initialize() {
        builtInImageSpecDropDown.items = FXCollections.observableArrayList(*BuiltInImageSpecs.values())

        builtInImageSpecDropDown.converter = object : StringConverter<BuiltInImageSpecs>() {
            override fun toString(imageSpec: BuiltInImageSpecs): String = imageSpec.humanReadableName

            override fun fromString(string: String): BuiltInImageSpecs = throw NotImplementedError()
        }

        customFileLocationField.textProperty().bindAndMap(customFileProperty) { it?.name ?: "" }

        listOf(builtInImageSpecDropDown)
            .forEach { it.selectionModel.select(0) }
    }

    @FXML
    fun browseButtonOnAction() = with(FileChooser()) {
        title = App.resourceBundle["imageSpecSelectionView.fileChooser.title"]
        customFileProperty.value?.let { currentFile ->
            initialDirectory = currentFile.parentFile
            initialFileName = currentFile.name
        }

        this.extensionFilters.add(
            FileChooser.ExtensionFilter(
                App.resourceBundle["imageSpecSelectionView.fileChooser.supportedFileTypes"],
                "*.bmp", "*.gif", "*.jpg", "*.jpeg", "*.png"
            )
        )

        customFileProperty.value = showOpenDialog(stage)
    }

    private fun generateImageSpecFromView(): ImageSpec? = when (tabPane.selectionModel.selectedItem) {
        builtInTab -> builtInImageSpecDropDown.selectionModel.selectedItem.imageSpec
        customTab -> customFileProperty.value?.let { ImageSpec.Custom(it) }
        else -> throw IllegalStateException("Illegal tab selected")
    }

    @FXML
    fun okButtonOnAction() {
        val currentSpec = generateImageSpecFromView()
        if (currentSpec == null) {
            Alert(
                Alert.AlertType.WARNING,
                App.resourceBundle["imageSpecSelectionView.alert.noFileSelected.contentText"],
                ButtonType.OK
            ).show()
            return
        }

        onFontSelectedCallback(currentSpec)
        stage.hide()
    }

    @FXML
    fun cancelButtonOnAction() {
        stage.hide()
    }
}
