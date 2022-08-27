/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2019 - 2022 Frederik Kammel
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


class FuzzySearch(private val dictionaryValues: List<String>) {
    private val ABC = "abcdefghijklmnopqrstuvwxyz"
    private val dictionary = dictionaryValues
        .map { it.lowercase() }
        .groupBy { it }
        .mapValues { it.value.size }

    private fun getDeletesReplacesInsertsAndTransposes(word: String): List<String> {
        val deletes = word.indices
            .map { word.substring(0, it) + word.substring(it + 1) }
        val replaces = word.indices
            .flatMap { index ->
                ABC.map { char -> word.substring(0, index) + char + word.substring(index + 1) }
            }
        val inserts = (0 until word.length + 1).flatMap { index ->
            ABC.map { char ->
                word.substring(0, index) + char + word.substring(index)
            }
        }
        val transposes = (0 until word.length - 1).map { index ->
            word.substring(0, index) + word.substring(index + 1, index + 2) + word[index] + word.substring(index + 2)
        }
        return listOf(deletes, replaces, inserts, transposes).flatten()
    }

    private fun correct(word: String): String {
        if (dictionary.containsKey(word)) return word

        val e1 = getDeletesReplacesInsertsAndTransposes(word)
            .filterKnownWords()
            .maxWithOrNull(Comparator.comparingInt { a: String -> dictionary[a]!! })

        if (e1 != null) return e1

        return getDeletesReplacesInsertsAndTransposes(word)
            .map { obj -> getDeletesReplacesInsertsAndTransposes(obj) }
            .flatten()
            .filterKnownWords()
            .maxWithOrNull(Comparator.comparingInt { a: String -> dictionary[a]!! }) ?: word
    }

    private fun List<String>.filterKnownWords() =
        filter { word: String -> dictionary.containsKey(word) }
}
