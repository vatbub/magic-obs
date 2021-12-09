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

import com.github.vatbub.magic.data.PreferenceKeys.AbilityKeys
import com.github.vatbub.magic.data.PreferenceKeys.BackgroundColor
import com.github.vatbub.magic.data.PreferenceKeys.CardStatisticsFontSpec
import com.github.vatbub.magic.data.PreferenceKeys.DayNightMechanicEnabledKey
import com.github.vatbub.magic.data.PreferenceKeys.DayNightStateKey
import com.github.vatbub.magic.data.PreferenceKeys.HealthPoints
import com.github.vatbub.magic.data.PreferenceKeys.HealthPointsBackgroundImageSpec
import com.github.vatbub.magic.data.PreferenceKeys.HealthPointsFontColor
import com.github.vatbub.magic.data.PreferenceKeys.HealthPointsFontSpec
import com.github.vatbub.magic.util.PermutatingObservableList
import com.github.vatbub.magic.view.FontSpec
import com.github.vatbub.magic.view.ImageSpec
import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.scene.paint.Color

object DataHolder {
    val backgroundColorProperty: ObjectProperty<Color> = preferences.property(BackgroundColor)

    val healthPointsFontColorProperty: ObjectProperty<Color> = preferences.property(HealthPointsFontColor)

    val healthPointsFontSpecProperty: ObjectProperty<FontSpec> = preferences.property(HealthPointsFontSpec)

    val cardStatisticsFontSpecProperty: ObjectProperty<FontSpec> = preferences.property(CardStatisticsFontSpec)

    val healthPointsImageSpecProperty: ObjectProperty<ImageSpec> = preferences.property(HealthPointsBackgroundImageSpec)

    val healthPointsProperty: IntegerProperty = preferences.property(HealthPoints)

    val abilitySortModeProperty: ObjectProperty<Ability.SortMode> = preferences.property(AbilityKeys.SortMode)

    val dayNightState: ObjectProperty<DayNightState> = preferences.property(DayNightStateKey)

    val dayNightMechanicEnabled: BooleanProperty = preferences.property(DayNightMechanicEnabledKey)

    val cardList: ObservableList<Card> = PermutatingObservableList(mutableListOf())

    fun resetGame() {
        healthPointsProperty.value = HealthPoints.defaultValue
        dayNightState.value = DayNightStateKey.defaultValue
        cardList.clear()
    }
}
