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
