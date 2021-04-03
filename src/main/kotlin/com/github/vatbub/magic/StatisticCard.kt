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

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates


class StatisticCard {
    companion object {
        fun newInstance(card: Card): StatisticCard {
            val fxmlLoader = FXMLLoader(HealthPointsView::class.java.getResource("StatisticCard.fxml"))
            fxmlLoader.load<Parent>()
            return fxmlLoader.getController<StatisticCard>().also {
                it.card = card
            }
        }

        private const val maxFontSize = 90.0
        private const val minFontSize = 50.0
    }

    var card: Card? by Delegates.observable(null) { _, _, newValue ->
        if (newValue == null) return@observable
        with(newValue) {
            updateStatisticLabel(
                newValue.attackProperty.value,
                newValue.defenseProperty.value,
                newValue.tappedProperty.value,
                newValue.flyingProperty.value
            )

            attackProperty.addListener { _, _, newValue ->
                updateStatisticLabel(attack = newValue.toInt())
            }
            defenseProperty.addListener { _, _, newValue ->
                updateStatisticLabel(defense = newValue.toInt())
            }
            tappedProperty.addListener { _, _, newValue ->
                updateStatisticLabel(tapped = newValue)
            }
            flyingProperty.addListener { _, _, newValue ->
                updateStatisticLabel(flying = newValue)
            }
        }
    }
        private set

    @FXML
    lateinit var rootPane: GridPane

    @FXML
    private lateinit var statisticLabel: Label

    @FXML
    private lateinit var middleImageView: ImageView

    @FXML
    fun initialize() {
        updateMiddleWidth()
        rootPane.widthProperty().addListener { _, _, _ ->
            updateMiddleWidth()
        }
    }

    fun updateMiddleWidth() {
        val newMiddleWidth =
            rootPane.prefWidth - rootPane.getCellBounds(0, 0).width - rootPane.getCellBounds(2, 0).width + 2
        val fontSize = max(minFontSize, min(maxFontSize, newMiddleWidth - 135))

        middleImageView.fitWidth = newMiddleWidth
        statisticLabel.font = Fonts.magic(fontSize)
    }

    private fun updateStatisticLabel(
        attack: Int = card!!.attackProperty.value,
        defense: Int = card!!.defenseProperty.value,
        tapped: Boolean = card!!.tappedProperty.value,
        flying: Boolean = card!!.flyingProperty.value
    ) {
        statisticLabel.text = with(StringBuilder("$attack/$defense")) {
            if (tapped)
                append("*")
            if (flying)
                append("(f)")
            toString()
        }
    }
}
