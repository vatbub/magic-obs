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
