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
import com.github.vatbub.magic.util.runOnUiThread
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
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
import javafx.util.Duration
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

        private const val additionAnimationLayoutYOffset = 0.0
    }

    lateinit var stage: Stage
        private set

    @FXML
    private lateinit var cardContainer: HBox

    private val animationQueue = AnimationQueue()

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
            val keyValuePrefWidth = KeyValue(it.rootPane.prefWidthProperty(), cardWidth, Interpolator.EASE_BOTH)
            val keyValueMaxWidth = KeyValue(it.rootPane.maxWidthProperty(), cardWidth, Interpolator.EASE_BOTH)
            val keyFrame = KeyFrame(Duration(animationDuration), keyValueMaxWidth, keyValuePrefWidth)
            Timeline(keyFrame).play()

            it.updateMiddleWidth()
        }

        val cardSpacing = min(maxSpacing, (width - cardWidth * viewList.size) / (viewList.size - 1))
        val keyValueSpacing = KeyValue(cardContainer.spacingProperty(), cardSpacing, Interpolator.EASE_BOTH)
        val keyFrameSpacing = KeyFrame(Duration(animationDuration), keyValueSpacing)
        Timeline(keyFrameSpacing).play()
    }

    override fun close() {
        stage.hide()
        DataHolder.cardList.removeListener(itemListener)
    }

    private val itemListener = ListChangeListener<Card> { change ->
        while (change.next()) {
            if (change.wasPermutated()) {
                val noInverses = mutableListOf<Permutation>()
                change.permutations.filterNot { it.newIndex == it.oldIndex }
                    .forEach { permutation ->
                        if (noInverses.map { it.newIndex }.contains(permutation.oldIndex)) {
                            permutation.animate()
                            return@forEach
                        }
                        noInverses.add(permutation)
                        permutation.doPermutation()
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
                    runOnUiThread { statisticCard.animateAddition() }
                }
            }
        }
    }

    private fun StatisticCard.animateAddition() {
        rootPane.translateY = cardContainer.height + additionAnimationLayoutYOffset

        val keyValue1 = KeyValue(rootPane.translateYProperty(), 0, Interpolator.EASE_OUT)
        val keyFrame1 = KeyFrame(Duration(1.5 * animationDuration), keyValue1)
        Timeline(keyFrame1).play()
    }

    private fun Permutation.animate() {
        doPermutation()
        animationQueue.runInQueue {
            val children = listOf(cardContainer.children[oldIndex], cardContainer.children[newIndex])
            val child1 = children.minByOrNull { it.layoutX }!!
            val child2 = children.maxByOrNull { it.layoutX }!!
            val xDifference = child2.layoutX - child1.layoutX

            val keyValue11 = KeyValue(child1.translateXProperty(), -xDifference, Interpolator.EASE_BOTH)
            val keyValue12 = KeyValue(child2.translateXProperty(), xDifference, Interpolator.EASE_BOTH)
            val keyFrame1 = KeyFrame(Duration.ZERO, keyValue11, keyValue12)


            val keyValue21 = KeyValue(child1.translateXProperty(), 0.0, Interpolator.EASE_BOTH)
            val keyValue22 = KeyValue(child2.translateXProperty(), 0.0, Interpolator.EASE_BOTH)
            val keyFrame2 = KeyFrame(Duration(animationDuration), keyValue21, keyValue22)

            animationQueue.add(Timeline(keyFrame1, keyFrame2))
        }
    }

    private fun Permutation.doPermutation() {
        animationQueue.runInQueue {
            val child = cardContainer.children.removeAt(oldIndex)
            if (newIndex > oldIndex)
                cardContainer.children.add(newIndex - 1, child)
            else cardContainer.children.add(newIndex, child)
        }
    }

    private val viewList = mutableListOf<StatisticCard>()
}
