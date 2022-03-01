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

import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.kotlin.preferences.Preferences
import com.github.vatbub.kotlin.preferences.PropertiesFileKeyValueProvider
import com.github.vatbub.magic.data.Ability.SortMode.Usage
import com.github.vatbub.magic.data.DayNightState.None
import com.github.vatbub.magic.data.DayNightState.valueOf
import com.github.vatbub.magic.view.BuiltInFontSpecs
import com.github.vatbub.magic.view.BuiltInImageSpecs
import com.github.vatbub.magic.view.FontSpec
import com.github.vatbub.magic.view.ImageSpec
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import java.io.File
import com.github.vatbub.magic.data.Ability.SortMode as SortModeEnum

val preferences = Preferences(PropertiesFileKeyValueProvider(File("magicObsViewSettings.properties")))

object PreferenceKeys {
    object BackgroundColor : ColorKey("backgroundColor", Color.RED)

    object HealthPoints : Key<Int>("healthPoints", 20, { it.toInt() }, { it.toString() })

    object DayNightStateKey :
        Key<DayNightState>("dayNightState", None, { valueOf(it) }, { it.toString() })

    object DayNightMechanicEnabledKey :
        Key<Boolean>("dayNightMechanicEnabled", false, { it.toBooleanStrict() }, { it.toString() })

    object CardDatabaseEnabledKey :
        Key<Boolean>("cardDatabaseEnabled", true, { it.toBooleanStrict() }, { it.toString() })

    object HealthPointsFontColor : ColorKey("healthPointsFontColor", Color.WHITE)

    object HealthPointsFontSpec : FontSpecKey(
        "healthPointsFontSpec",
        BuiltInFontSpecs.MagicTheGathering.fontSpec
    )

    object CardStatisticsFontSpec : FontSpecKey(
        "cardStatisticsFontSpec",
        BuiltInFontSpecs.MagicTheGathering.fontSpec
    )

    object HealthPointsBackgroundImageSpec : ImageSpecKey(
        uniqueName = "healthPointsImageSpec",
        defaultValue = BuiltInImageSpecs.GreenRing.imageSpec
    )

    object RunningInstances : Key<Int>("runningInstances", 0, String::toInt, Int::toString)

    object AbilityKeys {
        object SortMode : Key<SortModeEnum>("abilitySortMode", Usage, { SortModeEnum.valueOf(it) }, { it.toString() })

        fun historyEntry(ability: Ability) =
            Key("abilityHistory.$ability", 0, { it.toInt() }, { it.toString() })
    }

    abstract class ColorKey(uniqueName: String, defaultValue: Color) : Key<Color>(uniqueName, defaultValue, {
        val components = it.split(";")
        Color(
            components[0].toDouble(),
            components[1].toDouble(),
            components[2].toDouble(),
            components[3].toDouble()
        )
    }, { "${it.red};${it.green};${it.blue};${it.opacity}" })

    abstract class FontSpecKey(uniqueName: String, defaultValue: FontSpec) : Key<FontSpec>(uniqueName, defaultValue, {
        val parts = it.split(";")
        when (parts[0]) {
            "BuiltIn" -> BuiltInFontSpecs.valueOf(parts[1]).fontSpec
            "System" -> FontSpec.System(parts[1], parts[2].toFontWeightOrNull(), parts[3].toPostureOrNull())
            else -> throw IllegalArgumentException("Illegal FontSpecType found in preferences")
        }
    }, {
        when (it) {
            is FontSpec.BuiltIn -> "BuiltIn;${BuiltInFontSpecs.forSpec(it)}"
            is FontSpec.System -> "System;${it.family};${it.weight};${it.posture}"
        }
    })

    abstract class ImageSpecKey(uniqueName: String, defaultValue: ImageSpec) :
        Key<ImageSpec>(uniqueName, defaultValue, {
            val parts = it.split(";")
            when (parts[0]) {
                "BuiltIn" -> BuiltInImageSpecs.valueOf(parts[1]).imageSpec
                "System" -> ImageSpec.Custom(File(parts[1]))
                else -> throw IllegalArgumentException("Illegal ImageSpecType found in preferences")
            }
        }, {
            when (it) {
                is ImageSpec.BuiltIn -> "BuiltIn;${BuiltInImageSpecs.forSpec(it)}"
                is ImageSpec.Custom -> "System;${it.file.absolutePath}"
            }
        })

    private fun String.toFontWeightOrNull(): FontWeight? = when (this) {
        "null" -> null
        else -> FontWeight.valueOf(this)
    }

    private fun String.toPostureOrNull(): FontPosture? = when (this) {
        "null" -> null
        else -> FontPosture.valueOf(this)
    }
}
