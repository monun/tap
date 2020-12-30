/*
 * Copyright (c) 2020 Noonmaru
 *  
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.template

import org.bukkit.configuration.ConfigurationSection
import org.mozilla.javascript.Context
import java.util.regex.Pattern

/**
 * [renderTemplates]
 */
fun Collection<String>.renderTemplatesAll(config: ConfigurationSection): List<String> {
    val list = ArrayList<String>(count())

    for (s in this) {
        list += s.renderTemplates(config)
    }

    return list
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

        override fun renderValue(config: ConfigurationSection): String? {
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

            val builder = StringBuilder(name)

            for (path in groups) {
                if (path.toDoubleOrNull() != null) continue //숫자 체크

                config.find(path)?.let { value ->
                    require(value is Number) { "Value is not a number $path in $token" }

                    val index = builder.indexOf(path)
                    builder.replace(index, index + path.length, value.toString())
                } ?: throw IllegalArgumentException("Not found value for $path")
            }

            val script = builder.toString()

            val cx = Context.enter()
            val scope = cx.initSafeStandardObjects()

            cx.runCatching {
                return evaluateString(scope, script, "EvaluationScript", 1, null).toString()
            }.onFailure {
                throw IllegalArgumentException("Failed to evaluate $script", it)
            }.also {
                Context.exit()
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