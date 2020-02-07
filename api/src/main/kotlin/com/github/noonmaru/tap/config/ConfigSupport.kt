/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.lang.reflect.Field


@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(val value: String = "")

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeInt(val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeLong(val min: Long = Long.MIN_VALUE, val max: Long = Long.MAX_VALUE)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeFloat(val min: Float = java.lang.Float.MAX_VALUE, val max: Float = java.lang.Float.MIN_VALUE)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeDouble(val min: Double = java.lang.Double.MAX_VALUE, val max: Double = java.lang.Double.MIN_VALUE)

private object PrimitiveSupport {

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

    fun findAdapter(type: Class<*>): ((Field, Any) -> Any)? {
        return primitiveAdapters[type]
    }
}

/**
 * 설정을 인스턴스의 [Config] 속성에 적용합니다.
 * 결손된 값이 있다면 [ConfigurationSection]에 저장하고 **true**를 반환합니다.
 *
 * @return 결손된 값이 있을 경우 **true**, 모든 값이 로딩됐을 경우 **false**
 *
 * @see Config
 * @see RangeInt
 * @see RangeLong
 * @see RangeFloat
 * @see RangeDouble
 */
fun Any.applyConfig(config: ConfigurationSection): Boolean {
    var absent = false
    val configurables = javaClass.getConfigurables()

    for ((name, field) in configurables) {
        var value = config.get(name)

        if (value != null) {
            val type = field.type

            if (type.isPrimitive) {
                val adapter = PrimitiveSupport.findAdapter(type)

                if (adapter != null) {
                    value = adapter.invoke(field, value)
                }
            } else if (type.isEnum) {
                value = EnumSupport.valueOf(type, value.toString())
            }

            field.set(this, value)
        } else {
            value = field.get(this)

            if (value.javaClass.isEnum) {
                value = EnumSupport.name(value)
            }

            config.set(name, value)
            absent = true
        }
    }

    return absent
}

/**
 * 파일로부터 불러온 설정을 인스턴스의 [Config] 속성에 적용합니다.
 * 결손된 값이 있다면 [ConfigurationSection]에 저장하고 **true**를 반환합니다.
 *
 * @return 결손된 값이 있을 경우 **true**, 모든 값이 로딩됐을 경우 **false**
 *
 * @see Config
 * @see RangeInt
 * @see RangeLong
 * @see RangeFloat
 * @see RangeDouble
 */
fun Any.applyConfig(configFile: File): Boolean {
    if (!configFile.exists()) {
        val config = YamlConfiguration()
        applyConfig(config)
        config.save(configFile)

        return true
    }

    val config = YamlConfiguration.loadConfiguration(configFile)

    if (applyConfig(config)) {
        config.save(configFile)

        return true
    }

    return false
}

private fun Class<*>.getConfigurables(): List<Pair<String, Field>> {

    val superClasses = getSuperClasses(Any::class.java).reversed()
    val list = ArrayList<Pair<String, Field>>()

    for (clazz in superClasses) {

        for (field in clazz.declaredFields) {
            field.getAnnotation(Config::class.java)?.let {
                field.isAccessible = true

                var key = it.value

                if (key.isBlank()) {
                    key = field.name.toConfigKey()
                }

                list += Pair(key, field)
            }
        }
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
            builder[i] = c.toLowerCase()

            if (i > 0) {
                builder.insert(i, '-')
                i++
            }
        }

        i++
    }

    return builder.toString()
}