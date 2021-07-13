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

import java.util.*
import kotlin.math.max

/**
 * Tick 기반의 태스크 스케쥴러 (Tick + Timer = Ticker)
 *
 * @see TickerTask
 */
abstract class Ticker : Runnable {
    companion object {
        /**
         * 호출 횟수에 따라 tick이 증가하는 Ticker를 생성합니다.
         */
        fun plank(): Ticker {
            return Plank()
        }

        /**
         * 시간에 따라 tick이 변화하는 Ticker를 생성합니다.
         *
         * 태스크는 시간 지연 관계없이 tick과 동기화되어 실행됩니다.
         */
        fun precision(tick: Pair<() -> Long, Long> = System::nanoTime to 50L * 1000L * 1000L): Ticker {
            return Precision(tick.first, tick.second)
        }

        /**
         * 시간에 따라 tick이 변화하는 Ticker를 생성합니다.
         *
         * 태스크는 시간 지연에 따라 한번만 호출됩니다.
         */
        fun flex(tick: Pair<() -> Long, Long> = System::nanoTime to 50L * 1000L * 1000L): Ticker {
            return Flex(tick.first, tick.second)
        }
    }

    /**
     * Ticker 내부에서 사용하는 tick입니다.
     */
    abstract val currentTicks: Long

    protected val queue = PriorityQueue<TickerTask>()

    var removeDelay = 0xFF

    protected open fun TickerTask.calculateNextRun() = nextRun + period

    /**
     * 지연 실행할 태스크를 등록합니다.
     *
     * 태스크는 한번만 호출된 이후 [TickerTask.isDone] 상태가 됩니다.
     */
    fun runTask(runnable: Runnable, delay: Long = 0L) = registerTask(runnable, delay, TickerTask.NO_REPEATING)

    /**
     * 반복 실행할 태스크를 등록합니다.
     *
     * 태스크는 지연 이후에 period tick 마다 호출됩니다.
     */
    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long) = registerTask(runnable, delay, max(1L, period))

    private fun registerTask(runnable: Runnable, delay: Long, period: Long) = TickerTask(this, runnable).apply {
        this.nextRun = currentTicks + max(0L, delay)
        this.period = period
    }.also(queue::offer)

    /**
     * 모든 태스크를 실행합니다.
     */
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

    fun cancelAll() {
        queue.apply {
            forEach { it.period = TickerTask.CANCEL }
            clear()
        }
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