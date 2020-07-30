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

import com.google.common.base.Preconditions
import org.bukkit.configuration.ConfigurationSection
import java.util.regex.Pattern
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

fun Collection<String>.renderTemplatesAll(config: ConfigurationSection): List<String> {
    val list = ArrayList<String>(count())

    for (s in this) {
        list += s.renderTemplates(config)
    }

    return list
}

fun String.renderTemplates(
    config: ConfigurationSection
): String {
    val builder = StringBuilder(this)

    createTemplates(this).forEach { template ->
        val token = template.token

        runCatching {
            template.process(config).let { replace ->
                val index = builder.indexOf(token)
                builder.replace(index, index + token.count(), replace ?: "null")
            }
        }.onFailure { t ->
            throw IllegalArgumentException("Failed to process template for $token", t)
        }
    }

    return builder.toString()
}


private fun createTemplates(s: String): List<Template> {
    val list = ArrayList<Template>()

    s.findMatchGroups(Template.pattern).forEach {
        list += Template.createTemplate(it)
    }

    return list
}

internal abstract class Template {
    companion object {
        val js: ScriptEngine by lazy {
            ScriptEngineManager().getEngineByName("js")
        }

        internal val pattern = Pattern.compile("\\\$((\\{.+?})|(\\w+(-\\w*)*))")

        private val variablePattern = Pattern.compile("(\\w+(-\\w*)*)")

        private val expressionRegex = Regex("\\\$(\\{.+?})")

        fun createTemplate(s: String): Template {
            return if (s.matches(expressionRegex)) {
                Expression()
            } else {
                Variable()
            }.apply {
                token = s
            }
        }
    }

    lateinit var token: String
        private set

    abstract fun process(config: ConfigurationSection): String?

    private class Expression : Template() {

        override fun process(config: ConfigurationSection): String? {
            val groups = token.findMatchGroups(variablePattern)

            if (groups.isEmpty()) return null
            if (groups.count() == 1) return config.find(groups.first())?.toString()

            val builder = StringBuilder(token)
            builder.delete(0, 2)
            builder.deleteCharAt(builder.lastIndex)

            for (path in groups) {
                if (path.toDoubleOrNull() != null) continue

                config.find(path)?.let { value ->
                    Preconditions.checkArgument(value is Number, "Value $path is not a number")

                    val index = builder.indexOf(path)
                    builder.replace(index, index + path.length, value.toString())
                } ?: throw IllegalArgumentException("Not found value for $path")
            }

            val script = builder.toString()

            js.runCatching {
                return eval(script).toString()
            }.onFailure {
                throw IllegalArgumentException("Failed to evaluate $script")
            }

            return null
        }
    }

    private class Variable : Template() {
        override fun process(config: ConfigurationSection): String? {
            return config[token.substring(1)]?.toString()
        }
    }
}

private fun ConfigurationSection.find(path: String): Any? {
    var config = this
    val keys = path.split(".")

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

private fun String.findMatchGroups(pattern: Pattern): List<String> {
    val list = ArrayList<String>()
    val matcher = pattern.matcher(this)

    while (matcher.find()) {
        list.add(matcher.group())
    }

    return list
}