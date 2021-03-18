/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.monun.tap.task

import java.util.*
import kotlin.math.max

abstract class Ticker : Runnable {
    companion object {
        fun plank(): Ticker {
            return Plank()
        }

        fun precision(tick: Pair<() -> Long, Long> = System::nanoTime to 50L * 1000L * 1000L): Ticker {
            return Precision(tick.first, tick.second)
        }

        fun flex(tick: Pair<() -> Long, Long> = System::nanoTime to 50L * 1000L * 1000L): Ticker {
            return Flex(tick.first, tick.second)
        }
    }

    abstract val currentTicks: Long

    protected val queue = PriorityQueue<TickerTask>()

    protected open fun TickerTask.calculateNextRun() = nextRun + period

    fun runTask(runnable: Runnable, delay: Long = 0L) = registerTask(runnable, delay, TickerTask.NO_REPEATING)

    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long) = registerTask(runnable, delay, max(1L, period))

    private fun registerTask(runnable: Runnable, delay: Long, period: Long) = TickerTask(this, runnable).apply {
        this.nextRun = currentTicks + max(0L, delay)
        this.period = period
    }.also(queue::offer)

    override fun run() {
        val queue = queue
        while (queue.peek().let { it != null && it.nextRun <= currentTicks }) {
            val task = queue.remove()

            if (task.isScheduled) task.run {
                execute()

                if (period > 0L) {
                    nextRun = task.calculateNextRun()
                    queue.offer(task)
                } else {
                    period = TickerTask.DONE
                }
            }
        }
    }

    internal fun remove(task: TickerTask) {
        queue.remove(task)
    }

    private class Plank : Ticker() {
        override var currentTicks = 0L

        override fun run() {
            super.run()
            currentTicks++
        }
    }

    private class Precision(
        private val time: () -> Long,
        private val tickTime: Long
    ) : Ticker() {
        private val currentTicksByTime = time()
            get() = (time() - field) / tickTime

        override var currentTicks = 0L

        override fun run() {
            super.run()

            while (currentTicks < currentTicksByTime) {
                if (queue.isEmpty()) {
                    currentTicks = currentTicksByTime
                    return
                }
                currentTicks++
                super.run()
            }
        }
    }

    private class Flex(
        private val time: () -> Long,
        private val tickTime: Long
    ) : Ticker() {
        override val currentTicks = time()
            get() = (time() - field) / tickTime

        override fun TickerTask.calculateNextRun() = currentTicks + period
    }
}