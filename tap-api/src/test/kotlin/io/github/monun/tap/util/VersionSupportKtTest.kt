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

package io.github.monun.tap.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class VersionSupportKtTest {
    @Test
    fun test() {
        assertEquals(0, "1".compareVersion("1"))
        assertEquals(0, "1.0".compareVersion("1.0"))
        assertEquals(0, "1.0.0".compareVersion("1.0.0"))
        assertEquals(0, "0.01.0".compareVersion("0.1.0"))
        assertEquals(-1, "0".compareVersion("1"))
        assertEquals(-1, "0".compareVersion("0.1"))
        assertEquals(-1, "0.1".compareVersion("0.2"))
        assertEquals(-1, "0.0.1".compareVersion("0.1"))
        assertEquals(-1, "0.1.0".compareVersion("0.012.0"))
        assertEquals(1, "1".compareVersion("0"))
        assertEquals(1, "1".compareVersion("0.1"))
        assertEquals(1, "0.1".compareVersion("0.0.1"))
        assertEquals(1, "0.2.0".compareVersion("0.01.0"))
    }
}