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

import io.github.monun.tap.ref.UpstreamReference

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

    private val schedulerRef = UpstreamReference(ticker)

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

