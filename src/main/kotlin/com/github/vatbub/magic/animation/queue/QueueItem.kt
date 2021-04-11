package com.github.vatbub.magic.animation.queue

interface QueueItem<T : QueueItem<T>> {
    fun play(onFinished: (T) -> Unit)
}
