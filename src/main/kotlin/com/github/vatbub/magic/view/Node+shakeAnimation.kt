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

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.util.Duration
import kotlin.random.Random


fun Node.shakeAnimation(
    duration: Duration,
    fps: Int = 25,
    maxTranslationX: Double = 10.0,
    maxTranslationY: Double = 10.0
): Timeline {
    val numberOfFrames = (fps * duration.toSeconds()).toInt()
    val frames = (0 until numberOfFrames)
        .map { frameNumber ->
            val translationX = Random.nextDouble(-maxTranslationX, maxTranslationX)
            val translationY = Random.nextDouble(-maxTranslationY, maxTranslationY)
            KeyFrame(
                Duration(frameNumber * 1000.0 / fps),
                KeyValue(translateXProperty(), translationX),
                KeyValue(translateYProperty(), translationY)
            )
        }.toMutableList()

    KeyFrame(
        Duration((numberOfFrames + 1) * 1000.0 / fps),
        KeyValue(translateXProperty(), 0),
        KeyValue(translateYProperty(), 0)
    ).let { frames.add(it) }

    return Timeline(*frames.toTypedArray())
}
