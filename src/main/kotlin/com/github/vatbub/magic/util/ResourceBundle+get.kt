package com.github.vatbub.magic.util

import java.util.*

operator fun ResourceBundle.get(key: String): String = this.getString(key)
