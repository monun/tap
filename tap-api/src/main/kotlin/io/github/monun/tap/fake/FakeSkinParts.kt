/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.fake

import com.destroystokyo.paper.SkinParts

class FakeSkinParts(
    raw: Int = 0b1111111
) {
    companion object {
        fun from(parts: SkinParts) = FakeSkinParts(parts.raw)
    }

    var raw: Int = raw
        private set

    fun has(part: SkinPart): Boolean {
        return raw and part.bit != 0
    }

    fun enable(part: SkinPart) {
        raw = raw or part.bit
    }

    operator fun plusAssign(part: SkinPart) {
        enable(part)
    }

    fun disable(part: SkinPart) {
        raw = raw and part.bit.inv()
    }

    operator fun minusAssign(part: SkinPart) {
        disable(part)
    }

    fun enableAll() {
        raw = 0b1111111
    }

    fun disableAll() {
        raw = 0
    }
}

enum class SkinPart {
    CAPE,
    JACKET,
    LEFT_SLEEVE,
    RIGHT_SLEEVE,
    LEFT_PANTS,
    RIGHT_PANTS,
    HATS;

    val bit: Int = 1 shl ordinal
}

