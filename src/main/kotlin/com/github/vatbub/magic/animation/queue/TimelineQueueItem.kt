package com.github.vatbub.magic.animation.queue

import javafx.animation.Timeline

class TimelineQueueItem(private val timeline: Timeline) : QueueItem<TimelineQueueItem> {
    override fun play(onFinished: (TimelineQueueItem) -> Unit) {
        val previousOnFinished = timeline.onFinished
        timeline.setOnFinished { event ->
            previousOnFinished?.handle(event)
            onFinished(this)
        }
        timeline.play()
    }
}

fun Timeline.toQueueItem() = TimelineQueueItem(this)
