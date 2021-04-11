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
