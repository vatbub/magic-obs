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
package com.github.vatbub.magic

import javafx.fxml.FXML
import javafx.scene.control.ColorPicker
import javafx.scene.control.TextField

class MainView {
    @FXML
    private lateinit var backgroundColorPicker: ColorPicker

    @FXML
    private lateinit var healthPointsBox: TextField

    private var healthPointUpdateInProgress = false

    @FXML
    fun initialize() {
        backgroundColorPicker.valueProperty().bindBidirectional(DataHolder.backgroundColorProperty)
        healthPointsBox.textProperty().addListener { _, oldValue, newValue ->
            val newIntValue = newValue.toIntOrNull()

            if (newIntValue == null) {
                DataHolder.healthPointsProperty.set(oldValue.toInt())
                return@addListener
            }

            if (newIntValue < 0) {
                DataHolder.healthPointsProperty.set(0)
                return@addListener
            }


            healthPointUpdateInProgress = true
            DataHolder.healthPointsProperty.set(newIntValue)
            healthPointUpdateInProgress = false
        }

        DataHolder.healthPointsProperty.addListener { _, _, newValue ->
            if (healthPointUpdateInProgress) return@addListener
            healthPointsBox.text = newValue.toInt().toString()
        }

        healthPointsBox.text = DataHolder.healthPointsProperty.value.toString()
    }

    @FXML
    fun healthPointsAddOnAction() {
        DataHolder.healthPointsProperty.value++
    }

    @FXML
    fun healthPointsSubtractOnAction() {
        DataHolder.healthPointsProperty.value--
    }

    fun close() {

    }
}
