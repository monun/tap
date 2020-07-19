/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.fake

import com.github.noonmaru.tap.math.copy
import com.github.noonmaru.tap.math.vector
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.*

open class FakeProjectile(
    maxTicks: Int,
    range: Double
) {
    private lateinit var _previousLocation: Location
    private lateinit var _location: Location
    private lateinit var _targetLocation: Location
    private val _velocity: Vector = Vector()

    val previousLocation: Location
        get() = _previousLocation.clone()

    val location: Location
        get() = _location.clone()

    var targetLocation: Location
        get() = _targetLocation.clone()
        set(value) = _targetLocation.copy(value)

    var velocity: Vector
        get() = _velocity.clone()
        set(value) {
            _velocity.copy(value)

            if (this::_targetLocation.isInitialized) {
                _targetLocation.apply {
                    copy(_location)
                    add(value)
                }
            }
        }

    var ticks: Int = 0
        private set

    var maxTicks: Int = maxTicks

    var range: Double = range

    var distanceFlown: Double = 0.0
        private set

    val availableRange: Double
        get() = range - distanceFlown

    var launched: Boolean = false
        internal set

    var isValid: Boolean = true
        private set

    var passenger: Passenger? = null
        private set

    private val trailQueue = ArrayDeque<Trail>(2)

    internal fun init(location: Location) {
        _previousLocation = location.clone()
        _location = location.clone()
        _targetLocation = location.clone().add(_velocity)
    }

    fun setPassenger(fakeEntity: FakeEntity, applier: ((passenger: FakeEntity, to: Location) -> Unit)? = null) {
        checkState()

        passenger = Passenger(fakeEntity, applier)
    }

    fun removePassenger() {
        passenger = null
    }

    internal fun update() {
        if (!isValid) return

        ticks++

        runCatching { onPreUpdate() }

        val previous = _previousLocation
        val current = _location
        val target = _targetLocation
        var vector: Vector? = null

        val movement = Movement(current.clone(), target.clone())

        onMove(movement)
        target.copy(movement.to)
        previous.copy(current)
        current.copy(target)

        if (previous.world === current.world) {
            vector = previous vector current
            current.direction = vector

            distanceFlown += previous.distance(current)
        }

        passenger?.let { passenger ->
            val entity = passenger.fakeEntity

            if (entity.dead) {
                this.passenger = null
            } else {
                val applier = passenger.applier

                if (applier == null) {
                    passenger.fakeEntity.moveTo(current)
                } else {
                    applier(entity, current.clone())
                }
            }
        }

        var mortal = false

        if (ticks >= maxTicks || distanceFlown >= range) {
            // 비행시간을 모두 소모하거나 최대 사거리를 넘은경우 제거
            mortal = true
        } else {
            // 다음 틱 이동 준비
            var velocity = _velocity
            var speed = velocity.length()
            val availableRange = availableRange

            //남은 사거리가 현재 속력보다 작을경우 최대 사거리를 넘지 않기 위해 속력을 남은 사거리로 보정
            if (availableRange < speed) {
                speed = availableRange
                velocity = velocity.clone().apply {
                    x /= speed
                    y /= speed
                    z /= speed
                    multiply(availableRange)
                }
            }

            target.add(velocity)
        }

        val trailQueue = this.trailQueue

        trailQueue += Trail(previous.clone(), current.clone(), vector)

        while (trailQueue.count() > 3) {
            val trail = trailQueue.remove()

            onTrail(trail)
        }

        if (mortal)
            remove()

        runCatching { onPostUpdate() }
    }

    fun remove() {
        if (isValid) {
            isValid = false
            trailQueue.clear()

            runCatching { onRemove() }

            passenger = null
        }
    }

    protected open fun onPreUpdate() {}
    protected open fun onPostUpdate() {}
    protected open fun onMove(movement: Movement) {}
    protected open fun onTrail(trail: Trail) {}
    protected open fun onRemove() {}

    fun checkState() {
        require(isValid) { "Invalid ${this.javaClass.simpleName}@${System.identityHashCode(this).toString(0x10)}" }
    }
}

class Passenger(
    val fakeEntity: FakeEntity,
    val applier: ((passenger: FakeEntity, to: Location) -> Unit)?
)

class Movement(
    var from: Location,
    var to: Location
)

class Trail(
    val from: Location,
    val to: Location,
    val velocity: Vector?
)