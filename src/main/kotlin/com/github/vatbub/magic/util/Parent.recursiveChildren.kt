package com.github.vatbub.magic.util

import javafx.scene.Node
import javafx.scene.Parent

val Node.recursiveChildren
    get() = if (this is Parent) recursiveChildren else listOf()

val Parent.recursiveChildren: List<Node>
    get() = childrenUnmodifiable
        .toTypedArray()
        .let { mutableListOf(*it) }
        .also { result ->
            childrenUnmodifiable
                .filterIsInstance<Parent>()
                .let { children -> result.addAll(children) }
        }
