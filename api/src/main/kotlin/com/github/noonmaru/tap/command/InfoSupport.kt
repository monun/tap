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

import org.bukkit.ChatColor
import kotlin.math.min

fun pageHeader(
    lineColor: ChatColor = ChatColor.RED,
    line: String = "────────",
    nameColor: ChatColor = ChatColor.GOLD,
    name: String,
    page: Int,
    total: Int,
    size: Int
): String {
    return "$lineColor$line $nameColor$name ${ChatColor.RED}[ ${ChatColor.AQUA}$page ${ChatColor.RESET}/ ${ChatColor.GRAY}$total ${ChatColor.RESET}] ${ChatColor.LIGHT_PURPLE} $size $lineColor$line"
}

fun <T> pageInformation(
    lineColor: ChatColor = ChatColor.RED,
    line: String = "────────",
    nameColor: ChatColor = ChatColor.GOLD,
    name: String,
    list: List<T>,
    describer: (index: Int, o: T) -> String,
    _page: Int,
    length: Int
): List<String> {
    val size = list.size
    val total = (size - 1) / length
    val page = if (_page > total) total else if (_page < 0) 0 else _page
    var index = page * length
    val end = min((page + 1) * length, size)
    val info = ArrayList<String>(end - index + 1)

    info += pageHeader(lineColor, line, nameColor, name, page, total, size)

    while (index < end) {
        info += describer.invoke(index, list[index])
        index++
    }

    return info
}