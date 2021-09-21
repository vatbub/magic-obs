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
import com.github.vatbub.magic.data.*
import com.github.vatbub.magic.data.Either.Right
import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.fitFontSizeToWidth
import com.github.vatbub.magic.util.times
import javafx.animation.Interpolator.EASE_BOTH
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.effect.GaussianBlur
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import kotlin.collections.set
import kotlin.math.abs
import kotlin.properties.Delegates
import kotlin.time.ExperimentalTime


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

        private const val abilityScrollMargin = 15.0
        private const val abilityInitialScrollSpeed = 0.5
        private const val abilityScrollSpeed = 0.02
        private const val abilityScrollPauseDurationInMillis = 4000.0
    }

    var card: Card? by Delegates.observable(null) { _, oldValue, newValue ->
        oldValue?.abilities?.removeListener(abilitiesChangeListener)
        if (newValue == null) return@observable

        with(newValue) {
            updateStatisticLabel(
                attackProperty.value,
                defenseProperty.value
            )
            updateCounter(counterProperty.value)

            attackProperty.addListener { _, _, newValue ->
                updateStatisticLabel(attack = newValue.toInt())
            }
            defenseProperty.addListener { _, _, newValue ->
                updateStatisticLabel(defense = newValue.toInt())
            }
            counterProperty.addListener { _, _, newValue -> updateCounter(newValue.toInt()) }

            abilities.addListener(abilitiesChangeListener)
            abilityIcons.children.clear()
            abilities.forEach { addAbility(it.left()) }
        }
    }
        private set

    @FXML
    lateinit var rootPane: StackPane

    @FXML
    private lateinit var statisticLabel: Label

    @FXML
    private lateinit var backgroundRectangle: Rectangle

    @FXML
    private lateinit var killCrossLeftBottomToRightTop: ImageView

    @FXML
    private lateinit var killCrossLeftTopToRightBottom: ImageView

    @FXML
    private lateinit var abilityIcons: HBox

    @FXML
    private lateinit var abilityIconsWrapper: HBox

    private var scrollAnimation: Timeline? = null

    private val abilitiesClippingRectangle = Rectangle().apply {
        addGaussianBlurForClip()
    }

    private val abilityAnimationQueue = AnimationQueue()

    private val abilityViewMap = mutableMapOf<RenderedAbility, ImageView>()

    private val fontOffset = SimpleDoubleProperty(0.0)

    private val abilitiesClippingRectangleXMargin = SimpleDoubleProperty(0.0)

    @FXML
    fun initialize() {
        backgroundRectangle.widthProperty().bind(rootPane.widthProperty())
        abilityIcons.children.addListener(ListChangeListener { change ->
            animateStatisticsOffset(change.list.size)
        })
        DataHolder.cardStatisticsFontSpecProperty.addListener { _, _, newValue ->
            updateMiddleFontSize(fontSpec = newValue, forceUpdate = true)
        }
        statisticLabel.widthProperty().addListener { _, _, _ ->
            updateMiddleFontSize()
        }
        rootPane.widthProperty().addListener { _, _, newValue ->
            updateMiddleFontSize(targetWidth = newValue.toDouble() - 20)
        }
        fontOffset.addListener { _, _, newValue ->
            updateMiddleFontSize(targetHeight = backgroundRectangle.height - newValue.toDouble())
        }
        abilityIcons.widthProperty().addListener { _, _, newValue ->
            determineIfAbilitiesShouldScroll(abilitiesWidth = newValue.toDouble())
            abilitiesClippingRectangle.updateDimensions(abilitiesWidth = newValue.toDouble())
        }
        rootPane.widthProperty().addListener { _, _, newValue ->
            determineIfAbilitiesShouldScroll(rootWidth = newValue.toDouble())
            abilitiesClippingRectangle.updateDimensions(rootWidth = newValue.toDouble())
        }
        abilitiesClippingRectangleXMargin.addListener { _, _, newValue ->
            abilitiesClippingRectangle.updateDimensions(margin = newValue.toDouble())
        }

        abilityIconsWrapper.clip = abilitiesClippingRectangle
        abilitiesClippingRectangle.heightProperty().bind(abilityIcons.heightProperty().add(10.0))
    }

    private fun Rectangle.updateDimensions(
        abilitiesWidth: Double = abilityIcons.width,
        rootWidth: Double = rootPane.width,
        margin: Double = abilitiesClippingRectangleXMargin.value
    ) {
        x = margin + abs(rootWidth - abilitiesWidth) / 2
        width = rootWidth - 2 * margin
    }

    private fun determineIfAbilitiesShouldScroll(
        abilitiesWidth: Double = abilityIcons.width,
        rootWidth: Double = rootPane.width
    ) {
        val xMarginTargetValue = if (abilitiesWidth > (rootWidth - abilityScrollMargin)) abilityScrollMargin
        else 0.0

        if (abilitiesWidth > rootWidth) startScrollAnimation(abilityInitialScrollSpeed)
        else stopScrollAnimation()

        Timeline(
            KeyFrame(
                Duration(100.0),
                KeyValue(abilitiesClippingRectangleXMargin, xMarginTargetValue)
            )
        ).play()
    }

    private fun startScrollAnimation(initialScrollSpeed: Double = abilityScrollSpeed) {
        scrollAnimation?.stop()

        val translateAmount = abilityScrollMargin + abs(rootPane.width - abilityIcons.width) / 2
        val firstScrollDuration = translateAmount / initialScrollSpeed
        val secondScrollDuration = translateAmount / abilityScrollSpeed

        val keyValue1 = KeyValue(abilityIcons.translateXProperty(), -translateAmount, EASE_BOTH)
        val keyValue2 = KeyValue(abilityIcons.translateXProperty(), translateAmount, EASE_BOTH)

        scrollAnimation = Timeline(
            KeyFrame(
                Duration(firstScrollDuration),
                keyValue1
            ),
            KeyFrame(
                Duration(firstScrollDuration + abilityScrollPauseDurationInMillis),
                keyValue1
            ),
            KeyFrame(
                Duration(firstScrollDuration + secondScrollDuration + abilityScrollPauseDurationInMillis),
                keyValue2
            ),
            KeyFrame(
                Duration(firstScrollDuration + secondScrollDuration + 2 * abilityScrollPauseDurationInMillis),
                keyValue2
            )
        )
            .apply { setOnFinished { startScrollAnimation() } }
            .apply { play() }
    }

    private fun stopScrollAnimation() {
        scrollAnimation?.stop()

        scrollAnimation = Timeline(
            KeyFrame(
                Duration(2000.0),
                KeyValue(abilityIcons.translateXProperty(), 0.0, EASE_BOTH)
            )
        ).apply { play() }
    }

    private fun updateMiddleFontSize(
        targetWidth: Double = backgroundRectangle.width - 20,
        targetHeight: Double = backgroundRectangle.height - fontOffset.value,
        tolerance: Double = 1.0,
        fontSpec: FontSpec = DataHolder.cardStatisticsFontSpecProperty.get(),
        forceUpdate: Boolean = false
    ) {
        if (targetWidth <= 0) return
        if (targetHeight <= 0) return

        with(statisticLabel) {
            val newFontSize = fitFontSizeToWidth(font, this.text, targetWidth, targetHeight)
            if (abs(font.size - newFontSize) <= tolerance && !forceUpdate) return
            font = fontSpec.withSize(newFontSize)
        }
    }

    fun createKillAnimation(): ConcurrentTimelineQueueItem {
        killCrossLeftTopToRightBottom.prepareKillAnimation()
        killCrossLeftBottomToRightTop.prepareKillAnimation()

        val leftTopToRightBottomClip = Rectangle(0.0, 0.0, 0.0, 0.0).addGaussianBlurForClip()
        val leftBottomToRightTopClip = Rectangle(0.0, 0.0, 0.0, 0.0).addGaussianBlurForClip().apply {
            xProperty().bindAndMap(widthProperty()) { killCrossLeftBottomToRightTop.fitWidth - it.toDouble() }
        }

        killCrossLeftTopToRightBottom.clip = leftTopToRightBottomClip
        killCrossLeftBottomToRightTop.clip = leftBottomToRightTopClip

        return ConcurrentTimelineQueueItem(
            rootPane.shakeAnimation(2.0 * killAnimationDuration),
            Timeline(
                KeyFrame(
                    killAnimationDuration,
                    KeyValue(leftTopToRightBottomClip.widthProperty(), rootPane.width, EASE_BOTH),
                    KeyValue(leftTopToRightBottomClip.heightProperty(), rootPane.height, EASE_BOTH),
                    KeyValue(leftBottomToRightTopClip.widthProperty(), 0, EASE_BOTH),
                    KeyValue(leftBottomToRightTopClip.heightProperty(), 0, EASE_BOTH)
                ),
                KeyFrame(
                    2.0 * killAnimationDuration,
                    KeyValue(leftBottomToRightTopClip.widthProperty(), rootPane.width, EASE_BOTH),
                    KeyValue(leftBottomToRightTopClip.heightProperty(), rootPane.height, EASE_BOTH)
                )
            )
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
    }

    private fun updateCounter(counter: Int) {
        val currentNumberOfSixes = abilityViewMap.keys
            .mapNotNull { it.rightOrNull() }
            .filter { it.value == 6 }
            .count()

        val numberOfSixes = (counter / 6.0).toInt()
        val remainder = counter % 6

        // Remove previous remainders
        abilityViewMap.keys
            .filter { it is Right<Ability, CounterAbility> && it.value.value != 6 }
            .reversed()
            .forEach { removeAbility(it) }

        // Remove excess sixes
        if (currentNumberOfSixes > numberOfSixes) {
            abilityViewMap.keys
                .filter { it is Right<Ability, CounterAbility> && it.value.value == 6 }
                .takeLast(currentNumberOfSixes - numberOfSixes)
                .reversed()
                .forEach { removeAbility(it) }
        }

        repeat(numberOfSixes - currentNumberOfSixes) {
            addAbility(CounterAbility(6).right())
        }

        if (remainder > 0)
            addAbility(CounterAbility(remainder).right())
    }

    private val abilitiesChangeListener = ListChangeListener<Ability> { change ->
        while (change.next()) {
            change.removed.forEach { removedItem ->
                removeAbility(removedItem.left())
            }
            change.addedSubList.forEach { addedItem ->
                addAbility(addedItem.left())
            }
        }
    }

    private fun addAbility(ability: RenderedAbility) {
        if (abilityViewMap.containsKey(ability)) return
        val imageView = ImageView(Image(ability.iconStream, abilityIconSize, abilityIconSize, false, true))

        abilityViewMap[ability] = imageView
        abilityAnimationQueue.add(CodeBlockQueueItem {
            imageView.animateAddition()
        })
    }

    private fun removeAbility(ability: RenderedAbility) {
        abilityViewMap.remove(ability)?.let { view ->
            abilityAnimationQueue.add(CodeBlockQueueItem {
                view.animateRemoval()
            })
        }
    }

    private fun ImageView.animateAddition() {
        val targetWidth = image.width
        val space = Rectangle(0.0, image.height, Color.TRANSPARENT)

        abilityAnimationQueue.add(CodeBlockQueueItem {
            abilityIcons.children.add(space)
        })

        Timeline(
            KeyFrame(
                Duration(abilityAnimationDuration),
                KeyValue(space.widthProperty(), targetWidth, EASE_BOTH)
            )
        ).let { abilityAnimationQueue.add(it.toQueueItem()) }

        abilityAnimationQueue.add(CodeBlockQueueItem {
            opacity = 0.0
            val indexOfSpace = abilityIcons.children.indexOf(space)
            abilityIcons.children[indexOfSpace] = this
        })

        Timeline(
            KeyFrame(
                Duration(0.5 * abilityAnimationDuration),
                KeyValue(opacityProperty(), 1.0)
            )
        ).let { abilityAnimationQueue.add(it.toQueueItem()) }
    }

    private fun ImageView.animateRemoval() {
        val targetWidth = -abilityIcons.spacing
        val space = Rectangle(image.width, image.height, Color.TRANSPARENT)

        Timeline(
            KeyFrame(
                Duration(0.5 * abilityAnimationDuration),
                KeyValue(opacityProperty(), 0.0)
            )
        ).let { abilityAnimationQueue.add(it.toQueueItem()) }

        abilityAnimationQueue.add(CodeBlockQueueItem {
            val indexInParent = abilityIcons.children.indexOf(this)
            abilityIcons.children[indexInParent] = space
        })

        Timeline(
            KeyFrame(
                Duration(abilityAnimationDuration),
                KeyValue(space.widthProperty(), targetWidth, EASE_BOTH)
            )
        ).let { abilityAnimationQueue.add(it.toQueueItem()) }

        abilityAnimationQueue.add(CodeBlockQueueItem {
            abilityIcons.children.remove(space)
        })
    }

    private fun animateStatisticsOffset(childCount: Int) {
        val targetValue = if (childCount != 0) statisticsOffset else 0.0
        val fontOffsetTargetValue = if (childCount != 0) abilityIcons.height * 0.4 else 0.0

        if (statisticLabel.translateY == -targetValue) return

        Timeline(
            KeyFrame(
                statisticsOffsetAnimationDuration,
                KeyValue(statisticLabel.translateYProperty(), -targetValue, EASE_BOTH),
                KeyValue(fontOffset, fontOffsetTargetValue, EASE_BOTH)
            )
        ).play()
    }
}
