/*-
 * #%L
 * Smart Charge
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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

import com.github.vatbub.magic.App.Companion.appId
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.nio.file.Files

val appDataFolder = when {
    SystemUtils.IS_OS_WINDOWS -> System.getenv("AppData")
    SystemUtils.IS_OS_MAC -> System.getProperty("user.home") + "/Library/Application Support"
    else -> System.getProperty("user.home") + "/.local/share"
}.let { File(it, appId) }
        .also { Files.createDirectories(it.toPath()) }
