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

import javafx.collections.ListChangeListener

data class Permutation(val oldIndex: Int, val newIndex: Int) {
    override fun toString() = "oldIndex: $oldIndex, newIndex: $newIndex"
}

val ListChangeListener.Change<*>.permutations: List<Permutation>
    get() {
        require(wasPermutated())
        val result = mutableMapOf<Int, Int>()
        (from until to).forEach { oldIndex ->
            val newIndex = getPermutation(oldIndex)
            if (result.containsKey(newIndex)) return@forEach
            result[oldIndex] = newIndex
        }
        return result.map { Permutation(it.key, it.value) }
    }
