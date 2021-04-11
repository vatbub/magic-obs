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
package com.github.vatbub.magic.animation.queue

import javafx.animation.Timeline

class ConcurrentTimelineQueueItem(private val timelines: List<Timeline>) : QueueItem<ConcurrentTimelineQueueItem> {
    constructor(vararg timelines: Timeline) : this(timelines.toList())

    override fun play(onFinished: (ConcurrentTimelineQueueItem) -> Unit) {
        val finishedStates = timelines.associateWith { false }.toMutableMap()
        timelines.forEach { timeline ->
            val previousOnFinished = timeline.onFinished
            timeline.setOnFinished { event ->
                previousOnFinished?.handle(event)
                finishedStates[timeline] = true

                if (finishedStates.values.all { it }) onFinished(this)
            }
        }
        timelines.forEach { it.play() }
    }
}
