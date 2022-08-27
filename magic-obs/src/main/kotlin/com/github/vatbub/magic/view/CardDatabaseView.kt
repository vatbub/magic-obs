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
@file:Suppress("DuplicatedCode")

package com.github.vatbub.magic.view

import com.github.vatbub.magic.App
import com.github.vatbub.magic.common.CommonPreferenceKeys
import com.github.vatbub.magic.common.preferences
import com.github.vatbub.magic.data.Ability
import com.github.vatbub.magic.data.CardDatabase
import com.github.vatbub.magic.data.CardObjectNoNullables
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.ManaColor.*
import com.github.vatbub.magic.util.get
import com.github.vatbub.magic.util.map
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Callback
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.JMetroStyleClass.TABLE_GRID_LINES
import jfxtras.styles.jmetro.Style.LIGHT
import java.util.concurrent.Executors

class CardDatabaseView {
    companion object {
        fun show(): CardDatabaseView {
            val fxmlLoader =
                FXMLLoader(
                    CardDatabaseView::class.java.getResource("CardDatabaseView.fxml"),
                    App.resourceBundle
                )
            val root = fxmlLoader.load<Parent>()
            with(fxmlLoader.getController<CardDatabaseView>()) {
                val scene = Scene(root)
                jMetro = JMetro(scene, preferences[CommonPreferenceKeys.UIStyle])
                jMetro.styleProperty().bind(DataHolder.uiStyle)

                stage.title = App.resourceBundle["cardDatabaseView.searchLabel"]
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

    val stage: Stage = Stage(StageStyle.DECORATED)

    @FXML
    private lateinit var addButton: Button

    @FXML
    private lateinit var resultsTable: TableView<CardObjectNoNullables>

    @FXML
    private lateinit var searchTextField: TextField

    @FXML
    private lateinit var abilitiesColumn: TableColumn<CardObjectNoNullables, List<Ability>>

    @FXML
    private lateinit var attackColumn: TableColumn<CardObjectNoNullables, Double>

    @FXML
    private lateinit var defenseColumn: TableColumn<CardObjectNoNullables, Double>

    @FXML
    private lateinit var nameColumn: TableColumn<CardObjectNoNullables, String>

    @FXML
    private lateinit var rootPane: GridPane

    private val searchResults: ObservableList<CardObjectNoNullables> = FXCollections.observableArrayList()

    private val searchExecutor = Executors.newSingleThreadExecutor()

    private val searchTasks = mutableListOf<Task<List<CardObjectNoNullables>?>>()

    @FXML
    fun addOnAction() {
        searchExecutor.shutdownNow()
        resultsTable.selectionModel
            .selectedItem
            ?.toOverlayCard()
            ?.let { DataHolder.cardList.add(it) }
        stage.hide()
    }

    @FXML
    fun cancelOnAction() {
        searchExecutor.shutdownNow()
        stage.hide()
    }

    @FXML
    fun initialize() {
        rootPane.styleClass.add(JMetroStyleClass.BACKGROUND)
        resultsTable.placeholder = Label(
            App.resourceBundle["cardDatabaseView.resultView.placeholder"]!!
        )
        resultsTable.styleClass.addAll(TABLE_GRID_LINES)
        resultsTable.items = searchResults
        refreshCardTableFactories()
        addButton.disableProperty().bind(
            resultsTable.selectionModel.selectedItemProperty()
                .map { it == null }
        )
        searchTextField.textProperty().addListener { _, _, newValue -> search(newValue) }
        Platform.runLater { search("") }
    }

    private fun refreshCardTableFactories() {
        attackColumn.cellValueFactory = PropertyValueFactory("power")
        defenseColumn.cellValueFactory = PropertyValueFactory("toughness")
        nameColumn.cellValueFactory = PropertyValueFactory("name")
        abilitiesColumn.cellValueFactory = PropertyValueFactory("abilities")
        abilitiesColumn.cellFactory = Callback { AbilityImageCell() }
        resultsTable.setRowFactory {
            object : TableRow<CardObjectNoNullables>() {
                override fun updateItem(item: CardObjectNoNullables?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || item.colors.isEmpty()) {
                        style = ""
                        return
                    }

                    val color = when (item.colors.first()) {
                        White -> if (DataHolder.uiStyle.value == LIGHT) "ffffff" else "555555"
                        Blue -> if (DataHolder.uiStyle.value == LIGHT) "9999ff" else "0000ff"
                        Black -> if (DataHolder.uiStyle.value == LIGHT) "aaaaaa" else "000000"
                        Red -> if (DataHolder.uiStyle.value == LIGHT) "ff9999" else "ff0000"
                        Green -> if (DataHolder.uiStyle.value == LIGHT) "99ff99" else "00aa00"
                    }
                    style = "-fx-background-color: #$color;"
                }
            }
        }
    }

    private fun search(searchText: String) {
        searchTasks.forEach { it.cancel() }
        val task = object : Task<List<CardObjectNoNullables>?>() {
            override fun call(): List<CardObjectNoNullables>? {
                if (isCancelled) return null
                return CardDatabase.cardObjects
                    .filter { it.name.contains(searchText, ignoreCase = true) }
            }
        }.apply {
            setOnFailed {
                (exception as? Exception)?.printStackTrace()
            }
            setOnSucceeded {
                val newResult = get() ?: return@setOnSucceeded
                searchResults.clear()
                searchResults.addAll(newResult)
            }
        }
        searchTasks.add(task)
        searchExecutor.submit(task)
    }
}
