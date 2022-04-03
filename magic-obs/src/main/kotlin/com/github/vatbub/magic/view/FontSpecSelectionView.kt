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
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.PreferenceKeys
import com.github.vatbub.magic.data.preferences
import com.github.vatbub.magic.util.get
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.StringConverter
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import kotlin.properties.Delegates


class FontSpecSelectionView {
    companion object {
        fun show(initialSpec: FontSpec?, onFontSelectedCallback: (FontSpec) -> Unit): FontSpecSelectionView {
            val stage = Stage(StageStyle.UNIFIED)
            stage.initModality(Modality.APPLICATION_MODAL)

            val fxmlLoader =
                FXMLLoader(
                    FontSpecSelectionView::class.java.getResource("FontSpecSelectionView.fxml"),
                    App.resourceBundle
                )
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<FontSpecSelectionView>()
            controllerInstance.stage = stage
            if (initialSpec != null)
                controllerInstance.initialSpec = initialSpec
            controllerInstance.onFontSelectedCallback = onFontSelectedCallback

            val scene = Scene(root)
            controllerInstance.jMetro = JMetro(scene, preferences[PreferenceKeys.UIStyle])
            controllerInstance.jMetro.styleProperty().bind(DataHolder.uiStyle)

            stage.title = App.resourceBundle["fontSpecSelectionView.title"]
            stage.icons.add(Image(FontSpecSelectionView::class.java.getResourceAsStream("icon.png")))
            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()
            return controllerInstance
        }
    }

    private lateinit var jMetro: JMetro

    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var systemWeightDropDown: ComboBox<FontWeight>

    @FXML
    private lateinit var builtInFontSpecDropDown: ComboBox<BuiltInFontSpecs>

    @FXML
    private lateinit var systemFamilyDropDown: ComboBox<String>

    @FXML
    private lateinit var systemPostureDropDown: ComboBox<FontPosture>

    @FXML
    private lateinit var tabPane: TabPane

    @FXML
    private lateinit var builtInTab: Tab

    @FXML
    private lateinit var systemTab: Tab

    lateinit var stage: Stage
        private set
    private var initialSpec: FontSpec by Delegates.observable(BuiltInFontSpecs.ArchitectsDaughterRegular.fontSpec) { _, _, newValue ->
        when (newValue) {
            is FontSpec.BuiltIn -> {
                tabPane.selectionModel.select(builtInTab)
                builtInFontSpecDropDown.selectionModel.select(BuiltInFontSpecs.forSpec(newValue))
            }
            is FontSpec.System -> {
                tabPane.selectionModel.select(systemTab)
                systemFamilyDropDown.selectionModel.select(newValue.family)
                systemPostureDropDown.selectionModel.select(newValue.posture)
                systemWeightDropDown.selectionModel.select(newValue.weight)
            }
        }
    }

    private lateinit var onFontSelectedCallback: (FontSpec) -> Unit

    @FXML
    fun initialize() {
        rootPane.styleClass.add(JMetroStyleClass.BACKGROUND)
        systemWeightDropDown.items = FXCollections.observableArrayList(*FontWeight.values())
        systemPostureDropDown.items = FXCollections.observableArrayList(*FontPosture.values())
        systemFamilyDropDown.items = FXCollections.observableArrayList(FontSpec.systemFonts)
        builtInFontSpecDropDown.items = FXCollections.observableArrayList(*BuiltInFontSpecs.values())

        builtInFontSpecDropDown.converter = object : StringConverter<BuiltInFontSpecs>() {
            override fun toString(fontSpec: BuiltInFontSpecs): String = fontSpec.humanReadableName

            override fun fromString(string: String): BuiltInFontSpecs = throw NotImplementedError()
        }

        listOf(systemWeightDropDown, systemPostureDropDown, systemFamilyDropDown, builtInFontSpecDropDown)
            .forEach { it.selectionModel.select(0) }
    }

    private fun generateFontSpecFromView(): FontSpec = when (tabPane.selectionModel.selectedItem) {
        builtInTab -> builtInFontSpecDropDown.selectionModel.selectedItem.fontSpec
        systemTab -> FontSpec.System(
            family = systemFamilyDropDown.selectionModel.selectedItem,
            weight = systemWeightDropDown.selectionModel.selectedItem,
            posture = systemPostureDropDown.selectionModel.selectedItem
        )
        else -> throw IllegalStateException("Illegal tab selected")
    }

    @FXML
    fun okButtonOnAction() {
        onFontSelectedCallback(generateFontSpecFromView())
        stage.hide()
    }

    @FXML
    fun cancelButtonOnAction() {
        stage.hide()
    }
}
