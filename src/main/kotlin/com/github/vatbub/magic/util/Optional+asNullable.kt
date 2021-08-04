package com.github.vatbub.magic.util

import java.util.*

fun <T> Optional<T>.asNullable(): T? = if (this.isPresent) this.get() else null
