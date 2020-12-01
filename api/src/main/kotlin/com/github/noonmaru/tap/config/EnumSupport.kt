package com.github.noonmaru.tap.config

internal object EnumSupport {
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun valueOf(enumType: Class<*>, name: String): Enum<*> {
        return java.lang.Enum.valueOf(enumType as Class<out Enum<*>>, name)
    }

    @JvmStatic
    fun name(value: Any): String {
        return (value as Enum<*>).name
    }
}