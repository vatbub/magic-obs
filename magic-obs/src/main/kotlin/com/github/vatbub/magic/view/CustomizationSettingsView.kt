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
@file:Suppress("DuplicatedCode")

package com.github.vatbub.magic.view

import com.github.vatbub.magic.App
import com.github.vatbub.magic.appVersion
import com.github.vatbub.magic.buildTimestamp
import com.github.vatbub.magic.common.CommonPreferenceKeys.UIStyle
import com.github.vatbub.magic.common.preferences
import com.github.vatbub.magic.data.Ability
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.PreferenceKeys.AbilityKeys.SortMode
import com.github.vatbub.magic.uiString
import com.github.vatbub.magic.util.EnumStringConverter
import com.github.vatbub.magic.util.get
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import java.io.Closeable


class CustomizationSettingsView : Closeable {
    companion object {
        fun show(): CustomizationSettingsView {
            val fxmlLoader =
                FXMLLoader(
                    CustomizationSettingsView::class.java.getResource("CustomizationSettingsView.fxml"),
                    App.resourceBundle
                )
            val root = fxmlLoader.load<Parent>()
            with(fxmlLoader.getController<CustomizationSettingsView>()) {
                val scene = Scene(root)
                jMetro = JMetro(scene, preferences[UIStyle])
                jMetro.styleProperty().bind(DataHolder.uiStyle)

                stage.title = App.resourceBundle["customizationView.windowTitle"]
                stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
                stage.minWidth = root.minWidth(0.0) + 70
                stage.minHeight = root.minHeight(0.0) + 70

                stage.scene = scene

                stage.show()
                return this
            }
        }
    }

    private lateinit var jMetro: JMetro

    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var backgroundColorPicker: ColorPicker

    @FXML
    private lateinit var healthPointsFontColorPicker: ColorPicker

    @FXML
    private lateinit var dropDownAbilitySortMode: ComboBox<Ability.SortMode>

    @FXML
    private lateinit var versionLabel: Label

    @FXML
    private lateinit var dayNightControlsEnabledCheckbox: CheckBox

    @FXML
    private lateinit var uiStyleComboBox: ComboBox<Style>

    val stage: Stage = Stage(StageStyle.DECORATED)

    @FXML
    fun aboutOnAction() {
        AboutView.show(
            appName = "Magic OBS",
            appVersion = "$appVersion; ${buildTimestamp.uiString}",
            author = "Frederik Kammel",
            classToResolveLicenseInfo = App::class.java
        )
    }

    @FXML
    fun initialize() {
        rootPane.styleClass.add(JMetroStyleClass.BACKGROUND)
        backgroundColorPicker.valueProperty().bindBidirectional(DataHolder.backgroundColorProperty)
        healthPointsFontColorPicker.valueProperty().bindBidirectional(DataHolder.healthPointsFontColorProperty)

        dropDownAbilitySortMode.items = FXCollections.observableArrayList(*Ability.SortMode.values())
        dropDownAbilitySortMode.selectionModel.select(preferences[SortMode])
        dropDownAbilitySortMode.converter = EnumStringConverter()
        dropDownAbilitySortMode.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            DataHolder.abilitySortModeProperty.value = newValue
        }

        dayNightControlsEnabledCheckbox.selectedProperty().bindBidirectional(DataHolder.dayNightMechanicEnabled)

        uiStyleComboBox.items.addAll(Style.values())
        uiStyleComboBox.selectionModel.select(DataHolder.uiStyle.value)
        DataHolder.uiStyle.bind(uiStyleComboBox.selectionModel.selectedItemProperty())

        versionLabel.text =
            "${App.resourceBundle["customizationView.label.version"]}: $appVersion; ${buildTimestamp.uiString}"
    }

    @FXML
    fun healthPointsFontSpecChangeButtonOnAction() {
        FontSpecSelectionView.show(DataHolder.healthPointsFontSpecProperty.value) {
            DataHolder.healthPointsFontSpecProperty.value = it
        }
    }

    @FXML
    fun cardStatisticsFontSpecChangeButtonOnAction() {
        FontSpecSelectionView.show(DataHolder.cardStatisticsFontSpecProperty.value) {
            DataHolder.cardStatisticsFontSpecProperty.value = it
        }
    }

    @FXML
    fun healthPointsImageSpecChangeButtonOnAction() {
        ImageSpecSelectionView.show(DataHolder.healthPointsImageSpecProperty.value) {
            DataHolder.healthPointsImageSpecProperty.value = it
        }
    }

    override fun close() {
        stage.hide()
    }
}
