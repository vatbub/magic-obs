package com.github.vatbub.magic

import javafx.event.EventHandler
import javafx.scene.control.TableCell
import javafx.scene.control.TextField

abstract class ObjectIntegerEditingCell<T> : TableCell<T, Int>() {
    private val textField = TextField()
    private val intPattern = "-?\\d+".toRegex()

    init {
        textField.focusedProperty()
            .addListener { _, _, isNowFocused ->
                if (!isNowFocused) {
                    processEdit()
                }
            }
        textField.onAction = EventHandler { processEdit() }
    }

    private fun processEdit() {
        val text = textField.text
        if (intPattern.matches(text))
            commitEdit(text.toInt())
        else cancelEdit()
    }

    override fun updateItem(item: Int?, empty: Boolean) {
        super.updateItem(item, empty)
        when {
            empty -> {
                text = null
                graphic = null
            }
            isEditing -> {
                text = null
                textField.text = item.toString()
            }
            else -> {
                text = item.toString()
                graphic = null
            }
        }
    }

    override fun startEdit() {
        super.startEdit()
        val value = item ?: return
        textField.text = value.toString()
        graphic = textField
        text = null
    }

    override fun cancelEdit() {
        super.cancelEdit()
        text = item.toString()
        graphic = null
    }

    override fun commitEdit(newValue: Int) {
        super.commitEdit(newValue)
        updateItemPropertyValue(tableRow.item, newValue)
    }

    abstract fun updateItemPropertyValue(item: T, newValue: Int)
}
