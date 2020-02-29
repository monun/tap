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
import org.bukkit.block.data.BlockData
import org.bukkit.entity.*
import java.util.*

class FakeEntityManager : Runnable {

    private val entities = HashSet<FakeEntity>()

    private val queue = ArrayDeque<FakeEntity>()

    private val trackerQueue = ArrayDeque<FakeEntity>()

    private var trackerCount = 0.0

    private var trackerUpdatePerTick = 0.0

    private val _players = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())

    val players: Set<Player>
        get() = _players

    fun createFakeEntity(
        loc: Location,
        entityClass: Class<out Entity>
    ): FakeEntity {
        val entity =
            entityClass.createFakeEntity() ?: throw NullPointerException("Cannot create FakeEntity for $entityClass")
        entity.setPositionAndRotation(loc)
        val fake = entity.toFake()
        fake.manager = this

        entities.add(fake)
        fake.updateTrackers()

        return fake
    }

    inline fun <reified T : FakeEntity> createFakeEntity(loc: Location): T {
        val type: Class<out Entity> = when (T::class.java) {
            FakeArmorStand::class.java -> ArmorStand::class.java
            else -> throw IllegalArgumentException("Unsupported FakeEntity ${T::class.java}")
        }

        return createFakeEntity(loc, type) as T
    }

    @Throws(IllegalArgumentException::class)
    fun createFallingBlock(
        loc: Location,
        data: BlockData
    ): FakeFallingBlock {
        val fallingBlock = createFallingBlock(data).apply {
            setPositionAndRotation(loc)
        }

        return addFakeEntity(fallingBlock)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : FakeEntity> addFakeEntity(entity: Entity): T {
        val fake = entity.toFake()

        entities += fake
        fake.updateTrackers()

        return fake as T
    }

    private fun Entity.toFake(): FakeEntity {
        return when (this) {
            is FallingBlock -> FakeFallingBlock(this)
            is ArmorStand -> FakeArmorStand(this)
            is LivingEntity -> FakeLivingEntity(this)
            else -> FakeEntity(this)
        }.apply {
            manager = this@FakeEntityManager
        }
    }

    fun addPlayer(player: Player) {
        _players.add(player)
    }

    fun removePlayer(player: Player) {
        _players.remove(player)

        entities.forEach { it.removeTracker(player) }
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
        while (true) {
            queue.poll()?.also { entity ->
                if (entity.valid) {
                    entity.onUpdate()
                }
            } ?: break
        }
    }

    private fun updateTrackers() {
        if (trackerQueue.isEmpty()) {
            recalculateUpdatePerTick()
            trackerQueue.addAll(entities)
        }

        var count = trackerUpdatePerTick.let { trackerCount += it; trackerCount }.toInt()
//        var count = 1
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
        trackerUpdatePerTick = entities.count() / 32.0
    }

    fun destroyAll() {
        entities.forEach {
            it.remove()
        }

        entities.clear()
        queue.clear()
        trackerQueue.clear()
    }
}

