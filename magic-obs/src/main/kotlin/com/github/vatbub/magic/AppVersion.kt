/*-
 * #%L
 * magic-obs
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor

// yyyy-MM-dd HH:mm:ss
private val mavenDateFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4)
    .appendLiteral("-")
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral("-")
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendLiteral(" ")
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(":")
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .appendLiteral(":")
    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
    .toFormatter()

private val uiDateFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendLiteral(".")
    .appendValue(ChronoField.MONTH_OF_YEAR)
    .appendLiteral(".")
    .appendValue(ChronoField.YEAR)
    .appendLiteral(" ")
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(":")
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .appendLiteral(":")
    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
    .toFormatter()

internal val TemporalAccessor.uiString
    get() = uiDateFormatter.format(this)

internal const val appVersion = "%PROJECT_VERSION%"
internal val buildTimestamp: LocalDateTime = try {
    LocalDateTime.parse("%build_timestamp%", mavenDateFormatter)
} catch (e: DateTimeParseException) {
    LocalDateTime.now()
}
