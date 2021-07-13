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

internal object EnumSupport {
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun valueOf(enumType: Class<*>, name: String): Enum<*> {
        @Suppress("TYPE_MISMATCH_WARNING")
        return java.lang.Enum.valueOf(enumType as Class<out Enum<*>>, name)
    }

    @JvmStatic
    fun name(value: Any): String {
        return (value as Enum<*>).name
    }
}