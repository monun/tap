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

package io.github.monun.tap.task

import org.junit.Test
import org.mockito.Mockito.*

class TickerTest {
    @Test
    fun testPlank() {
        val runTask = mock(Runnable::class.java)
        val runTaskLater = mock(Runnable::class.java)
        val runTaskTimer = mock(Runnable::class.java)
        val tickerPlank = Ticker.plank()

        tickerPlank.apply {
            runTask(runTask)
            runTask(runTaskLater, 1L)
            runTaskTimer(runTaskTimer, 1L, 1L)
        }

        tickerPlank.run()
        verify(runTask).run()
        verifyNoInteractions(runTaskLater, runTaskTimer)

        tickerPlank.run()
        verifyNoMoreInteractions(runTask)
        verify(runTaskLater).run()
        verify(runTaskTimer).run()

        tickerPlank.run()
        verifyNoMoreInteractions(runTask)
        verifyNoMoreInteractions(runTaskLater)
        verify(runTaskTimer, times(2)).run()
    }

    @Test
    fun testPrecision() {
        val tickMillis = 10L
        val runTaskLater = mock(Runnable::class.java)
        val runTaskTimer = mock(Runnable::class.java)

        val tickerPrecision = Ticker.precision(System::nanoTime to tickMillis * 1000L * 1000L)
        tickerPrecision.apply {
            runTask(runTaskLater, 5L)
            runTaskTimer(runTaskTimer, 1L, 1L)
        }
        Thread.sleep(tickMillis * 10L)
        tickerPrecision.run()

        verify(runTaskLater).run()
        verify(runTaskTimer, times(tickerPrecision.currentTicks.toInt())).run() // 틱마다 모두 호출
    }

    @Test
    fun testFlex() {
        val tickMillis = 10L
        val runTaskLater = mock(Runnable::class.java)
        val runTaskTimer = mock(Runnable::class.java)

        val tickerFlex = Ticker.flex(System::nanoTime to tickMillis * 1000L * 1000L)
        tickerFlex.apply {
            runTask(runTaskLater, 5L)
            runTaskTimer(runTaskTimer, 1L, 1L)
        }
        Thread.sleep(tickMillis * 10L)
        tickerFlex.run()

        verify(runTaskLater).run()
        verify(runTaskTimer).run()
    }
}