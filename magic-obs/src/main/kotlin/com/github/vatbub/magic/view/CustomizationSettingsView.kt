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
import com.github.vatbub.magic.appVersion
import com.github.vatbub.magic.buildTimestamp
import com.github.vatbub.magic.data.Ability
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.PreferenceKeys.AbilityKeys.SortMode
import com.github.vatbub.magic.data.preferences
import com.github.vatbub.magic.uiString
import com.github.vatbub.magic.util.get
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.StringConverter
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

                stage.title = App.resourceBundle["customizationView.windowTitle"]
                // stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
                stage.minWidth = root.minWidth(0.0) + 70
                stage.minHeight = root.minHeight(0.0) + 70

                stage.scene = scene

                stage.show()
                return this
            }
        }
    }

    @FXML
    private lateinit var backgroundColorPicker: ColorPicker

    @FXML
    private lateinit var healthPointsFontColorPicker: ColorPicker

    @FXML
    private lateinit var dropDownAbilitySortMode: ComboBox<Ability.SortMode>

    @FXML
    private lateinit var versionLabel: Label

    val stage: Stage = Stage(StageStyle.DECORATED)

    @FXML
    fun initialize() {
        backgroundColorPicker.valueProperty().bindBidirectional(DataHolder.backgroundColorProperty)
        healthPointsFontColorPicker.valueProperty().bindBidirectional(DataHolder.healthPointsFontColorProperty)

        dropDownAbilitySortMode.items = FXCollections.observableArrayList(*Ability.SortMode.values())
        dropDownAbilitySortMode.selectionModel.select(preferences[SortMode])
        dropDownAbilitySortMode.converter = object : StringConverter<Ability.SortMode>() {
            override fun toString(sortMode: Ability.SortMode): String =
                App.resourceBundle["ability.sortMode.$sortMode"] ?: sortMode.toString()

            override fun fromString(string: String): Ability.SortMode = throw NotImplementedError("Not supported")
        }
        dropDownAbilitySortMode.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            DataHolder.abilitySortModeProperty.value = newValue
        }

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
