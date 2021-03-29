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
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration


class HealthPointsView {
    companion object {
        fun show(): HealthPointsView {
            val stage = Stage(StageStyle.UNDECORATED)

            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("HealthPointsView.fxml"))
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<HealthPointsView>()
            controllerInstance.stage = stage

            val scene = Scene(root)

            stage.title = "Magic OBS Health Points"
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
    private lateinit var rootPane: AnchorPane

    @FXML
    private lateinit var healthPointsLabel: Label

    private val animationQueue = AnimationQueue()

    @FXML
    fun initialize() {
        healthPointsLabel.font = Fonts.magic(240.0)

        rootPane.styleProperty().bindAndMap(DataHolder.backgroundColorProperty, Color::asBackgroundStyle)

        DataHolder.healthPointsProperty.addListener { _, oldValue, newValue ->
            if (newValue.toInt() == oldValue.toInt()) return@addListener
            if (newValue.toInt() < 0) return@addListener
            if (oldValue.toInt() < 0) return@addListener
            healthPointsAnimation(oldValue.toInt(), newValue.toInt())
        }

        healthPointsLabel.text = DataHolder.healthPointsProperty.value.toString()
    }

    private fun healthPointsAnimation(oldHealthPoints: Int, newHealthPoints: Int) {
        val direction = if (oldHealthPoints < newHealthPoints) 1 else -1
        val moveDistance = rootPane.height * 0.2

        val motionBlur = MotionBlur(90.0, 0.0)
        healthPointsLabel.effect = motionBlur

        val motionBlurKeyValue1 = KeyValue(motionBlur.radiusProperty(), 45.0, EASE_IN)
        val positionKeyValue1 = KeyValue(healthPointsLabel.translateYProperty(), moveDistance * direction, EASE_IN)
        val opacityKeyValue1 = KeyValue(healthPointsLabel.opacityProperty(), 0, EASE_IN)
        val keyFrame1 =
            KeyFrame(Duration(animationDuration / 2), motionBlurKeyValue1, positionKeyValue1, opacityKeyValue1)

        animationQueue.add(Timeline(keyFrame1).apply {
            setOnFinished {
                healthPointsLabel.translateY = -moveDistance * direction
                healthPointsLabel.text = newHealthPoints.toString()
            }
        })


        val motionBlurKeyValue2 = KeyValue(motionBlur.radiusProperty(), 0.0, EASE_OUT)
        val positionKeyValue2 = KeyValue(healthPointsLabel.translateYProperty(), 0, EASE_OUT)
        val opacityKeyValue2 = KeyValue(healthPointsLabel.opacityProperty(), 1, EASE_OUT)
        val keyFrame2 =
            KeyFrame(Duration(animationDuration / 2), motionBlurKeyValue2, positionKeyValue2, opacityKeyValue2)

        animationQueue.add(Timeline(keyFrame2).apply {
            setOnFinished {
                healthPointsLabel.effect = null
            }
        })
    }
}
