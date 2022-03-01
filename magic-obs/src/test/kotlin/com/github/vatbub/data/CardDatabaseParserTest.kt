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
package com.github.vatbub.data

import com.github.vatbub.magic.data.Ability
import com.github.vatbub.magic.data.CardDatabase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CardDatabaseParserTest {
    @Test
    fun parsesCorrectly() {
        val cardObjects = CardDatabase.cardObjects
        assertTrue(cardObjects.isNotEmpty())

        cardObjects.forEach { card ->
            // println("Asserting card ${card.name} ...")
            assertTrue(card.name.isNotEmpty())

            card.card_faces.forEach { face ->
                assertNotNull(face)
                assertTrue(face.name.isNotEmpty())
                assertTrue(face.type_line.isNotEmpty())
                face.power?.let { assertTrue(it.isValidPowerToughnessNumber()) }
                face.toughness?.let { assertTrue(it.isValidPowerToughnessNumber()) }
                face.colors.forEach { color -> assertNotNull(color) }
                face.color_identity.forEach { color -> assertNotNull(color) }
                face.keywords.forEach { assertTrue(it.isNotEmpty()) }
            }

            assertTrue(card.type_line.isNotEmpty())
            assertTrue(card.power.isValidPowerToughnessNumber())
            assertTrue(card.toughness.isValidPowerToughnessNumber())
            card.colors.forEach { color -> assertNotNull(color) }
            card.color_identity.forEach { color -> assertNotNull(color) }

            card.keywords.forEach { assertTrue(it.isNotEmpty()) }
        }
    }

    @Test
    fun conversionToDataCardsWorks() {
        val cardObjects = CardDatabase.cardObjects
        val convertedCards = cardObjects.associateWith { it.toOverlayCard() }
        convertedCards.forEach { (cardObject, convertedCard) ->
            assertEquals(cardObject.power.toDoubleOrNull() ?: 1.0, convertedCard.attackProperty.value)
            assertEquals(cardObject.toughness.toDoubleOrNull() ?: 1.0, convertedCard.defenseProperty.value)
            assertEquals(0, convertedCard.counterProperty.value)

            convertedCard.abilities.forEach { ability ->
                assertTrue(cardObject.keywords.contains(ability.keywordValue))
            }
        }
    }

    private fun String.isValidPowerToughnessNumber() = when (this) {
        "*" -> true
        "1+*" -> true
        "*+1" -> true
        "2+*" -> true
        "7-*" -> true
        "?" -> true
        "∞" -> true
        "*²" -> true
        else -> toDoubleOrNull() != null
    }.also { if (!it) println("Unrecognized value was \"$this\"") }

    private val Ability.keywordValue
        get() = this.name
}
