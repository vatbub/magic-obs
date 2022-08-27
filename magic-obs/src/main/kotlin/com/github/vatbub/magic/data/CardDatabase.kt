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
package com.github.vatbub.magic.data

object CardDatabase {
    @Suppress("SENSELESS_COMPARISON")
    val cardObjects by lazy {
        CardDatabaseParser
            .parse()
            // Might actually be nullable (Instants, Sorcery have no power or toughness),
            // but we don't want them in our list
            // and also don't want to pollute the type with the nullable marker
            .filter { it.power != null }
            .filter { it.toughness != null }
            .map { it.fixNulls() }
            .distinct()
    }
}

data class CardObject(
    val name: String,
    val card_faces: List<CardFace>?,
    val lang: Language,
    val type_line: String,
    val power: String,
    val toughness: String,
    val colors: List<ManaColor>,
    val color_identity: List<ManaColor>,
    val keywords: List<String>?
) {
    fun fixNulls() = CardObjectNoNullables(
        name,
        card_faces?.map { it.fixNulls() } ?: listOf(),
        lang,
        type_line,
        power,
        toughness,
        colors,
        color_identity,
        keywords ?: listOf(),
    )
}

data class CardObjectNoNullables(
    val name: String,
    val card_faces: List<CardFaceLessNullables>,
    val lang: Language,
    val type_line: String,
    val power: String,
    val toughness: String,
    val colors: List<ManaColor>,
    val color_identity: List<ManaColor>,
    val keywords: List<String>
) {
    val abilities = keywords.mapNotNull { it.toAbility() }

    fun toOverlayCard() = Card(
        attack = power.toDoubleOrNull() ?: 1.0,
        defense = toughness.toDoubleOrNull() ?: 1.0,
        counter = 0,
        abilities = abilities.toSet()
    )
}

enum class Language {
    en,
    es,
    fr,
    de,
    it,
    pt,
    ja,
    ko,
    ru,
    zhs,
    zht,
    he,
    la,
    grc,
    ar,
    sa,
    ph,
}

enum class ManaColor(val apiString: String) {
    White("W"),
    Blue("U"),
    Black("B"),
    Red("R"),
    Green("G");

    companion object {
        fun fromApiString(apiString: String) = values().first { it.apiString == apiString }
        fun fromArray(iterable: Iterable<String>) = iterable.map { fromApiString(it) }
    }
}

data class CardFace(
    val name: String,
    val type_line: String,
    val power: String?,
    val toughness: String?,
    val colors: List<ManaColor>?,
    val color_identity: List<ManaColor>?,
    val keywords: List<String>?
) {
    fun fixNulls() = CardFaceLessNullables(
        name,
        type_line,
        power,
        toughness,
        colors ?: listOf(),
        color_identity ?: listOf(),
        keywords ?: listOf(),
    )
}

data class CardFaceLessNullables(
    val name: String,
    val type_line: String,
    val power: String?,
    val toughness: String?,
    val colors: List<ManaColor>,
    val color_identity: List<ManaColor>,
    val keywords: List<String>
)

private fun String.toAbility(): Ability? = when (this) {
    "Explore" -> null
    "Parley" -> null
    else -> Ability.values().firstOrNull { it.name == this }
}
