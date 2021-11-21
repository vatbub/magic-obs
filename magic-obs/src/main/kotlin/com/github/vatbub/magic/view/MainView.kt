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
import com.github.vatbub.magic.data.Card
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.DayNightState
import com.github.vatbub.magic.data.PreferenceKeys.HealthPoints
import com.github.vatbub.magic.util.asNullable
import com.github.vatbub.magic.util.get
import javafx.beans.property.IntegerProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.ButtonType.NO
import javafx.scene.control.ButtonType.YES
import javafx.scene.layout.GridPane
import javafx.util.Callback


class MainView {
    @FXML
    private lateinit var rootPane: GridPane

    @FXML
    private lateinit var healthPointsBox: TextField

    @FXML
    private lateinit var cardsTableView: TableView<Card>

    @FXML
    private lateinit var counterColumn: TableColumn<Card, Int>

    @FXML
    private lateinit var defenseColumn: TableColumn<Card, Int>

    @FXML
    private lateinit var attackColumn: TableColumn<Card, Int>

    @FXML
    private lateinit var abilitiesColumn: TableColumn<Card, Card>

    @FXML
    private lateinit var buttonsColumn: TableColumn<Card, Card>

    @FXML
    private lateinit var comboBoxDayNightState: ComboBox<DayNightState>

    private var healthPointUpdateInProgress = false

    @FXML
    fun initialize() {
        cardsTableView.placeholder = Label(
            App.resourceBundle["mainView.tableView.cards.placeholder"]!!
                .format(App.resourceBundle["mainView.button.addCard"])
        )

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

        DataHolder.abilitySortModeProperty.addListener { _, _, _ -> refreshCardTableFactories() }

        cardsTableView.items = DataHolder.cardList
        refreshCardTableFactories()

        comboBoxDayNightState.items = FXCollections.observableArrayList(*DayNightState.values())
        comboBoxDayNightState.selectionModel.select(DataHolder.dayNightState.value)
        DataHolder.dayNightState.addListener { _, _, newValue ->
            comboBoxDayNightState.selectionModel.select(newValue)
        }
        comboBoxDayNightState.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            DataHolder.dayNightState.value = newValue
        }
    }

    private fun refreshCardTableFactories() {
        attackColumn.setIntegerColumnFactories { attackProperty }
        defenseColumn.setIntegerColumnFactories { defenseProperty }
        counterColumn.setIntegerColumnFactories { counterProperty }
        abilitiesColumn.cellFactory = Callback { CheckboxDropDownCell() }
        buttonsColumn.cellFactory = Callback { CardButtonCell() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun TableColumn<Card, Int>.setIntegerColumnFactories(objectProperty: Card.() -> IntegerProperty) {
        cellValueFactory = Callback { objectProperty(it.value) as ObservableValue<Int> }
        cellFactory = Callback {
            object : ObjectIntegerEditingCell<Card>() {
                override fun updateItemPropertyValue(item: Card, newValue: Int) {
                    objectProperty(item).value = newValue
                }
            }
        }
    }

    @FXML
    fun healthPointsAddOnAction() {
        DataHolder.healthPointsProperty.value++
    }

    @FXML
    fun healthPointsSubtractOnAction() {
        DataHolder.healthPointsProperty.value--
    }

    @FXML
    fun addCardButtonOnAction() {
        DataHolder.cardList.add(Card())
    }

    @FXML
    fun customizeAppearanceButtonOnAction(): Unit = with(App.instance) {
        val existingViews = auxiliaryViews.mapNotNull { it as? CustomizationSettingsView }
        if (existingViews.isEmpty())
            auxiliaryViews.add(CustomizationSettingsView.show())
        else
            existingViews.forEach {
                it.stage.show()
                it.stage.isIconified = false
                it.stage.requestFocus()
            }
    }

    @FXML
    fun resetGameButtonOnAction() {
        val alertResult = Alert(CONFIRMATION).apply {
            contentText = App.resourceBundle["mainView.confirmation.reset.content"]!!.format(HealthPoints.defaultValue)
            buttonTypes.clear()
            buttonTypes.addAll(YES, NO)
        }.showAndWait().asNullable() ?: return

        if (alertResult != YES) return
        DataHolder.resetGame()
    }

    @FXML
    fun buttonResetWindowsOnAction() {
        App.instance.resetAuxiliaryViews()
    }

    fun close() {

    }
}
