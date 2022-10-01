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

import com.github.vatbub.magic.common.preferenceFolder
import javafx.concurrent.Task
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL
import java.util.concurrent.Executors

object CardDatabase {
    @Suppress("SENSELESS_COMPARISON")
    val cardObjects by lazy {
        getParserToUse()
            .parse()
            // Might actually be nullable (Instants, Sorcery have no power or toughness),
            // but we don't want them in our list
            // and also don't want to pollute the type with the nullable marker
            .filter { it.power != null }
            .filter { it.toughness != null }
            .map { it.fixNulls() }
            .distinct()
    }

    private var parser: CardDatabaseParser? = null
    private fun getParserToUse() = parser
        ?: if (destinationFile.exists()) CardDatabaseParser(destinationFile)
        else CardDatabaseParser()

    private val destinationFile get() = preferenceFolder.resolve("mtg-card-database.json")

    private fun downloadUpdate(): File {
        println("Getting bulk data download location from the Scyfall API...")
        val downloadUrl = URL("https://api.scryfall.com/bulk-data")
        val bulkDataApiJson = downloadUrl.openStream().bufferedReader().use { it.readText() }
        val parsedApiResponse = gson.fromJson(bulkDataApiJson, BulkDataApiResult::class.java)

        val jsonFileUri = parsedApiResponse.data.first { it.type == "default_cards" }.download_uri
        println("Download uri is ${jsonFileUri.toURL().toExternalForm()}, downloading now...")
        val downloadedJson = jsonFileUri.toURL().openStream().bufferedReader().use { it.readText() }

        println("Writing updated database to ${destinationFile.absolutePath}...")
        destinationFile.writeText(downloadedJson)
        println("Update complete")
        return destinationFile
    }

    val downloadAndParseTask = object : Task<Unit>() {
        init {
            updateTitle("Loading the card database...")
            updateProgress(-1L, 1L)
        }

        override fun call() {
            updateMessage("Downloading card database updates...")
            try {
                val downloadedFile = downloadUpdate()
                parser = CardDatabaseParser(downloadedFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            updateMessage("Parsing the database...")
            @Suppress("UNUSED_VARIABLE")
            val dummy = cardObjects
        }
    }

    fun downloadUpdateAsyncWithGui() {
        with(Executors.newSingleThreadExecutor()) {
            downloadAndParseTask.setOnFailed { (downloadAndParseTask.exception as? Exception)?.printStackTrace() }
            submit(downloadAndParseTask)
            shutdown()
        }
    }
}

data class BulkDataApiResult(
    val `object`: String,
    val has_more: Boolean,
    val data: List<BulkDataApiObject>
)

data class BulkDataApiObject(
    val `object`: String,
    val id: String,
    val type: String,
    val updated_at: String,
    val uri: URI,
    val name: String,
    val description: String,
    val compressed_size: Long,
    val download_uri: URI,
    val content_type: String,
    val content_encoding: String
)

data class CardObject(
    val name: String,
    val card_faces: List<CardFace>?,
    val lang: Language,
    val type_line: String,
    val power: String,
    val toughness: String,
    val colors: List<ManaColor>,
    val color_identity: List<ManaColor>,
    val keywords: List<String>?
) {
    fun fixNulls() = CardObjectNoNullables(
        name,
        card_faces?.map { it.fixNulls() } ?: listOf(),
        lang,
        type_line,
        power,
        toughness,
        colors,
        color_identity,
        keywords ?: listOf(),
    )
}

data class CardObjectNoNullables(
    val name: String,
    val card_faces: List<CardFaceLessNullables>,
    val lang: Language,
    val type_line: String,
    val power: String,
    val toughness: String,
    val colors: List<ManaColor>,
    val color_identity: List<ManaColor>,
    val keywords: List<String>
) {
    val abilities = keywords.mapNotNull { it.toAbility() }

    fun toOverlayCard() = Card(
        attack = power.toDoubleOrNull() ?: 1.0,
        defense = toughness.toDoubleOrNull() ?: 1.0,
        counter = 0,
        abilities = abilities.toSet()
    )
}

enum class Language {
    en,
    es,
    fr,
    de,
    it,
    pt,
    ja,
    ko,
    ru,
    zhs,
    zht,
    he,
    la,
    grc,
    ar,
    sa,
    ph,
}

enum class ManaColor(val apiString: String) {
    White("W"),
    Blue("U"),
    Black("B"),
    Red("R"),
    Green("G");

    companion object {
        fun fromApiString(apiString: String) = values().first { it.apiString == apiString }
        fun fromArray(iterable: Iterable<String>) = iterable.map { fromApiString(it) }
    }
}

data class CardFace(
    val name: String,
    val type_line: String,
    val power: String?,
    val toughness: String?,
    val colors: List<ManaColor>?,
    val color_identity: List<ManaColor>?,
    val keywords: List<String>?
) {
    fun fixNulls() = CardFaceLessNullables(
        name,
        type_line,
        power,
        toughness,
        colors ?: listOf(),
        color_identity ?: listOf(),
        keywords ?: listOf(),
    )
}

data class CardFaceLessNullables(
    val name: String,
    val type_line: String,
    val power: String?,
    val toughness: String?,
    val colors: List<ManaColor>,
    val color_identity: List<ManaColor>,
    val keywords: List<String>
)

private fun String.toAbility(): Ability? = when (this) {
    "Explore" -> null
    "Parley" -> null
    else -> Ability.values().firstOrNull { it.name == this }
}
