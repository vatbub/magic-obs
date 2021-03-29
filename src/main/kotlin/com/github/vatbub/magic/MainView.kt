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

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.util.Callback


class MainView {
    @FXML
    private lateinit var backgroundColorPicker: ColorPicker

    @FXML
    private lateinit var healthPointsBox: TextField

    @FXML
    private lateinit var cardsTableView: TableView<Card>

    @FXML
    private lateinit var defenseColumn: TableColumn<Card, Int>

    @FXML
    private lateinit var attackColumn: TableColumn<Card, Int>

    @FXML
    private lateinit var tappedColumn: TableColumn<Card, Boolean>

    @FXML
    private lateinit var flyingColumn: TableColumn<Card, Boolean>

    @FXML
    private lateinit var buttonsColumn: TableColumn<Card, Card>

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

        cardsTableView.items = DataHolder.cardList
        attackColumn.setIntegerColumnFactories { attackProperty }
        defenseColumn.setIntegerColumnFactories { defenseProperty }
        tappedColumn.setBooleanColumnFactories { tappedProperty }
        flyingColumn.setBooleanColumnFactories { flyingProperty }
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
    private fun TableColumn<Card, Boolean>.setBooleanColumnFactories(objectProperty: Card.() -> BooleanProperty) {
        cellValueFactory = Callback { objectProperty(it.value) }
        cellFactory = CheckBoxTableCell.forTableColumn(this)
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
    fun untapAllCardsButtonOnAction() {
        DataHolder.cardList.forEach { it.tappedProperty.value = false }
    }

    fun close() {

    }
}
