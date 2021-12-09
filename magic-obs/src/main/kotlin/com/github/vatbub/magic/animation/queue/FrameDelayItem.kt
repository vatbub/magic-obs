/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
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

import javafx.application.Platform

class FrameDelayItem(private val numberOfFrames: Int = 1) : QueueItem<FrameDelayItem> {
    override fun play(onFinished: (FrameDelayItem) -> Unit) =
        awaitFrameUpdates(numberOfFrames) { onFinished(this) }
}

private fun awaitFrameUpdates(numberOfFrames: Int, block: () -> Unit): Unit =
    if (numberOfFrames == 0) block()
    else Platform.runLater { awaitFrameUpdates(numberOfFrames - 1, block) }

fun AnimationQueue.addFrameDelay(numberOfFrames: Int = 1) {
    add(FrameDelayItem(numberOfFrames))
}
