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

package io.github.monun.tap.task

import org.junit.jupiter.api.Test
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