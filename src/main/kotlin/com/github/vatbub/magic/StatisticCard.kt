package com.github.vatbub.magic

import com.github.vatbub.magic.util.bindAndMap
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
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

        private const val fontToWidthFactor = 0.3
    }

    private var card: Card? by Delegates.observable(null) { _, _, newValue ->
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

    @FXML
    lateinit var rootPane: AnchorPane

    @FXML
    private lateinit var statisticLabel: Label

    @FXML
    fun initialize() {
        statisticLabel.fontProperty().bindAndMap(rootPane.widthProperty()) {
            Fonts.magic(it.toDouble() * fontToWidthFactor)
        }
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
