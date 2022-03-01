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

import javafx.event.EventHandler
import javafx.scene.control.TableCell
import javafx.scene.control.TextField

abstract class ObjectDoubleEditingCell<T> : TableCell<T, Double>() {
    private val textField = TextField()

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
        textField.text.toDoubleOrNull()?.let { commitEdit(it) } ?: cancelEdit()
    }

    override fun updateItem(item: Double?, empty: Boolean) {
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

    override fun commitEdit(newValue: Double) {
        super.commitEdit(newValue)
        updateItemPropertyValue(tableRow.item, newValue)
    }

    abstract fun updateItemPropertyValue(item: T, newValue: Double)
}
