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

enum class Ability(imageFileName: String? = null, translationKey: String? = null) {
    Annihilator, CantBeBlocked, CantBlock, Deathtouch, Defender, DoesntUntap, DoubleFacedCard, DoubleStrike, Exile,
    FirstStrike, Flying, ForestWalk, Haste, Hexproof, Indestructible, Intimidate, Lifelink, Menace, MustAttack,
    Planeswalker, Protection, Reach, Regenerate, Renown, TemporaryControl, Token, Trample, Undying, Vigilance, Rally,
    Ingest, Cohort, Delirium, Skulk;

    val imageFileName = imageFileName ?: toString()
    val translationKey = translationKey ?: toString()

    enum class SortMode {
        Original, Usage, Alphabetical
    }

    companion object
}
