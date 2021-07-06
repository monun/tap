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

package io.github.monun.tap.util

import org.junit.Assert.assertEquals
import org.junit.Test

class VersionSupportKtTest {
    @Test
    fun test() {
        assertEquals(0, "1".compareVersion("1"))
        assertEquals(0, "1.0".compareVersion("1.0"))
        assertEquals(0, "v1.0.0".compareVersion("v1.0.0"))
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