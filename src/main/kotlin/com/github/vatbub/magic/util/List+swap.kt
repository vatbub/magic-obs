package com.github.vatbub.magic.util

import java.util.*

inline fun <T> MutableList<T>.swap(i: Int, j: Int): List<T> = this.apply {
    Collections.swap(this, i, j)
}
