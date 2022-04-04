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
package com.github.vatbub.magic

import com.github.vatbub.magic.common.preferences
import com.github.vatbub.magic.data.PreferenceKeys.RunningInstances
import tk.pratanumandal.unique4j.Unique4j


object AppInstanceManager {
    private var unique: Unique4j = object : Unique4j(App.id, false) {
        override fun receiveMessage(message: String) {}

        override fun sendMessage(): String = ""
    }

    private var runningInstanceCount: Int by preferences.delegate(RunningInstances)

    fun afterStart(): Int {
        if (unique.acquireLock()) {
            runningInstanceCount = 1
            return 1
        }

        runningInstanceCount++
        return runningInstanceCount
    }

    fun beforeExit() {
        unique.freeLock()
        runningInstanceCount--
    }
}
