/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import java.io.File
import java.lang.reflect.Field


@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(val value: String = "", val required: Boolean = true)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeInt(val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeLong(val min: Long = java.lang.Long.MIN_VALUE, val max: Long = java.lang.Long.MAX_VALUE)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeFloat(val min: Float = java.lang.Float.MIN_VALUE, val max: Float = java.lang.Float.MAX_VALUE)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeDouble(val min: Double = java.lang.Double.MIN_VALUE, val max: Double = java.lang.Double.MAX_VALUE)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(val value: String)

object ConfigSupport {
    private val primitiveAdapters = HashMap<Class<*>, (Field, Any) -> Any>()

    init {
        primitiveAdapters[Int::class.java] = { field, value ->
            value as Number
            var i = value.toInt()
            val range = field.getAnnotation(RangeInt::class.java)
            if (range != null) {
                i = i.coerceIn(range.min, range.max)
            }
            i
        }

        primitiveAdapters[Long::class.java] = { field, value ->
            value as Number
            var l = value.toLong()
            val range = field.getAnnotation(RangeLong::class.java)
            if (range != null) {
                l = l.coerceIn(range.min, range.max)
            }
            l
        }

        primitiveAdapters[Float::class.java] = { field, value ->
            value as Number
            var f = value.toFloat()
            val range = field.getAnnotation(RangeFloat::class.java)
            if (range != null) {
                f = f.coerceIn(range.min, range.max)
            }
            f
        }

        primitiveAdapters[Double::class.java] = { field, value ->
            value as Number
            var d = value.toDouble()
            val range = field.getAnnotation(RangeDouble::class.java)
            if (range != null) {
                d = d.coerceIn(range.min, range.max)
            }
            d
        }
    }

    private fun findAdapter(type: Class<*>): ((Field, Any) -> Any)? {
        return primitiveAdapters[type]
    }

    /**
     * 설정을 인스턴스의 [Config] 속성에 적용합니다.
     * 결손된 값이 있다면 설정에 저장하고 **true**를 반환합니다.
     *
     * @param config 적용할 설정
     * @param separateByClass 클래스별 섹션 분리 여부
     *
     * @return 결손된 값이 있을 경우 **true**, 모든 값이 로딩됐을 경우 **false**
     *
     * @see Config
     * @see RangeInt
     * @see RangeLong
     * @see RangeFloat
     * @see RangeDouble
     */
    fun compute(target: Any, config: ConfigurationSection, separateByClass: Boolean = false): Boolean {
        var absent = false
        val configurables = target.javaClass.getConfigurables()

        for ((clazz, list) in configurables) {
            val sectionPath = if (separateByClass) clazz.configKey else ""
            var section = if (sectionPath.isNotBlank()) config.getConfigurationSection(sectionPath) else config

            for ((field, settings) in list) {
                val key = settings.value.let { it.ifBlank { field.name.toConfigKey() } }
                var value = section?.get(key)

                if (value != null) {
                    val type = field.type
                    when {
                        type.isPrimitive -> {
                            value = findAdapter(type)?.invoke(field, value)
                        }
                        type.isEnum -> {
                            try {
                                value = EnumSupport.valueOf(type, value.toString())
                            } catch (e: IllegalArgumentException) {
                                error("Not found Enum $type for $value")
                            }
                        }
                        value is ConfigurationSection -> {
                            require(ConfigurationSerializable::class.java.isAssignableFrom(type))

                            value = ConfigurationSerialization.deserializeObject(
                                value.getValues(false), type.asSubclass(
                                    ConfigurationSerializable::class.java
                                )
                            )
                        }
                    }

                    value?.let { input ->
                        try {
                            field.set(target, input)
                        } catch (e: Exception) {
                            error("Type mismatch! ${type.name} != ${input.javaClass.name}")
                        }
                    }

                    continue
                }

                value = field.get(target)

                if (!settings.required && (value == null || (value is Number && value.isZero()))) { //필요하지 않을경우 스킵
                    continue
                }

                if (value.javaClass.isEnum) {
                    value = EnumSupport.name(value)
                }

                absent = true

                if (section == null) section = config.createSection(sectionPath)

                if (value is ConfigurationSerializable) {
                    value = value.serialize()
                }

                section.set(key, value)
            }
        }

        return absent
    }

    /**
     * 파일로부터 불러온 설정을 인스턴스의 [Config] 속성에 적용합니다.
     * 결손된 값이 있다면 설정에 저장하고 **true**를 반환합니다.
     *

     * @param configFile 적용할 설정파일
     * @param separateByClass 클래스별 섹션 분리 여부
     *
     * @return 결손된 값이 있을 경우 (파일이 수정됨) **true**, 모든 값이 로딩됐을 경우 **false**
     *
     * @see Config
     * @see RangeInt
     * @see RangeLong
     * @see RangeFloat
     * @see RangeDouble
     */
    fun compute(target: Any, configFile: File, separateByClass: Boolean = false): Boolean {
        if (!configFile.exists()) {
            val config = YamlConfiguration()
            compute(target, config)
            config.save(configFile)

            return true
        }

        val config = YamlConfiguration.loadConfiguration(configFile)

        if (compute(target, config, separateByClass)) {
            config.save(configFile)

            return true
        }

        return false
    }
}

private val Class<*>.configKey: String
    get() {
        val name = getAnnotation(Name::class.java)

        return name?.value ?: simpleName.toConfigKey()
    }

private fun Number.isZero(): Boolean {
    return when (this) {
        is Int -> toInt() == 0
        is Long -> toLong() == 0L
        is Float -> toFloat() == 0.0F
        else -> toDouble() == 0.0
    }
}

private fun Class<*>.getConfigurables(): Map<Class<*>, List<Pair<Field, Config>>> {
    val superClasses = getSuperClasses(Any::class.java).reversed()
    val list: MutableMap<Class<*>, List<Pair<Field, Config>>> = LinkedHashMap()

    for (clazz in superClasses) {
        val configFields = ArrayList<Pair<Field, Config>>()

        for (field in clazz.declaredFields) {
            field.getAnnotation(Config::class.java)?.let { config ->
                field.isAccessible = true

                configFields += Pair(field, config)
            }
        }

        list += Pair(clazz, configFields)
    }

    return list
}

private fun Class<*>.getSuperClasses(limit: Class<*>): List<Class<*>> {
    val list = ArrayList<Class<*>>()

    var c: Class<*> = this

    while (c != limit) {
        list += c
        c = c.superclass
    }

    return list
}

private fun String.toConfigKey(): String {
    val builder = StringBuilder(this)

    var i = 0

    while (i < builder.count()) {
        val c = builder[i]

        if (c.isUpperCase()) {
            builder[i] = c.lowercaseChar()

            if (i > 0) {
                builder.insert(i, '-')
                i++
            }
        }

        i++
    }

    return builder.toString()
}

fun ConfigurationSection.compute(target: Any, separateByClass: Boolean = false): Boolean {
    return ConfigSupport.compute(target, this, separateByClass)
}