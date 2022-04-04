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
package com.github.vatbub.magic.common

import com.github.vatbub.kotlin.preferences.Key
import com.github.vatbub.kotlin.preferences.Preferences
import com.github.vatbub.kotlin.preferences.PropertiesFileKeyValueProvider
import jfxtras.styles.jmetro.Style
import java.io.File
import kotlin.properties.Delegates

var preferenceFolder by Delegates.observable(File(System.getProperty("user.home"))) { _, _, newValue ->
    preferences = loadPreferences(newValue)
}

var preferences: Preferences = loadPreferences(preferenceFolder)
    private set

private fun loadPreferences(preferenceFolder: File): Preferences {
    val file = File(preferenceFolder, "magicObsViewSettings.properties")
    println("Reading preferences from ${file.absolutePath}...")
    return Preferences(PropertiesFileKeyValueProvider(file))
}

object CommonPreferenceKeys {
    object UIStyle : Key<Style>("uiStyle", Style.DARK, { Style.valueOf(it) }, { it.name })
}
