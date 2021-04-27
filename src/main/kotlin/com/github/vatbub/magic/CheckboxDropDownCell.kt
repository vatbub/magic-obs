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

import com.github.vatbub.magic.util.get
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.TableCell
import javafx.util.StringConverter
import org.controlsfx.control.CheckComboBox
import kotlin.properties.Delegates


class CheckboxDropDownCell : TableCell<Card, Card>() {
    private val dropDown = CheckComboBox(FXCollections.observableArrayList(*Ability.values())).apply {
        checkModel.checkedItems.addListener(ListChangeListener { change ->
            val currentCard = currentCard ?: return@ListChangeListener
            while (change.next()) {
                currentCard.abilities.removeAll(change.removed)
                currentCard.abilities.addAll(change.addedSubList)
            }
        })

        converter = object : StringConverter<Ability>() {
            override fun toString(ability: Ability): String = App.resourceBundle["ability.${ability.translationKey}"]

            override fun fromString(string: String): Ability = Ability.valueOf(string)
        }
    }

    private var currentCard: Card? by Delegates.observable(null) { _, oldValue, newValue ->
        oldValue?.abilities?.removeListener(abilitiesChangeListener)
        newValue?.abilities?.addListener(abilitiesChangeListener)

        Platform.runLater {
            dropDown.checkModel.clearChecks()
        }
    }

    private val abilitiesChangeListener = ListChangeListener<Ability> { change ->
        while (change.next()) {
            while (change.next()) {
                dropDown.checkModel.checkedItems.removeAll(change.removed)
                dropDown.checkModel.checkedItems.addAll(change.addedSubList)
            }
        }
    }

    override fun updateItem(item: Card?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            graphic = null
            currentCard = null
            return
        }

        currentCard = tableRow.item
        graphic = dropDown
        Platform.runLater {
            if (tableColumn.width < dropDown.width)
                tableColumn.prefWidth = dropDown.width + 50
        }
    }
}
