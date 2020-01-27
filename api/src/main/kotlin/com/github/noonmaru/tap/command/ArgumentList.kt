/*
 * Copyright (c) 2019 Noonmaru
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

import org.apache.commons.lang.StringUtils
import java.util.*

class ArgumentList(private val arguments: Array<out String>, cursor: Int) {

    private var cursor: Int = cursor
        set(value) {
            if (value < 0 || value > arguments.size) throw ArrayIndexOutOfBoundsException(value)
            field = value
        }

    fun isLast(): Boolean {
        return remain() == 1
    }

    fun remain(): Int {
        return arguments.size - cursor
    }

    fun isEmpty(): Boolean {
        return arguments.isEmpty()
    }

    operator fun hasNext(): Boolean {
        return cursor < arguments.size
    }

    operator fun next(): String {
        if (cursor >= arguments.size) throw NoSuchElementException()
        return arguments[cursor++]
    }

    fun joinToString(separator: String = " "): String {
        return StringUtils.join(arguments, separator, cursor, arguments.size)
    }
}