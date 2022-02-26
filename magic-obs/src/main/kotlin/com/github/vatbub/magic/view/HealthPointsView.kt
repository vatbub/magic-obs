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
import com.github.vatbub.magic.animation.queue.toQueueItem
import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.util.asBackgroundStyle
import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.runOnUiThread
import javafx.animation.Interpolator.EASE_IN
import javafx.animation.Interpolator.EASE_OUT
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.effect.MotionBlur
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class HealthPointsView : Closeable {
    companion object {
        fun show(): HealthPointsView {
            val stage = Stage(StageStyle.UNIFIED)

            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("HealthPointsView.fxml"))
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<HealthPointsView>()
            controllerInstance.stage = stage

            val scene = Scene(root)

            stage.title = "Magic OBS Health Points".appendInstanceNumber()
            stage.icons.add(Image(HealthPointsView::class.java.getResourceAsStream("icon.png")))
            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()
            return controllerInstance
        }

        private const val animationDuration = 500.0
        private const val fontSizeFactor = 0.55
    }

    lateinit var stage: Stage
        private set

    @FXML
    private lateinit var rootPane: AnchorPane

    @FXML
    private lateinit var healthPointsLabel: Label

    @FXML
    private lateinit var backgroundImageView: ImageView

    private val animationQueue = AnimationQueue()

    private var loadBackgroundImageDelayJob: Job? = null

    @FXML
    fun initialize() {
        rootPane.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)
        healthPointsLabel.textFillProperty().bind(DataHolder.healthPointsFontColorProperty)

        DataHolder.healthPointsImageSpecProperty.addListener { _, _, newValue ->
            loadBackgroundImage(imageSpec = newValue)
        }

        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty())
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty())

        backgroundImageView.fitWidthProperty().addListener { _, _, newValue ->
            loadBackgroundImageWithDelay(requestedWidth = newValue.toDouble())
        }
        backgroundImageView.fitHeightProperty().addListener { _, _, newValue ->
            loadBackgroundImageWithDelay(requestedHeight = newValue.toDouble())
        }

        healthPointsLabel.font = DataHolder.healthPointsFontSpecProperty.get().withSize(240.0)
        backgroundImageView.fitWidthProperty().addListener { _, _, newValue ->
            updateFont(fitWidth = newValue.toDouble())
        }
        backgroundImageView.fitHeightProperty().addListener { _, _, newValue ->
            updateFont(fitHeight = newValue.toDouble())
        }
        DataHolder.healthPointsFontSpecProperty.addListener { _, _, newValue ->
            updateFont(fontSpec = newValue)
        }

        DataHolder.healthPointsProperty.addListener { _, oldValue, newValue ->
            if (newValue.toInt() == oldValue.toInt()) return@addListener
            if (newValue.toInt() < 0) return@addListener
            if (oldValue.toInt() < 0) return@addListener
            healthPointsAnimation(oldValue.toInt(), newValue.toInt())
        }

        healthPointsLabel.text = DataHolder.healthPointsProperty.value.toString()
    }

    @OptIn(DelicateCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
    private fun loadBackgroundImageWithDelay(
        imageSpec: ImageSpec = DataHolder.healthPointsImageSpecProperty.value,
        requestedWidth: Double = backgroundImageView.fitWidth,
        requestedHeight: Double = backgroundImageView.fitHeight
    ) {
        loadBackgroundImageDelayJob?.cancel()
        loadBackgroundImageDelayJob = GlobalScope.launch {
            delay(1.0.toDuration(DurationUnit.SECONDS))
            loadBackgroundImage(imageSpec, requestedWidth, requestedHeight)
        }
    }

    private fun loadBackgroundImage(
        imageSpec: ImageSpec = DataHolder.healthPointsImageSpecProperty.value,
        requestedWidth: Double = backgroundImageView.fitWidth,
        requestedHeight: Double = backgroundImageView.fitHeight
    ) = runOnUiThread {
        backgroundImageView.image = imageSpec.load(
            requestedWidth = requestedWidth,
            requestedHeight = requestedHeight,
            preserveRatio = true,
            smooth = false
        )
    }

    private fun updateFont(
        fontSpec: FontSpec = DataHolder.healthPointsFontSpecProperty.get(),
        fitWidth: Double = backgroundImageView.fitWidth,
        fitHeight: Double = backgroundImageView.fitHeight
    ) {
        val minSize = minOf(fitWidth, fitHeight)
        healthPointsLabel.font = fontSpec.withSize(minSize * fontSizeFactor)
    }

    private fun healthPointsAnimation(oldHealthPoints: Int, newHealthPoints: Int) {
        val direction = if (oldHealthPoints < newHealthPoints) 1 else -1
        val moveDistance = rootPane.height * 0.2

        val motionBlur = MotionBlur(90.0, 0.0)
        healthPointsLabel.effect = motionBlur

        Timeline(
            KeyFrame(
                Duration(animationDuration / 2),
                KeyValue(motionBlur.radiusProperty(), 45.0, EASE_IN),
                KeyValue(healthPointsLabel.translateYProperty(), moveDistance * direction, EASE_IN),
                KeyValue(healthPointsLabel.opacityProperty(), 0, EASE_IN)
            )
        ).apply {
            setOnFinished {
                healthPointsLabel.translateY = -moveDistance * direction
                healthPointsLabel.text = newHealthPoints.toString()
            }
        }.let { animationQueue.add(it.toQueueItem()) }

        Timeline(
            KeyFrame(
                Duration(animationDuration / 2),
                KeyValue(motionBlur.radiusProperty(), 0.0, EASE_OUT),
                KeyValue(healthPointsLabel.translateYProperty(), 0, EASE_OUT),
                KeyValue(healthPointsLabel.opacityProperty(), 1, EASE_OUT)
            )
        ).apply {
            setOnFinished {
                healthPointsLabel.effect = null
            }
        }.let { animationQueue.add(it.toQueueItem()) }
    }

    override fun close() {
        stage.hide()
    }
}
