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

import com.github.vatbub.magic.Permutation
import javafx.collections.ModifiableObservableListBase
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * See https://stackoverflow.com/a/38918795/5736633
 * @author James_D
 */
class PermutatingObservableList<E>(private val source: MutableList<E>) :
    ModifiableObservableListBase<E>() {
    fun permute(permutation: Permutation) {
        val permutationArray = (0 until size).toMutableList()
        permutationArray.swap(permutation.oldIndex, permutation.newIndex)
        this.permute(permutationArray.toIntArray())
    }

    fun permute(permutation: IntArray) {
        checkPermutation(permutation)
        beginChange()
        val temp: List<E> = ArrayList(source)
        for (i in 0 until size) {
            source[i] = temp[permutation[i]]
        }
        nextPermutation(0, size, permutation)
        endChange()
    }

    fun pairwiseSwap() {
        val permutation = IntArray(size)
        var i = 0
        while (i + 1 < permutation.size) {
            permutation[i] = i + 1
            permutation[i + 1] = i
            i += 2
        }
        if (permutation.size % 2 == 1) {
            permutation[permutation.size - 1] = permutation.size - 1
        }
        permute(permutation)
    }

    private fun checkPermutation(permutation: IntArray) {
        var valid = permutation.size == size
        val values = IntStream.range(0, size).boxed().collect(Collectors.toSet())
        var i = 0
        while (i < permutation.size && valid) {
            valid = values.remove(permutation[i])
            i++
        }
        require(valid) {
            """Invalid permutation: ${permutation.contentToString()}
    Permutation must be same length as list and must contain each of the values 0-${size - 1} exactly once"""
        }
    }

    override fun get(index: Int): E {
        return source[index]
    }

    override val size: Int
        get() = source.size

    override fun doAdd(index: Int, element: E) {
        source.add(index, element)
    }

    override fun doSet(index: Int, element: E): E {
        return source.set(index, element)
    }

    override fun doRemove(index: Int): E {
        return source.removeAt(index)
    }
}
