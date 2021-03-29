package com.github.vatbub.magic.util

import javafx.beans.property.Property
import javafx.beans.value.ObservableValue

fun <In, Out> Property<Out>.bindAndMap(observable: ObservableValue<In>, block: (In) -> Out) {
    this.value = block(observable.value)
    observable.addListener { _, _, newValue ->
        this.value = block(newValue)
    }
}
