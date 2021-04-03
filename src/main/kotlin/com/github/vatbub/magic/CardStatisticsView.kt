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

import com.github.vatbub.magic.util.asBackgroundStyle
import com.github.vatbub.magic.util.bindAndMap
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Callback
import kotlin.properties.Delegates

class CardStatisticsView {
    companion object {
        fun show(): CardStatisticsView {
            val stage = Stage(StageStyle.UNDECORATED)

            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("CardStatisticsView.fxml"))
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<CardStatisticsView>()
            controllerInstance.stage = stage

            val scene = Scene(root)

            stage.title = "Magic OBS Card Statistics"
            // stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()
            return controllerInstance
        }

        private const val animationDuration = 500.0
    }

    lateinit var stage: Stage
        private set

    @FXML
    private lateinit var cardContainer: ListView<Card>

    @FXML
    fun initialize() {
        cardContainer.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)
        cardContainer.items = DataHolder.cardList
        cardContainer.cellFactory = Callback {
            CardCell()
                .also {
                    it.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)
                }
        }
    }

    private class CardCell : ListCell<Card>() {
        var statisticCard: StatisticCard? by Delegates.observable(null) { _, _, newValue ->
            graphic = newValue?.rootPane
        }
            private set

        override fun updateItem(item: Card?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                statisticCard = null
                return
            }
            statisticCard = StatisticCard.newInstance(item)
        }
    }
}
