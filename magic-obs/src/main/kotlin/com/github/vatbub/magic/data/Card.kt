/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2016 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.magic.data

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableSet

class Card(attack: Double = 1.0, defense: Double = 1.0, counter: Int = 0, abilities: Set<Ability> = setOf()) {
    val attackProperty: DoubleProperty = SimpleDoubleProperty(attack)
    val defenseProperty: DoubleProperty = SimpleDoubleProperty(defense)
    val counterProperty: IntegerProperty = SimpleIntegerProperty(counter)
    val abilities: ObservableSet<Ability> = FXCollections.observableSet(abilities.toMutableSet())
}

fun Card.copy(
    attack: Double = attackProperty.value,
    defense: Double = defenseProperty.value,
    counter: Int = counterProperty.value,
    abilities: Set<Ability> = this.abilities
) = Card(
    attack = attack,
    defense = defense,
    counter = counter,
    abilities = abilities
)
