package com.github.vatbub.magic.animation.queue

class CodeBlockQueueItem(private val block: () -> Unit) : QueueItem<CodeBlockQueueItem> {
    override fun play(onFinished: (CodeBlockQueueItem) -> Unit) {
        block()
        onFinished(this)
    }
}
