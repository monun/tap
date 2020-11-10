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

package com.github.noonmaru.tap.v1_16_R2.fake

import com.github.noonmaru.tap.fake.FakeSupport
import net.minecraft.server.v1_16_R3.EntityFallingBlock
import net.minecraft.server.v1_16_R3.IRegistry
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock

/**
 * @author Nemo
 */
class NMSFakeSupport : FakeSupport {

    override fun getNetworkId(entity: Entity): Int {
        entity as CraftEntity

        return IRegistry.ENTITY_TYPE.a(entity.handle.entityType)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> createEntity(entityClass: Class<out Entity>, world: World): T? {
        return NMSEntityTypes.findType(entityClass)?.run {
            val nmsWorld = (world as CraftWorld).handle
            this.a(nmsWorld)?.bukkitEntity as T
        }
    }

    override fun isInvisible(entity: Entity): Boolean {
        entity as CraftEntity
        val nmsEntity = entity.handle

        return nmsEntity.isInvisible
    }

    override fun setInvisible(entity: Entity, invisible: Boolean) {
        entity as CraftEntity
        val nmsEntity = entity.handle

        nmsEntity.isInvisible = invisible
    }

    override fun setLocation(
        entity: Entity,
        loc: Location
    ) {
        entity as CraftEntity
        val nmsEntity = entity.handle

        loc.run {
            nmsEntity.world = (world as CraftWorld).handle
            nmsEntity.setPositionRotation(x, y, z, yaw, pitch)
        }
    }

    override fun getMountedYOffset(entity: Entity): Double {
        entity as CraftEntity

        return entity.handle.bb()
    }

    override fun getYOffset(entity: Entity): Double {
        entity as CraftEntity

        return entity.handle.bb()
    }

    override fun createSpawnPacket(entity: Entity): Any {
        entity as CraftEntity

        return entity.handle.P()
    }

    override fun createFallingBlock(blockData: BlockData): FallingBlock {
        val entity =
            EntityFallingBlock(
                (Bukkit.getWorlds().first() as CraftWorld).handle,
                0.0,
                0.0,
                0.0,
                (blockData as CraftBlockData).state
            )
        entity.ticksLived = 1

        return entity.bukkitEntity as FallingBlock
    }
}