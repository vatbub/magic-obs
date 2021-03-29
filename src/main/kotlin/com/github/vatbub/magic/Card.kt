package com.github.vatbub.magic

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty

class Card(attack: Int = 1, defense: Int = 1, tapped: Boolean = false, flying: Boolean = false) {
    val attackProperty: IntegerProperty = SimpleIntegerProperty(attack)
    val defenseProperty: IntegerProperty = SimpleIntegerProperty(defense)
    val tappedProperty: BooleanProperty = SimpleBooleanProperty(tapped)
    val flyingProperty: BooleanProperty = SimpleBooleanProperty(flying)
}
