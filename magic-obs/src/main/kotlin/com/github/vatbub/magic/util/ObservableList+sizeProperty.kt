package com.github.vatbub.magic.util

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.lang.ref.WeakReference
import java.util.*

private val properties = WeakHashMap<ObservableList<*>, WeakReference<IntegerProperty>>()

fun ObservableList<*>.sizeProperty(): IntegerProperty {
    properties[this]?.get()?.let { return it }

    return SimpleIntegerProperty(this.size)
        .also { property ->
            addListener { change: ListChangeListener.Change<*> ->
                property.set(change.list.size)
            }
        }
        .also { properties[this] = WeakReference(it) }
}
