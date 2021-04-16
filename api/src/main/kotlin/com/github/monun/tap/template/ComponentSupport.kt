/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.template

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.configuration.ConfigurationSection

/**
 * [String.renderTemplates]
 */
fun Component.renderTemplates(
    config: ConfigurationSection
) {
    if (this is TextComponent) {
        content(content().renderTemplates(config))
    }

    for (child in children()) {
        child.renderTemplates(config)
    }
}

fun Iterable<Component>.renderTemplatesAll(config: ConfigurationSection) = forEach { component ->
    component.renderTemplates(config)
}