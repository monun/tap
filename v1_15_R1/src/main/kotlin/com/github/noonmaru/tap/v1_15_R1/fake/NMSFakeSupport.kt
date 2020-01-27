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

package com.github.noonmaru.tap.v1_15_R1.fake

import com.github.noonmaru.tap.fake.FakeSupport
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.v1_15_R1.CraftServer
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.Entity

/**
 * @author Nemo
 */
class NMSFakeSupport : FakeSupport {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> createEntity(entityClass: Class<out Entity>): T? {
        return NMSEntityTypes.findType(entityClass)?.run {
            val world = (Bukkit.getServer() as CraftServer).server.worlds.first()
            this.a(world)?.bukkitEntity as T
        }
    }

    override fun isInvisible(entity: Entity): Boolean {
        entity as CraftPlayer
        val nmsEntity = entity.handle

        return nmsEntity.isInvisible
    }

    override fun setInvisible(entity: Entity, invisible: Boolean) {
        entity as CraftPlayer
        val nmsEntity = entity.handle

        nmsEntity.isInvisible = invisible
    }

    override fun setPositionAndRotation(
        entity: Entity,
        world: World,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float
    ) {
        entity as CraftPlayer
        val nmsEntity = entity.handle

        nmsEntity.setPositionRotation(x, y, z, yaw, pitch)
    }
}