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

package com.github.noonmaru.tap.command

import org.bukkit.command.CommandSender

/**
 * @author Nemo
 */
interface CommandComponent {

    val argsCount: Int
        get() = 0

    fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean

    fun onTabComplete(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): List<String> {
        return emptyList()
    }

    fun test(sender: CommandSender): (() -> String)? {
        return null
    }
}