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
package com.github.vatbub.magic

import com.github.vatbub.magic.PreferenceKeys.BackgroundColor
import com.github.vatbub.magic.PreferenceKeys.HealthPoints
import com.github.vatbub.magic.util.PermutatingObservableList
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.paint.Color

object DataHolder {
    val backgroundColorProperty: ObjectProperty<Color> = SimpleObjectProperty(preferences[BackgroundColor]).apply {
        addListener { _, _, newValue ->
            preferences[BackgroundColor] = newValue
        }
    }

    val healthPointsProperty: IntegerProperty = SimpleIntegerProperty(preferences[HealthPoints]).apply {
        addListener { _, _, newValue ->
            preferences[HealthPoints] = newValue.toInt()
        }
    }

    val cardList: ObservableList<Card> = PermutatingObservableList(mutableListOf())
}
