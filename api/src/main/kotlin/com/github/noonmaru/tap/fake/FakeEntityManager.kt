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

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.*

class FakeEntityManager : Runnable {

    private val entities = HashSet<FakeEntity>()

    private val queue = ArrayDeque<FakeEntity>()

    private val trackerQueue = ArrayDeque<FakeEntity>()

    private var trackerCount = 0.0

    private var trackerUpdatePerTick = 0.0

    fun createFakeEntity(
        world: World,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        entityClass: Class<out Entity>
    ): FakeEntity {
        val entity =
            entityClass.createFakeEntity() ?: throw NullPointerException("Cannot create Entity for $entityClass")
        entity.setPositionAndRotation(world, x, y, z, yaw, pitch)
        val fake = entity.toFake()
        fake.manager = this

        entities.add(fake)
        fake.updateTrackers()

        return fake
    }

    fun createFakeEntity(loc: Location, entityClass: Class<out Entity>): FakeEntity {
        return createFakeEntity(loc.world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch, entityClass)
    }

    internal fun enqueue(entity: FakeEntity) {
        queue.offer(entity)
    }

    /**
     * 이 메서드를 Tick마다 호출해주세요!
     */
    override fun run() {
        updateEntities()
        updateTrackers()
    }

    private fun updateEntities() {
        var entity: FakeEntity
        while (queue.poll().also { entity = it } != null) {
            if (entity.valid) {
                entity.onUpdate()
            }
        }
    }

    private fun updateTrackers() {
        if (trackerQueue.isEmpty()) {
            recalculateUpdatePerTick()
            trackerQueue.addAll(entities)
        }

        var count = trackerUpdatePerTick.let { trackerCount += it; trackerCount }.toInt()
        trackerCount -= count.toDouble()

        while (count > 0) {
            trackerQueue.poll()?.also { entity ->
                if (entity.valid) {
                    count--
                    entity.updateTrackers()
                } else {
                    entities.remove(entity)
                }
            } ?: break
        }
    }

    private fun recalculateUpdatePerTick() {
        trackerCount = 0.0
        trackerUpdatePerTick = entities.count() / 40.0
    }
}

private fun Entity.toFake(): FakeEntity {
    return when (this) {
        is ArmorStand -> FakeArmorStand(this)
        is LivingEntity -> FakeLivingEntity(this)
        else -> throw UnsupportedOperationException("Unsupported Entity $this")
    }
}