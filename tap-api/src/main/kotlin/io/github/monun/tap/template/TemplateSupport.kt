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

package io.github.monun.tap.template

import org.bukkit.configuration.ConfigurationSection
import java.util.regex.Pattern

/**
 * [renderTemplates]
 */
fun Iterable<String>.renderTemplatesAll(config: ConfigurationSection) = map {
    it.renderTemplates(config)
}

/**
 * 문자열의 템플릿을 렌더합니다.
 *
 * 템플릿의 종류
 * 1. variable - $name
 * 2. expression - ${child.name}
 *
 * expression내에는 다음과 같이 식을 추가할 수 있습니다.
 *
 * ***${number * other-number / 100}***
 *
 * 섹션 접근 방법
 *
 * 1. ***parent.varname*** - 상위 섹션 접근
 * 2. ***childname.varname*** - 하위 섹션 접근
 *
 * 템플릿의 작성 규칙은 다음과 같습니다.
 *
 * 1. 변수의 이름은 숫자, 영어, underline(_), dash(-)로만 구성하세요.
 * 2. 변수의 이름은 공백을 허용하지 않습니다.
 * 3. expression내에 연산자는 반드시 앞뒤로 공백을 넣어 구분해주세요.
 *
 * @return rendered text
 */
fun String.renderTemplates(
    config: ConfigurationSection
): String {
    val builder = StringBuilder(this)
    val templates = Template.parse(this)

    for (template in templates) {
        val token = template.token

        template.runCatching {
            renderValue(config)
        }.onSuccess { replace ->
            val indexOfToken = builder.indexOf(token)

            builder.replace(indexOfToken, indexOfToken + token.count(), replace ?: "null")
        }.onFailure { throwable ->
            throw IllegalArgumentException("Failed to render template token $token", throwable)
        }
    }

    return builder.toString()
}

internal abstract class Template(
    val token: String
) {
    companion object {
        private val variablePattern = Pattern.compile("\\$([\\w-])+")

        private val expressionRegex = Pattern.compile("\\$\\{(.+?)}")

        internal fun parse(text: String): List<Template> {
            val list = arrayListOf<Template>()

            variablePattern.let { pattern ->
                val matcher = pattern.matcher(text)

                while (matcher.find()) {
                    list += Variable(matcher.group())
                }
            }

            expressionRegex.let { pattern ->
                val matcher = pattern.matcher(text)

                while (matcher.find()) {
                    list += Expression(matcher.group())
                }
            }

            return list
        }
    }

    abstract val name: String

    abstract fun renderValue(config: ConfigurationSection): String?

    private class Variable(token: String) : Template(token) {
        override val name: String
            get() = token.substring(1)

        override fun renderValue(config: ConfigurationSection): String {
            val value = config[name]

            if (value != null && value is Number) {
                return value.toString().removeSuffix(".0")
            }

            return value.toString()
        }
    }

    private class Expression(token: String) : Template(token) {
        companion object {
            private val expressionVariablePattern = Pattern.compile("[\\w-]+(\\.[\\w-]+)*")
        }

        override val name: String
            get() = token.run { substring(2, length - 1) }

        override fun renderValue(config: ConfigurationSection): String? {
            val groups = token.findMatchGroups(expressionVariablePattern)

            if (groups.isEmpty()) return null
            if (groups.count() == 1) {
                val value = config.find(groups.first())

                return if (value is Number) value.toString().removeSuffix(".0") else value.toString()
            }

            val expressionBuilder = StringBuilder(name)

            for (path in groups) {
                if (path.toDoubleOrNull() != null) continue //숫자 체크

                config.find(path)?.let { value ->
                    require(value is Number) { "Value is not a number $path in $token" }

                    val index = expressionBuilder.indexOf(path)
                    expressionBuilder.replace(index, index + path.length, value.toString())
                } ?: throw IllegalArgumentException("Not found value for $path")
            }

            val expressionString = expressionBuilder.toString()
            val expression = org.mariuszgromada.math.mxparser.Expression(expressionString)

            expression.runCatching {
                return expression.calculate().toString().removeSuffix(".0")
            }.onFailure {
                throw IllegalArgumentException("Failed to evaluate $expressionString", it)
            }

            return null
        }
    }
}

private fun String.findMatchGroups(pattern: Pattern): List<String> {
    val list = arrayListOf<String>()
    val matcher = pattern.matcher(this)

    while (matcher.find()) {
        list.add(matcher.group())
    }

    return list
}

private fun ConfigurationSection.find(path: String): Any? {
    var config = this
    val keys = path.split('.')

    var value: Any? = null

    keys.forEachIndexed { index, key ->
        if ("parent" == key) {
            config = config.parent ?: return null
        } else {
            if (index != keys.lastIndex) {
                config = config.getConfigurationSection(key) ?: return null
            } else {
                value = config[key]
            }
        }
    }

    return value
}