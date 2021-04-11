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

import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.times
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.effect.GaussianBlur
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates


class StatisticCard {
    companion object {
        fun newInstance(card: Card): StatisticCard {
            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("StatisticCard.fxml"))
            fxmlLoader.load<Parent>()
            return fxmlLoader.getController<StatisticCard>().also {
                it.card = card
            }
        }

        private const val maxFontSize = 90.0
        private const val minFontSize = 50.0
        private val killAnimationDuration = Duration(1000.0)
    }

    var card: Card? by Delegates.observable(null) { _, _, newValue ->
        if (newValue == null) return@observable
        with(newValue) {
            updateStatisticLabel(
                newValue.attackProperty.value,
                newValue.defenseProperty.value,
                newValue.tappedProperty.value,
                newValue.flyingProperty.value
            )

            attackProperty.addListener { _, _, newValue ->
                updateStatisticLabel(attack = newValue.toInt())
            }
            defenseProperty.addListener { _, _, newValue ->
                updateStatisticLabel(defense = newValue.toInt())
            }
            tappedProperty.addListener { _, _, newValue ->
                updateStatisticLabel(tapped = newValue)
            }
            flyingProperty.addListener { _, _, newValue ->
                updateStatisticLabel(flying = newValue)
            }
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
    fun initialize() {
        updateMiddleWidth()
        rootPane.widthProperty().addListener { _, _, _ ->
            updateMiddleWidth()
        }
    }

    fun updateMiddleWidth() {
        val newMiddleWidth =
            rootPane.prefWidth - rootPane.getCellBounds(0, 0).width - rootPane.getCellBounds(2, 0).width + 2
        val fontSize = max(minFontSize, min(maxFontSize, newMiddleWidth - 135))

        middleImageView.fitWidth = newMiddleWidth
        statisticLabel.font = Fonts.magic(fontSize)
    }

    fun playKillAnimation(onFinished: (StatisticCard) -> Unit) {
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

        rootPane.shakeAnimation(2.0 * killAnimationDuration).play()

        Timeline(keyFrame1, keyFrame2)
            .apply { setOnFinished { onFinished(this@StatisticCard) } }
            .play()
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

    private fun updateStatisticLabel(
        attack: Int = card!!.attackProperty.value,
        defense: Int = card!!.defenseProperty.value,
        tapped: Boolean = card!!.tappedProperty.value,
        flying: Boolean = card!!.flyingProperty.value
    ) {
        statisticLabel.text = with(StringBuilder("$attack/$defense")) {
            if (tapped)
                append("*")
            if (flying)
                append("(f)")
            toString()
        }
    }
}
