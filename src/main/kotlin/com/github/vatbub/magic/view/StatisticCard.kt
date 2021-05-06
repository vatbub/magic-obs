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

import com.github.vatbub.magic.animation.queue.AnimationQueue
import com.github.vatbub.magic.animation.queue.CodeBlockQueueItem
import com.github.vatbub.magic.animation.queue.ConcurrentTimelineQueueItem
import com.github.vatbub.magic.animation.queue.toQueueItem
import com.github.vatbub.magic.data.Ability
import com.github.vatbub.magic.data.Card
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.util.awaitLayoutCycles
import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.times
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.effect.GaussianBlur
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import kotlin.math.abs
import kotlin.math.min
import kotlin.properties.Delegates
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds


class StatisticCard {
    companion object {
        fun newInstance(card: Card): StatisticCard {
            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("StatisticCard.fxml"))
            fxmlLoader.load<Parent>()
            return fxmlLoader.getController<StatisticCard>().also {
                it.card = card
            }
        }

        private val killAnimationDuration = Duration(1000.0)

        private const val abilityIconSize = 40.0
        private const val statisticsOffset = 20.0
        private val statisticsOffsetAnimationDuration = Duration(500.0)

        private const val abilityAnimationDuration = 500.0
    }

    var card: Card? by Delegates.observable(null) { _, oldValue, newValue ->
        oldValue?.abilities?.removeListener(abilitiesChangeListener)
        if (newValue == null) return@observable

        cardAboutToBeKilled = false
        with(newValue) {
            updateStatisticLabel(
                newValue.attackProperty.value,
                newValue.defenseProperty.value
            )

            attackProperty.addListener { _, _, newValue ->
                updateStatisticLabel(attack = newValue.toInt())
            }
            defenseProperty.addListener { _, _, newValue ->
                updateStatisticLabel(defense = newValue.toInt())
            }

            abilities.addListener(abilitiesChangeListener)
            abilityIcons.children.clear()
            abilities.forEach { addAbility(it) }
        }
    }
        private set

    @FXML
    lateinit var rootPane: GridPane

    @FXML
    private lateinit var statisticLabel: Label

    @FXML
    private lateinit var middleImageView: ImageView

    @FXML
    private lateinit var killCrossLeftBottomToRightTop: ImageView

    @FXML
    private lateinit var killCrossLeftTopToRightBottom: ImageView

    @FXML
    private lateinit var abilityIcons: HBox

    private val abilityAnimationQueue = AnimationQueue()

    private val abilityViewMap = mutableMapOf<Ability, ImageView>()

    private var statisticsLabelFontSizeUpdateInProgress = false
    private var statisticsLabelCancelFontSizeUpdate = false
    private var cardAboutToBeKilled = false

    @FXML
    fun initialize() {
        updateMiddleWidth()
        rootPane.widthProperty().addListener { _, _, _ ->
            updateMiddleWidth()
        }
        abilityIcons.children.addListener(ListChangeListener { change ->
            animateStatisticsOffset(change.list.size)
        })
        DataHolder.cardStatisticsFontSpecProperty.addListener { _, _, newValue ->
            updateMiddleFontSize(fontSpec = newValue)
        }
    }

    fun updateMiddleWidth() {
        val newMiddleWidth =
            rootPane.prefWidth - rootPane.getCellBounds(0, 0).width - rootPane.getCellBounds(2, 0).width + 2

        middleImageView.fitWidth = newMiddleWidth

        updateMiddleFontSize(newMiddleWidth - 10)
    }

    private fun updateMiddleFontSize(
        targetWidth: Double = middleImageView.fitWidth,
        targetHeight: Double = middleImageView.fitHeight,
        tolerance: Double = 0.05,
        fontSpec: FontSpec = DataHolder.cardStatisticsFontSpecProperty.get()
    ) {
        if (cardAboutToBeKilled) return
        if (targetWidth < 0) return
        if (targetHeight < 0) return

        if (statisticsLabelFontSizeUpdateInProgress) {
            statisticsLabelCancelFontSizeUpdate = true
            Platform.runLater { updateMiddleFontSize(targetWidth, targetHeight, tolerance) }
            return
        }

        Platform.runLater { statisticLabel.updateMiddleFontSize(targetWidth, targetHeight, tolerance, fontSpec) }
    }

    private fun Label.updateMiddleFontSize(
        targetWidth: Double,
        targetHeight: Double,
        tolerance: Double,
        fontSpec: FontSpec
    ) {
        if (statisticsLabelCancelFontSizeUpdate || cardAboutToBeKilled) {
            statisticsLabelCancelFontSizeUpdate = false
            statisticsLabelFontSizeUpdateInProgress = false
            return
        }

        val widthStepSize = (targetWidth - width) / targetWidth
        val heightStepSize = (targetHeight - height) / targetHeight
        val stepSize = min(widthStepSize, heightStepSize)

        if (abs(stepSize) < tolerance) {
            statisticsLabelFontSizeUpdateInProgress = false
            return
        }

        statisticsLabelFontSizeUpdateInProgress = true
        val fontSize = font.size + 10 * stepSize
        font = fontSpec.withSize(fontSize)
        Platform.runLater { this.updateMiddleFontSize(targetWidth, targetHeight, tolerance, fontSpec) }
    }

    fun createKillAnimation(): ConcurrentTimelineQueueItem {
        cardAboutToBeKilled = true
        killCrossLeftTopToRightBottom.prepareKillAnimation()
        killCrossLeftBottomToRightTop.prepareKillAnimation()

        val leftTopToRightBottomClip = Rectangle(0.0, 0.0, 0.0, 0.0).addGaussianBlurForClip()
        val leftBottomToRightTopClip = Rectangle(0.0, 0.0, 0.0, 0.0).addGaussianBlurForClip().apply {
            xProperty().bindAndMap(widthProperty()) { killCrossLeftBottomToRightTop.fitWidth - it.toDouble() }
        }

        killCrossLeftTopToRightBottom.clip = leftTopToRightBottomClip
        killCrossLeftBottomToRightTop.clip = leftBottomToRightTopClip

        val keyValueWidthLeftTopToRightBottom1 = KeyValue(
            leftTopToRightBottomClip.widthProperty(),
            rootPane.width,
            Interpolator.EASE_BOTH
        )
        val keyValueHeightLeftTopToRightBottom1 = KeyValue(
            leftTopToRightBottomClip.heightProperty(),
            rootPane.height,
            Interpolator.EASE_BOTH
        )
        val keyValueWidthLeftBottomToRightTop1 = KeyValue(
            leftBottomToRightTopClip.widthProperty(),
            0,
            Interpolator.EASE_BOTH
        )
        val keyValueHeightLeftBottomToRightTop1 = KeyValue(
            leftBottomToRightTopClip.heightProperty(),
            0,
            Interpolator.EASE_BOTH
        )
        val keyFrame1 =
            KeyFrame(
                killAnimationDuration, keyValueWidthLeftTopToRightBottom1, keyValueHeightLeftTopToRightBottom1,
                keyValueWidthLeftBottomToRightTop1, keyValueHeightLeftBottomToRightTop1
            )

        val keyValueWidthLeftBottomToRightTop2 = KeyValue(
            leftBottomToRightTopClip.widthProperty(),
            rootPane.width,
            Interpolator.EASE_BOTH
        )
        val keyValueHeightLeftBottomToRightTop2 = KeyValue(
            leftBottomToRightTopClip.heightProperty(),
            rootPane.height,
            Interpolator.EASE_BOTH
        )
        val keyFrame2 =
            KeyFrame(
                2.0 * killAnimationDuration,
                keyValueWidthLeftBottomToRightTop2,
                keyValueHeightLeftBottomToRightTop2
            )

        return ConcurrentTimelineQueueItem(
            rootPane.shakeAnimation(2.0 * killAnimationDuration),
            Timeline(keyFrame1, keyFrame2)
        )
    }

    private fun ImageView.prepareKillAnimation() {
        isPreserveRatio = false
        isVisible = true
        fitHeightProperty().bind(rootPane.heightProperty())
        fitWidthProperty().bind(rootPane.widthProperty())
    }

    private fun <T : Node> T.addGaussianBlurForClip() = apply {
        effect = GaussianBlur(10.0)
    }

    @OptIn(ExperimentalTime::class)
    private fun updateStatisticLabel(
        attack: Int = card!!.attackProperty.value,
        defense: Int = card!!.defenseProperty.value
    ) {
        statisticLabel.text = "$attack/$defense"
        // Laying out text apparently takes more than one layout cycle...
        awaitLayoutCycles(5, 10.milliseconds) { updateMiddleFontSize() }
    }

    private val abilitiesChangeListener = ListChangeListener<Ability> { change ->
        while (change.next()) {
            change.removed.forEach { removedItem ->
                removeAbility(removedItem)
            }
            change.addedSubList.forEach { addedItem ->
                addAbility(addedItem)
            }
        }
    }

    private fun addAbility(ability: Ability) {
        if (abilityViewMap.containsKey(ability)) return
        val iconStream =
            StatisticCard::class.java.getResourceAsStream("AbilityIcons/${ability.imageFileName}.png")
        val imageView = ImageView(Image(iconStream, abilityIconSize, abilityIconSize, false, true))

        abilityViewMap[ability] = imageView
        abilityAnimationQueue.add(CodeBlockQueueItem {
            imageView.animateAddition()
        })
    }

    private fun removeAbility(ability: Ability) {
        abilityViewMap.remove(ability)?.let { view ->
            abilityAnimationQueue.add(CodeBlockQueueItem {
                view.animateRemoval()
            })
        }
    }

    private fun ImageView.animateAddition() {
        val targetWidth = image.width
        val space = Rectangle(0.0, image.height, Color.TRANSPARENT)

        val keyValueSpaceWidth = KeyValue(space.widthProperty(), targetWidth, Interpolator.EASE_BOTH)

        val keyFrameWidth = KeyFrame(Duration(abilityAnimationDuration), keyValueSpaceWidth)

        val keyValueOpacity = KeyValue(opacityProperty(), 1.0)
        val keyFrameOpacity = KeyFrame(Duration(0.5 * abilityAnimationDuration), keyValueOpacity)

        abilityAnimationQueue.add(CodeBlockQueueItem {
            abilityIcons.children.add(space)
        })
        abilityAnimationQueue.add(Timeline(keyFrameWidth).toQueueItem())

        abilityAnimationQueue.add(CodeBlockQueueItem {
            opacity = 0.0
            val indexOfSpace = abilityIcons.children.indexOf(space)
            abilityIcons.children[indexOfSpace] = this
        })
        abilityAnimationQueue.add(Timeline(keyFrameOpacity).toQueueItem())
    }

    private fun ImageView.animateRemoval() {
        val keyValueOpacity = KeyValue(opacityProperty(), 0.0)
        val keyFrameOpacity = KeyFrame(Duration(0.5 * abilityAnimationDuration), keyValueOpacity)

        val targetWidth = -abilityIcons.spacing
        val space = Rectangle(image.width, image.height, Color.TRANSPARENT)

        val keyValueSpaceWidth = KeyValue(space.widthProperty(), targetWidth, Interpolator.EASE_BOTH)

        val keyFrameWidth = KeyFrame(Duration(abilityAnimationDuration), keyValueSpaceWidth)

        abilityAnimationQueue.add(Timeline(keyFrameOpacity).toQueueItem())
        abilityAnimationQueue.add(CodeBlockQueueItem {
            val indexInParent = abilityIcons.children.indexOf(this)
            abilityIcons.children[indexInParent] = space
        })
        abilityAnimationQueue.add(Timeline(keyFrameWidth).toQueueItem())
        abilityAnimationQueue.add(CodeBlockQueueItem {
            abilityIcons.children.remove(space)
        })
    }

    private fun animateStatisticsOffset(childCount: Int) {
        val targetValue = if (childCount != 0) statisticsOffset else 0.0

        if (statisticLabel.translateY == -targetValue) return

        val keyValue = KeyValue(statisticLabel.translateYProperty(), -targetValue, Interpolator.EASE_BOTH)
        val keyFrame = KeyFrame(statisticsOffsetAnimationDuration, keyValue)
        Timeline(keyFrame).play()
    }
}
