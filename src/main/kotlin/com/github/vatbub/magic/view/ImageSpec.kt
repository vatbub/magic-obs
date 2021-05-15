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

import javafx.scene.image.Image
import java.io.File
import java.io.InputStream

sealed class ImageSpec {
    abstract fun openInputStream(): InputStream

    fun load(
        requestedWidth: Double = 0.0,
        requestedHeight: Double = 0.0,
        preserveRatio: Boolean = false,
        smooth: Boolean = false
    ): Image =
        openInputStream().use { Image(it, requestedWidth, requestedHeight, preserveRatio, smooth) }

    data class System(val file: File) : ImageSpec() {
        override fun openInputStream(): InputStream = file.inputStream()
    }

    data class BuiltIn(val resourceName: String) : ImageSpec() {
        override fun openInputStream(): InputStream = javaClass.getResourceAsStream(resourceName)!!
    }
}

enum class BuiltInImageSpecs(val imageSpec: ImageSpec.BuiltIn, val humanReadableName: String) {
    GreenRing(ImageSpec.BuiltIn("HealthPointsFrame.png"), "Green ring");

    companion object {
        fun forSpec(imageSpec: ImageSpec.BuiltIn) = values().first { it.imageSpec == imageSpec }
    }
}
