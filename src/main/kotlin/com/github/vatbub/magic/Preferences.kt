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

import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.kotlin.preferences.Preferences
import com.github.vatbub.kotlin.preferences.PropertiesFileKeyValueProvider
import javafx.scene.paint.Color
import java.io.File

val preferences = Preferences(PropertiesFileKeyValueProvider(File("magicObsViewSettings.properties")))

object PreferenceKeys {
    object BackgroundColor : Key<Color>("backgroundColor", Color.RED, {
        val components = it.split(";")
        Color(
            components[0].toDouble(),
            components[1].toDouble(),
            components[2].toDouble(),
            components[3].toDouble()
        )
    }, { "${it.red};${it.green};${it.blue};${it.opacity}" })

    object HealthPoints : Key<Int>("healthPoints", 20, { it.toInt() }, { it.toString() })
}
