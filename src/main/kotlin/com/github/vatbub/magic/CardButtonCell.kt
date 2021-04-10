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

import com.github.vatbub.magic.util.swap
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.TableCell
import javafx.scene.layout.HBox

class CardButtonCell : TableCell<Card, Card>() {
    private val hBox = HBox(
        Button("Kill").also { it.setOnAction(this::killButtonOnAction) },
        Button("Up").also { it.setOnAction(this::upButtonOnAction) },
        Button("Down").also { it.setOnAction(this::downButtonOnAction) }
    ).apply {
        spacing = 8.0
    }


    override fun updateItem(item: Card?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            text = null
            graphic = null
            return
        }

        graphic = hBox
        Platform.runLater {
            if (tableColumn.width < hBox.width)
                tableColumn.prefWidth = hBox.width + 50
        }
    }

    private fun killButtonOnAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val card = tableRow.item ?: return
        DataHolder.cardList.remove(card)
    }

    private fun upButtonOnAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val card = tableRow.item ?: return
        val index = DataHolder.cardList.indexOf(card)
        if (index == 0) return
        DataHolder.cardList.swap(index, index - 1)
    }

    private fun downButtonOnAction(@Suppress("UNUSED_PARAMETER") event: ActionEvent) {
        val card = tableRow.item ?: return
        val index = DataHolder.cardList.indexOf(card)
        if (index + 1 == DataHolder.cardList.size) return
        DataHolder.cardList.swap(index, index + 1)
    }
}