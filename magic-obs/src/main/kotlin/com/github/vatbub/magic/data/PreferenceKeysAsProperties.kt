package com.github.vatbub.magic.data

import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.kotlin.preferences.Preferences
import javafx.beans.property.*

fun Preferences.property(key: Key<Boolean>): BooleanProperty = SimpleBooleanProperty(this[key]).apply {
    addListener { _, _, newValue ->
        this@property[key] = newValue
    }
}

fun Preferences.property(key: Key<Int>): IntegerProperty = SimpleIntegerProperty(this[key]).apply {
    addListener { _, _, newValue ->
        this@property[key] = newValue.toInt()
    }
}

fun <T> Preferences.property(key: Key<T>): ObjectProperty<T> = SimpleObjectProperty(this[key]).apply {
    addListener { _, _, newValue ->
        this@property[key] = newValue
    }
}
