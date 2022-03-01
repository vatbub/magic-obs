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
package com.github.vatbub.magic.data

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object CardDatabaseParser {
    private val gson by lazy {
        Gson().newBuilder()
            .registerTypeAdapter(ManaColor::class.java, ManaColorDeserializer)
            .create()
    }

    fun parse(): List<CardObject> = gson.fromJson(
        javaClass.getResourceAsStream("card-database.json")!!.reader(),
        arrayOf<CardObject>().javaClass
    ).toList()
}

object ManaColorDeserializer : JsonDeserializer<ManaColor> {
    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext) =
        ManaColor.fromApiString(jsonElement.asString)
}
