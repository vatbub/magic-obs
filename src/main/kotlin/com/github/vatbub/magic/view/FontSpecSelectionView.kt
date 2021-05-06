package com.github.vatbub.magic.view

import com.github.vatbub.magic.App
import com.github.vatbub.magic.util.get
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle


class FontSpecSelectionView {
    companion object {
        fun show(initialSpec: FontSpec?): FontSpecSelectionView {
            val stage = Stage(StageStyle.UNIFIED)
            stage.initModality(Modality.APPLICATION_MODAL)

            val fxmlLoader =
                FXMLLoader(
                    FontSpecSelectionView::class.java.getResource("FontSpecSelectionView.fxml"),
                    App.resourceBundle
                )
            val root = fxmlLoader.load<Parent>()
            val controllerInstance = fxmlLoader.getController<FontSpecSelectionView>()
            controllerInstance.stage = stage
            if (initialSpec != null)
                controllerInstance.initialSpec = initialSpec

            val scene = Scene(root)

            stage.title = App.resourceBundle["fontSpecSelectionView.title"]
            // stage.icons.add(Image(javaClass.getResourceAsStream("icon.png")))
            stage.minWidth = root.minWidth(0.0) + 70
            stage.minHeight = root.minHeight(0.0) + 70

            stage.scene = scene

            stage.show()
            return controllerInstance
        }
    }

    @FXML
    private lateinit var systemWeightDropDown: ComboBox<FontWeight>

    @FXML
    private lateinit var builtInFontSpecDropDown: ComboBox<BuiltInFontSpecs>

    @FXML
    private lateinit var systemFamilyDropDown: ComboBox<String>

    @FXML
    private lateinit var systemPostureDropDown: ComboBox<FontPosture>

    @FXML
    private lateinit var builtInTab: Tab

    @FXML
    private lateinit var systemTab: Tab

    lateinit var stage: Stage
        private set
    private var initialSpec: FontSpec = BuiltInFontSpecs.ArchitectsDaughterRegular.fontSpec

    @FXML
    fun initialize() {
        systemWeightDropDown.items = FXCollections.observableArrayList(*FontWeight.values())
        systemPostureDropDown.items = FXCollections.observableArrayList(*FontPosture.values())
        systemFamilyDropDown.items = FXCollections.observableArrayList(FontSpec.systemFonts)
        builtInFontSpecDropDown.items = FXCollections.observableArrayList(*BuiltInFontSpecs.values())
    }

    @FXML
    fun okButtonOnAction() {
    }

    @FXML
    fun cancelButtonOnAction() {
    }
}
