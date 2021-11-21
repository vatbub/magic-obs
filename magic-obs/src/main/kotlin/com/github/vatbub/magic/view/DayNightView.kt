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
import com.github.vatbub.magic.data.DayNightState
import com.github.vatbub.magic.data.DayNightState.*
import com.github.vatbub.magic.util.asBackgroundStyle
import com.github.vatbub.magic.util.bindAndMap
import com.github.vatbub.magic.util.runOnUiThread
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
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


class DayNightView : Closeable {
    companion object {
        fun show(): DayNightView {
            val stage = Stage(StageStyle.UNIFIED)

            val fxmlLoader = FXMLLoader(DayNightView::class.java.getResource("DayNightView.fxml"))
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<DayNightView>()
            controllerInstance.stage = stage

            val scene = Scene(root)

            stage.title = "Magic OBS Day Night"
            // stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()
            return controllerInstance
        }

        private const val fadeAnimationDuration = 500.0
        private const val flipAnimationDuration = 1000.0
    }

    lateinit var stage: Stage
        private set

    @FXML
    private lateinit var rootPane: AnchorPane

    @FXML
    private lateinit var dayView: ImageView

    @FXML
    private lateinit var nightView: ImageView

    @FXML
    private lateinit var noneView: ImageView

    private val animationQueue = AnimationQueue()

    private val loadBackgroundImageDelayJobs = mutableMapOf<(Double, Double) -> Unit, Job>()

    @FXML
    fun initialize() {
        rootPane.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)

        dayView.fitWidthProperty().bind(rootPane.widthProperty())
        dayView.fitHeightProperty().bind(rootPane.heightProperty())
        nightView.fitWidthProperty().bind(rootPane.widthProperty())
        nightView.fitHeightProperty().bind(rootPane.heightProperty())
        noneView.fitWidthProperty().bind(rootPane.widthProperty())
        noneView.fitHeightProperty().bind(rootPane.heightProperty())

        dayView.fitWidthProperty().addListener { _, _, newValue ->
            loadImageWithDelay(requestedWidth = newValue.toDouble(), loadMethod = this::loadDayImage)
        }
        nightView.fitWidthProperty().addListener { _, _, newValue ->
            loadImageWithDelay(requestedWidth = newValue.toDouble(), loadMethod = this::loadNightImage)
        }
        noneView.fitWidthProperty().addListener { _, _, newValue ->
            loadImageWithDelay(requestedWidth = newValue.toDouble(), loadMethod = this::loadNoneImage)
        }
        dayView.fitHeightProperty().addListener { _, _, newValue ->
            loadImageWithDelay(requestedHeight = newValue.toDouble(), loadMethod = this::loadDayImage)
        }
        nightView.fitHeightProperty().addListener { _, _, newValue ->
            loadImageWithDelay(requestedHeight = newValue.toDouble(), loadMethod = this::loadNightImage)
        }
        noneView.fitHeightProperty().addListener { _, _, newValue ->
            loadImageWithDelay(requestedHeight = newValue.toDouble(), loadMethod = this::loadNoneImage)
        }

        loadImageWithDelay(loadMethod = this::loadDayImage)
        loadImageWithDelay(loadMethod = this::loadNightImage)
        loadImageWithDelay(loadMethod = this::loadNoneImage)

        transitionImages(None, DataHolder.dayNightState.value)
        DataHolder.dayNightState.addListener { _, oldValue, newValue ->
            transitionImages(oldValue, newValue)
        }
    }


    @OptIn(DelicateCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
    private fun loadImageWithDelay(
        requestedWidth: Double = dayView.fitWidth,
        requestedHeight: Double = dayView.fitHeight,
        loadMethod: (Double, Double) -> Unit
    ) {
        loadBackgroundImageDelayJobs[loadMethod]?.cancel()
        loadBackgroundImageDelayJobs[loadMethod] = GlobalScope.launch {
            delay(1.0.toDuration(DurationUnit.SECONDS))
            loadMethod(requestedWidth, requestedHeight)
        }
    }

    private fun loadDayImage(
        requestedWidth: Double = dayView.fitWidth,
        requestedHeight: Double = dayView.fitHeight
    ) = runOnUiThread {
        dayView.image = javaClass
            .getResourceAsStream("Day.png")!!
            .let { Image(it, requestedWidth, requestedHeight, true, false) }
    }

    private fun loadNightImage(
        requestedWidth: Double = dayView.fitWidth,
        requestedHeight: Double = dayView.fitHeight
    ) = runOnUiThread {
        nightView.image = javaClass
            .getResourceAsStream("Night.png")!!
            .let { Image(it, requestedWidth, requestedHeight, true, false) }
    }

    private fun loadNoneImage(
        requestedWidth: Double = dayView.fitWidth,
        requestedHeight: Double = dayView.fitHeight
    ) = runOnUiThread {
        noneView.image = javaClass
            .getResourceAsStream("NoDayNight.png")!!
            .let { Image(it, requestedWidth, requestedHeight, true, false) }
    }

    private fun transitionImages(oldState: DayNightState, newState: DayNightState) {
        if ((oldState == None && newState == Day) || (oldState == Day && newState == None))
            fadeTransition(newState)
        else
            flipTransition(newState)
    }

    private fun fadeTransition(newState: DayNightState) {
        Timeline(
            KeyFrame(
                Duration(fadeAnimationDuration),
                KeyValue(dayView.opacityProperty(), if (newState == Day) 1.0 else 0.0),
                KeyValue(nightView.opacityProperty(), if (newState == Night) 1.0 else 0.0)
            )
        ).let { animationQueue.add(it.toQueueItem()) }
    }

    private fun flipTransition(newState: DayNightState) {
        val currentScaleX = dayView.scaleX
        Timeline(
            KeyFrame(
                Duration((flipAnimationDuration / 2.0) - 0.1),
                KeyValue(dayView.opacityProperty(), dayView.opacity),
                KeyValue(nightView.opacityProperty(), nightView.opacity)
            ),
            KeyFrame(
                Duration(flipAnimationDuration / 2.0),
                KeyValue(dayView.scaleXProperty(), 0, Interpolator.EASE_BOTH),
                KeyValue(nightView.scaleXProperty(), 0, Interpolator.EASE_BOTH),
                KeyValue(noneView.scaleXProperty(), 0, Interpolator.EASE_BOTH),
            ),
            KeyFrame(
                Duration((flipAnimationDuration / 2.0) + 0.1),
                KeyValue(dayView.opacityProperty(), if (newState == Day) 1.0 else 0.0),
                KeyValue(nightView.opacityProperty(), if (newState == Night) 1.0 else 0.0)
            ),
            KeyFrame(
                Duration(flipAnimationDuration),
                KeyValue(dayView.scaleXProperty(), currentScaleX, Interpolator.EASE_BOTH),
                KeyValue(nightView.scaleXProperty(), currentScaleX, Interpolator.EASE_BOTH),
                KeyValue(noneView.scaleXProperty(), currentScaleX, Interpolator.EASE_BOTH),
            )
        ).let { animationQueue.add(it.toQueueItem()) }
    }

    override fun close() {
        stage.hide()
    }
}
