/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.fake

import com.github.noonmaru.tap.loader.LibraryLoader
import org.bukkit.World
import org.bukkit.entity.Entity

/**
 * @author Nemo
 */
interface FakeSupport {

    fun <T : Entity> createEntity(entityClass: Class<out Entity>): T?

    fun setPositionAndRotation(entity: Entity, world: World, x: Double, y: Double, z: Double, yaw: Float, pitch: Float)

    fun setInvisible(entity: Entity, invisible: Boolean)

    fun isInvisible(entity: Entity): Boolean
}

internal val NMS = LibraryLoader.load(FakeSupport::class.java)

fun <T : Entity> Class<T>.createFakeEntity(): T? {
    return NMS.createEntity(this)
}

var Entity.invisible
    get() = NMS.isInvisible(this)
    set(value) {
        NMS.setInvisible(this, value)
    }

fun Entity.setPositionAndRotation(world: World, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
    NMS.setPositionAndRotation(this, world, x, y, z, yaw, pitch)
}