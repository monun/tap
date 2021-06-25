/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.monun.tap.template

import net.kyori.adventure.text.BuildableComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.TextComponent
import org.bukkit.configuration.ConfigurationSection

fun <T : ComponentBuilder<*, *>> T.renderTemplates(config: ConfigurationSection): T {
    applyDeep {
        if (it is TextComponent.Builder)
            it.content(it.content().renderTemplates(config))
    }

    return this
}

@Suppress("UNCHECKED_CAST")
fun <T : Component> T.renderTemplates(config: ConfigurationSection) = if (this is BuildableComponent<*, *>) {
    toBuilder().renderTemplates(config).build() as T
} else {
    this
}


@JvmName("renderComponentBuilderTemplatesAll")
fun <T : ComponentBuilder<*, *>> Iterable<T>.renderTemplatesAll(config: ConfigurationSection) =
    forEach { it.renderTemplates(config) }

@JvmName("renderComponentTemplatesAll")
fun <T : Component> Iterable<T>.renderTemplatesAll(config: ConfigurationSection): List<T> = map {
    it.renderTemplates(config)
}