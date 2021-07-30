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

import io.github.monun.tap.ref.Weaky

/**
 * Ticker에 등록된 태스크입니다.
 *
 * @see Ticker
 */
class TickerTask internal constructor(
    ticker: Ticker,
    val runnable: Runnable
) : Comparable<TickerTask> {
    companion object {
        internal const val ERROR = 0L
        internal const val NO_REPEATING = -1L
        internal const val CANCEL = -2L
        internal const val DONE = -3L
    }

    private val schedulerRef = Weaky(ticker)

    /**
     * 태스크를 등록한 Ticker 객체입니다
     */
    val ticker: Ticker
        get() = schedulerRef.get()

    internal var nextRun: Long = -1L

    /**
     * 태스크 등록시 설정한 반복 tick입니다
     */
    var period: Long = 0L
        internal set

    /**
     * 등록된 태스크
     */
    val isScheduled: Boolean
        get() = period.let { it != ERROR && it > CANCEL }

    /**
     * 취소된 태스크
     */
    val isCancelled
        get() = period == CANCEL

    /**
     * 실행이 완료된 태스크
     */
    val isDone
        get() = period == DONE

    internal fun execute() {
        runnable.runCatching { run() }
    }

    /**
     * 태스크를 해제합니다.
     *
     * 등록되지 태스크의 경우 아무것도 실행하지 않습니다.
     */
    fun cancel() {
        if (!isScheduled) return
        period = CANCEL

        // 실행까지 남은 틱이 Ticker의 removeDelay 값보다 이상일때 즉시 제거
        // 아닐경우 tick 흐름에 따라 자연스럽게 제거
        val ticker = ticker
        if (nextRun - ticker.currentTicks >= ticker.removeDelay)
            ticker.remove(this)
    }

    override fun compareTo(other: TickerTask): Int {
        return this.nextRun.compareTo(other.nextRun)
    }
}

