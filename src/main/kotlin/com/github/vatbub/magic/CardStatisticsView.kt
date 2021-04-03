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
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.Closeable
import kotlin.math.max
import kotlin.math.min

class CardStatisticsView : Closeable {
    companion object {
        fun show(): CardStatisticsView {
            val stage = Stage(StageStyle.UNIFIED)

            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("CardStatisticsView.fxml"))
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<CardStatisticsView>()
            controllerInstance.stage = stage

            val scene = Scene(root)

            stage.title = "Magic OBS Card Statistics"
            // stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))

            stage.scene = scene

            stage.show()
            return controllerInstance
        }

        private const val animationDuration = 500.0
        private const val maxCardWidth = 280.0
        private const val minCardWidth = 160.0
        private const val maxSpacing = 10.0
        private const val minSpacing = 8.0
    }

    lateinit var stage: Stage
        private set

    @FXML
    private lateinit var cardContainer: HBox

    @FXML
    fun initialize() {
        cardContainer.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)
        DataHolder.cardList.addListener(itemListener)
        cardContainer.widthProperty().addListener { _, _, newValue ->
            updateSpacing(newValue.toDouble())
        }
    }

    private fun updateSpacing(width: Double = cardContainer.width) {
        if (viewList.isEmpty()) {
            cardContainer.spacing = 0.0
            return
        }
        val cardWidth = max(
            minCardWidth, min(
                maxCardWidth, (width - minSpacing * (viewList.size - 1)) / viewList.size
            )
        )
        viewList.forEach {
            it.rootPane.prefWidth = cardWidth
            it.rootPane.maxWidth = cardWidth
            it.updateMiddleWidth()
        }
        cardContainer.spacing = min(maxSpacing, (width - cardWidth * viewList.size) / (viewList.size - 1))
    }

    override fun close() {
        stage.hide()
        DataHolder.cardList.removeListener(itemListener)
    }

    private val itemListener = ListChangeListener<Card> { change ->
        while (change.next()) {
            if (change.wasPermutated()) {
                change.permutations.filterNot { it.newIndex == it.oldIndex }
                    .forEach { permutation ->
                        val child = cardContainer.children.removeAt(permutation.oldIndex)
                        if (permutation.newIndex > permutation.oldIndex)
                            cardContainer.children.add(permutation.newIndex - 1, child)
                        else cardContainer.children.add(permutation.newIndex, child)

                    }
            } else {
                change.removed.forEach { removedItem ->
                    viewList.firstOrNull { it.card == removedItem }?.let { view ->
                        viewList.remove(view)
                        cardContainer.children.remove(view.rootPane)
                        Platform.runLater { updateSpacing() }
                    }
                }
                change.addedSubList.forEachIndexed { index, addedItem ->
                    val statisticCard = StatisticCard.newInstance(addedItem)
                    viewList.add(statisticCard)
                    cardContainer.children.add(index + change.from, statisticCard.rootPane)
                    Platform.runLater { updateSpacing() }
                }
            }
        }
    }

    private val viewList = mutableListOf<StatisticCard>()
}
