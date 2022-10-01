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
import com.github.vatbub.magic.data.CardDatabase
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.DayNightState
import com.github.vatbub.magic.data.PreferenceKeys.HealthPoints
import com.github.vatbub.magic.util.EnumStringConverter
import com.github.vatbub.magic.util.asNullable
import com.github.vatbub.magic.util.get
import com.github.vatbub.magic.util.invertIfDarkMode
import com.github.vatbub.magic.util.map
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.Button
import javafx.scene.control.ButtonType.NO
import javafx.scene.control.ButtonType.YES
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.util.Callback
import jfxtras.styles.jmetro.JMetroStyleClass.ALTERNATING_ROW_COLORS
import jfxtras.styles.jmetro.JMetroStyleClass.BACKGROUND
import jfxtras.styles.jmetro.JMetroStyleClass.TABLE_GRID_LINES
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


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
    private lateinit var defenseColumn: TableColumn<Card, Double>

    @FXML
    private lateinit var attackColumn: TableColumn<Card, Double>

    @FXML
    private lateinit var abilitiesColumn: TableColumn<Card, Card>

    @FXML
    private lateinit var buttonsColumn: TableColumn<Card, Card>

    @FXML
    private lateinit var comboBoxDayNightState: ComboBox<DayNightState>

    @FXML
    private lateinit var customizeAppearanceButton: Button

    private var healthPointUpdateInProgress = false

    private var healthPointUpdateJob: Job? = null

    @FXML
    fun showDatabaseOnAction() {
        CardDatabaseView.show()
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
    @FXML
    fun initialize() {
        rootPane.styleClass.add(BACKGROUND)
        cardsTableView.placeholder = Label(
            App.resourceBundle["mainView.tableView.cards.placeholder"]!!
                .format(App.resourceBundle["mainView.button.addCard"])
        )
        cardsTableView.styleClass.addAll(TABLE_GRID_LINES, ALTERNATING_ROW_COLORS)

        customizeAppearanceButton.graphicProperty().bind(
            Image(javaClass.getResourceAsStream("setting.png"), 15.0, 15.0, true, true)
                .invertIfDarkMode()
                .map(::ImageView)
        )

        healthPointsBox.textProperty().addListener { _, oldValue, newValue ->
            healthPointUpdateJob?.cancel()
            healthPointUpdateJob = GlobalScope.launch {
                delay(Duration.seconds(1))
                val newIntValue = newValue.toIntOrNull()

                if (newIntValue == null) {
                    DataHolder.healthPointsProperty.set(oldValue.toInt())
                    return@launch
                }

                if (newIntValue < 0) {
                    DataHolder.healthPointsProperty.set(0)
                    return@launch
                }


                healthPointUpdateInProgress = true
                DataHolder.healthPointsProperty.set(newIntValue)
                healthPointUpdateInProgress = false
            }
        }

        DataHolder.healthPointsProperty.addListener { _, _, newValue ->
            if (healthPointUpdateInProgress) return@addListener
            healthPointsBox.text = newValue.toInt().toString()
        }

        healthPointsBox.text = DataHolder.healthPointsProperty.value.toString()

        DataHolder.abilitySortModeProperty.addListener { _, _, _ -> refreshCardTableFactories() }

        cardsTableView.items = DataHolder.cardList
        refreshCardTableFactories()

        comboBoxDayNightState.converter = EnumStringConverter()
        comboBoxDayNightState.items = FXCollections.observableArrayList(*DayNightState.values())
        comboBoxDayNightState.selectionModel.select(DataHolder.dayNightState.value)
        DataHolder.dayNightState.addListener { _, _, newValue ->
            comboBoxDayNightState.selectionModel.select(newValue)
        }
        comboBoxDayNightState.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            DataHolder.dayNightState.value = newValue
        }

        val dayNightRowIndex = GridPane.getRowIndex(comboBoxDayNightState)
        rootPane.children
            .filter { GridPane.getRowIndex(it) == dayNightRowIndex }
            .forEach {
                it.managedProperty().bind(DataHolder.dayNightMechanicEnabled)
                it.visibleProperty().bind(DataHolder.dayNightMechanicEnabled)
            }

        Platform.runLater { CardDatabase.downloadUpdateAsyncWithGui() }
    }

    private fun refreshCardTableFactories() {
        attackColumn.setDoubleColumnFactories { attackProperty }
        defenseColumn.setDoubleColumnFactories { defenseProperty }
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

    @Suppress("UNCHECKED_CAST")
    private fun TableColumn<Card, Double>.setDoubleColumnFactories(objectProperty: Card.() -> DoubleProperty) {
        cellValueFactory = Callback { objectProperty(it.value) as ObservableValue<Double> }
        cellFactory = Callback {
            object : ObjectDoubleEditingCell<Card>() {
                override fun updateItemPropertyValue(item: Card, newValue: Double) {
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
        val alertResult = Alert(
            CONFIRMATION,
            App.resourceBundle["mainView.confirmation.reset.content"]!!.format(HealthPoints.defaultValue),
            YES, NO
        ).apply {
            this.initOwner(App.instance.currentStage)
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
