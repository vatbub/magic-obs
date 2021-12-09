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

import com.github.vatbub.magic.animation.queue.*
import com.github.vatbub.magic.data.Card
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.Permutation
import com.github.vatbub.magic.data.permutations
import com.github.vatbub.magic.util.asBackgroundStyle
import com.github.vatbub.magic.util.bindAndMap
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
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
            stage.icons.add(Image(CardStatisticsView::class.java.getResourceAsStream("icon.png")))

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

    private lateinit var stage: Stage

    @FXML
    private lateinit var anchorPane: AnchorPane

    @FXML
    private lateinit var cardContainer: HBox

    private val animationQueue = AnimationQueue()

    @FXML
    fun initialize() {
        anchorPane.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)
        DataHolder.cardList.forEachIndexed { index, card -> handleCardInsertion(index, card) }
        DataHolder.cardList.addListener(itemListener)
        cardContainer.widthProperty().addListener { _, _, newValue ->
            updateSpacing(newValue.toDouble())
        }
        cardContainer.children.addListener(ListChangeListener { change ->
            updateSpacing(cardCount = change.list.size)
        })
    }

    private fun updateSpacing(width: Double = cardContainer.width, cardCount: Int = cardContainer.children.size) {
        val combinedViewList = viewList + cardViewsInRemovalQueue
        if (combinedViewList.isEmpty()) {
            cardContainer.spacing = 0.0
            return
        }
        val optimalCardWidth = (width - minSpacing * (cardCount - 1)) / cardCount
        val actualCardWidth = max(minCardWidth, min(maxCardWidth, optimalCardWidth))
        combinedViewList.forEach {
            Timeline(
                KeyFrame(
                    Duration(animationDuration),
                    KeyValue(it.rootPane.prefWidthProperty(), actualCardWidth, Interpolator.EASE_BOTH),
                    KeyValue(it.rootPane.maxWidthProperty(), actualCardWidth, Interpolator.EASE_BOTH)
                )
            ).play()
        }

        val cardSpacing = min(maxSpacing, (width - actualCardWidth * cardCount) / (cardCount - 1))
        Timeline(
            KeyFrame(
                Duration(animationDuration),
                KeyValue(cardContainer.spacingProperty(), cardSpacing, Interpolator.EASE_BOTH)
            )
        ).play()
    }

    override fun close() {
        stage.close()
        DataHolder.cardList.removeListener(itemListener)
        animationQueue.shutdownNow()
    }

    private val itemListener = ListChangeListener<Card> { change ->
        while (change.next()) {
            if (change.wasPermutated()) {
                change.permutations.filterNot { it.newIndex == it.oldIndex }
                    .forEach { permutation ->
                        permutation.animate()
                    }
            } else {
                change.removed.forEach { removedItem ->
                    handleCardRemoval(removedItem)
                }
                change.addedSubList.forEachIndexed { index, addedItem ->
                    handleCardInsertion(index + change.from, addedItem)
                }
            }
        }
    }

    private fun handleCardRemoval(removedItem: Card) {
        animationQueue.add(CodeBlockQueueItem {
            viewList.firstOrNull { it.card == removedItem }?.let { view ->
                viewList.remove(view)
                cardViewsInRemovalQueue.add(view)
                animationQueue.add(DeferredQueueItem {
                    view.createKillAnimation()
                })
                view.animateRemoval()
            }
        })
    }

    private fun handleCardInsertion(index: Int, addedItem: Card) {
        animationQueue.add(DeferredQueueItem {
            val statisticCard = StatisticCard.newInstance(addedItem)
            viewList.add(statisticCard)
            cardContainer.children.add(index, statisticCard.rootPane)
            statisticCard.createAddAnimation().toQueueItem()
        })
    }

    private fun StatisticCard.createAddAnimation(): Timeline {
        rootPane.translateY = cardContainer.height + additionAnimationLayoutYOffset

        return Timeline(
            KeyFrame(
                Duration(1.5 * animationDuration),
                KeyValue(rootPane.translateYProperty(), 0, Interpolator.EASE_OUT)
            )
        )
    }

    private fun StatisticCard.animateRemoval() = animationQueue.add(DeferredQueueItem {
        rootPane.translateY = 0.0

        val targetWidth = -cardContainer.spacing


        Timeline(
            KeyFrame(
                Duration(1.5 * animationDuration),
                KeyValue(
                    rootPane.translateYProperty(),
                    cardContainer.height + additionAnimationLayoutYOffset,
                    Interpolator.EASE_IN
                ),
                KeyValue(rootPane.prefWidthProperty(), rootPane.width, Interpolator.EASE_BOTH),
                KeyValue(rootPane.maxWidthProperty(), rootPane.width, Interpolator.EASE_BOTH)
            ),
            KeyFrame(
                Duration(2.5 * animationDuration),
                KeyValue(rootPane.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                KeyValue(rootPane.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH)
            )
        ).apply {
            setOnFinished {
                cardContainer.children.remove(this@animateRemoval.rootPane)
                cardViewsInRemovalQueue.remove(this@animateRemoval)
            }
        }.toQueueItem()
    })

    private fun Permutation.animate() {
        animationQueue.add(DeferredQueueItem {
            val children = listOf(cardContainer.children[oldIndex], cardContainer.children[newIndex])
            val child1 = children.minByOrNull { it.layoutX + it.translateX }!!
            val child2 = children.maxByOrNull { it.layoutX + it.translateX }!!

            val xDifference = child2.layoutX - child1.layoutX

            Timeline(
                KeyFrame(
                    Duration(animationDuration),
                    KeyValue(child1.translateXProperty(), xDifference, Interpolator.EASE_BOTH),
                    KeyValue(child2.translateXProperty(), -xDifference, Interpolator.EASE_BOTH)
                )
            ).apply {
                setOnFinished {
                    doPermutation()
                    children.forEach { it.translateX = 0.0 }
                }
            }.toQueueItem()
        })
        animationQueue.addFrameDelay()
    }

    private fun Permutation.doPermutation() {
        val child = cardContainer.children.removeAt(oldIndex)
        cardContainer.children.add(newIndex, child)
    }

    private val viewList = mutableListOf<StatisticCard>()
    private val cardViewsInRemovalQueue = mutableListOf<StatisticCard>()
}
