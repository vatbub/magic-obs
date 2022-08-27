/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2019 - 2022 Frederik Kammel
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
package com.github.vatbub.magic.util

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue

fun <In, Out> ObservableValue<In>.map(block: (In) -> Out): ObservableValue<Out> =
    SimpleObjectProperty(block(this.value)).also { returnedProperty ->
        this.addListener { _, _, newValue -> returnedProperty.value = block(newValue) }
    }

fun <In> ObservableValue<In>.mapToBooleanProperty(block: (In) -> Boolean): ReadOnlyBooleanProperty =
    SimpleBooleanProperty(block(this.value)).also { returnedProperty ->
        this.addListener { _, _, newValue -> returnedProperty.value = block(newValue) }
    }

fun <In, Out> ObservableValue<In>.mapToObservable(block: (In) -> ObservableValue<Out>): ObservableValue<Out> =
    SimpleObjectProperty(block(this.value).value).also { returnedProperty ->
        returnedProperty.bind(block(this.value))
        this.addListener { _, _, newValue ->
            returnedProperty.unbind()
            returnedProperty.bind(block(newValue))
        }
    }


@JvmName("mapToBooleanObservable")
fun <In> ObservableValue<In>.mapToBooleanObservable(block: (In) -> ObservableValue<Boolean>): ReadOnlyBooleanProperty =
    SimpleBooleanProperty(block(this.value).value).also { returnedProperty ->
        returnedProperty.bind(block(this.value))
        this.addListener { _, _, newValue ->
            returnedProperty.unbind()
            returnedProperty.bind(block(newValue))
        }
    }