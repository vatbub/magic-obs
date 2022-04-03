/*-
 * #%L
 * Open Thesaurus Java View
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

import com.github.vatbub.magic.data.DataHolder
import com.github.vatbub.magic.data.PreferenceKeys.UIStyle
import com.github.vatbub.magic.data.preferences
import com.github.vatbub.magic.view.*
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import java.io.Closeable
import java.util.*

class App private constructor(callLaunch: Boolean, vararg args: String?) : Application() {
    companion object {
        lateinit var instance: App
            private set

        var instanceNumber: Int = 1
            private set

        const val id = "com.github.vatbub.magic.obs"

        val resourceBundle: ResourceBundle by lazy {
            ResourceBundle.getBundle("com.github.vatbub.magic.strings")
        }


        fun actualMain(vararg args: String) {
            instanceNumber = AppInstanceManager.afterStart()
            App(true, *args)
        }
    }

    /**
     * Required for JavaFX
     */
    @Suppress("unused")
    constructor() : this(false, null)

    init {
        if (callLaunch)
            launch(*args)
    }

    var currentStage: Stage? = null
        private set
    var controllerInstance: MainView? = null
        private set
    private lateinit var jMetro: JMetro

    val auxiliaryViews: MutableList<Closeable> = mutableListOf()

    override fun start(primaryStage: Stage) {
        instance = this
        currentStage = primaryStage

        val fxmlLoader = FXMLLoader(javaClass.getResource("view/MainView.fxml"), resourceBundle)
        val root = fxmlLoader.load<Parent>()
        controllerInstance = fxmlLoader.getController()

        val scene = Scene(root)
        jMetro = JMetro(scene, preferences[UIStyle])
        jMetro.styleProperty().bind(DataHolder.uiStyle)
        primaryStage.title = "Magic OBS".appendInstanceNumber()
        primaryStage.icons.add(Image(MainView::class.java.getResourceAsStream("icon.png")))
        primaryStage.minWidth = root.minWidth(0.0) + 70
        primaryStage.minHeight = root.minHeight(0.0) + 70

        primaryStage.scene = scene

        openAuxiliaryViews()

        primaryStage.setOnCloseRequest {
            controllerInstance?.close()
            closeAuxiliaryViews()
        }

        primaryStage.show()
    }

    override fun stop() {
        AppInstanceManager.beforeExit()
    }

    private fun openAuxiliaryViews() {
        auxiliaryViews.add(HealthPointsView.show())
        auxiliaryViews.add(CardStatisticsView.show())
        auxiliaryViews.add(DayNightView.show())
    }

    private fun closeAuxiliaryViews() {
        while (auxiliaryViews.isNotEmpty()) {
            auxiliaryViews.removeAt(0).close()
        }
    }

    fun resetAuxiliaryViews() {
        closeAuxiliaryViews()
        openAuxiliaryViews()
    }
}
