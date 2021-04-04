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

import com.github.vatbub.magic.util.runOnUiThread
import javafx.animation.Timeline

class AnimationQueue {
    private val currentQueue = mutableListOf<Timeline>()

    fun add(timeline: Timeline) = runOnUiThread {
        currentQueue.add(timeline.wrapOnFinished())
        if (currentQueue.size == 1)
            playNextIfApplicable()
    }

    fun runInQueue(block: () -> Unit) = add(Timeline().apply { setOnFinished { block() } })

    private fun Timeline.wrapOnFinished() = apply {
        val previousOnFinished = onFinished
        setOnFinished { event ->
            previousOnFinished?.handle(event)
            currentQueue.remove(this)
            playNextIfApplicable()
        }
    }

    private fun playNextIfApplicable() = currentQueue
        .firstOrNull()
        ?.play()
}
