/*
 * Copyright (c) $date.year Noonmaru
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

package com.github.noonmaru.tap.parallel

import org.bukkit.util.Vector

abstract class ParallelEntity() {
    lateinit var world: ParallelWorld
        internal set

    var x = 0.0
        private set

    var y = 0.0
        private set

    var z = 0.0
        private set

    var yaw = 0.0F
        private set

    var pitch = 0.0F
        private set

    var valid = true
        private set

    constructor(x: Double, y: Double, z: Double, yaw: Float = 0.0F, pitch: Float = 0.0F) {
        this.x = x
        this.y = y
        this.z = z
        this.yaw = yaw
        this.pitch = pitch
    }

    fun moveTo(
        world: ParallelWorld,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float = this.yaw,
        pitch: Float = this.pitch
    ) {
        checkWorld()

        val oldWorld = this.world
        if (oldWorld == world) {
            world.moveEntity(this, x, y, z)
        } else {
            oldWorld.removeEntity(this)
            world.addEntity(this, x, y, z)
        }

        this.yaw = yaw
        this.pitch = pitch
    }

    fun move(deltaX: Double, deltaY: Double, deltaZ: Double, yaw: Float = this.yaw, pitch: Float = this.pitch) {
        moveTo(world, this.x + deltaX, this.y + deltaY, this.z + deltaZ, yaw, pitch)
    }

    fun move(v: Vector, yaw: Float = this.yaw, pitch: Float = this.pitch) {
        move(v.x, v.y, v.z, yaw, pitch)
    }

    fun checkWorld() {
        check(this::world.isInitialized) {
            "Inactivated ${this.javaClass.simpleName}@${System.identityHashCode(this).toString(0x10)}"
        }
    }

    fun checkState() {
        check(valid) { "Invalid ${this.javaClass.simpleName}@${System.identityHashCode(this).toString(0x10)}" }
    }
}