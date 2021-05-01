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

import com.github.vatbub.magic.Ability.SortMode
import com.github.vatbub.magic.Ability.SortMode.*
import com.github.vatbub.magic.PreferenceKeys.AbilityKeys
import com.github.vatbub.magic.PreferenceKeys.AbilityKeys.historyEntry

private object Lock

fun Ability.Companion.addToHistory(ability: Ability) = synchronized(Lock) {
    preferences[historyEntry(ability)] = preferences[historyEntry(ability)] + 1
}

fun Ability.Companion.sortedValues(sortMode: SortMode = preferences[AbilityKeys.SortMode]): List<Ability> {
    return when (sortMode) {
        Original -> Ability.values().toList()
        Usage -> {
            val groupedHistory = Ability.values().associateWith { preferences[historyEntry(it)] }
            Ability.sortedValues(Alphabetical).sortedByDescending { groupedHistory[it] }
        }
        Alphabetical -> Ability.values().sortedBy { it.localizedLabel }
    }
}
