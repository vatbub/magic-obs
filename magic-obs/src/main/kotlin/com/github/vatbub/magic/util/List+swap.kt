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
package com.github.vatbub.magic.util

import com.github.vatbub.magic.data.Permutation
import java.util.*

fun <T> MutableList<T>.swap(i: Int, j: Int): List<T> = when (this) {
    is PermutatingObservableList -> this.apply {
        this.permute(Permutation(oldIndex = i, newIndex = j))
    }
    else -> this.apply {
        Collections.swap(this, i, j)
    }
}
