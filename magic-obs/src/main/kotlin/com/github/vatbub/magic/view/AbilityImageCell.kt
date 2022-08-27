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
package com.github.vatbub.magic.view

import com.github.vatbub.magic.data.Ability
import com.github.vatbub.magic.data.CardObjectNoNullables
import javafx.geometry.Orientation
import javafx.scene.control.TableCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.FlowPane


class AbilityImageCell(private val abilityIconSize: Double = 20.0) : TableCell<CardObjectNoNullables, List<Ability>>() {
    override fun updateItem(item: List<Ability>?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || item == null) {
            text = null
            graphic = null
            return
        }

        val imageViews = item
            .map { AbilityImageCell::class.java.getResourceAsStream("AbilityIcons/${it.imageFileName}.png")!! }
            .map { Image(it, abilityIconSize, abilityIconSize, true, false) }
            .map { ImageView(it) }
        graphic = FlowPane(Orientation.HORIZONTAL, 5.0, 2.0, *imageViews.toTypedArray())
    }
}
