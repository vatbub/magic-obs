package com.github.vatbub.magic.util

import com.github.vatbub.magic.App
import javafx.util.StringConverter

class EnumStringConverter<T : Enum<T>> : StringConverter<T>() {
    override fun toString(obj: T): String {
        val enumName = obj.javaClass.simpleName
        return App.resourceBundle["enum.$enumName.${obj.name}"] ?: obj.toString()
    }

    override fun fromString(string: String?): T = throw NotImplementedError("Not supported")
}
