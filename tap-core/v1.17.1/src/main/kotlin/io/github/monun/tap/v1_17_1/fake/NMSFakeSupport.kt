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

package io.github.monun.tap.v1_17_1.fake

import io.github.monun.tap.fake.FakeSupport
import io.github.monun.tap.v1_17_1.protocol.NMSPacketContainer
import net.minecraft.core.Registry
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.item.ItemEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

/**
 * @author Nemo
 */
class NMSFakeSupport : FakeSupport {

    override fun getNetworkId(entity: Entity): Int {
        entity as CraftEntity

        return Registry.ENTITY_TYPE.getId(entity.handle.type)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> createEntity(entityClass: Class<out Entity>, world: World): T {
        return NMSEntityTypes.findType(entityClass).run {
            val nmsWorld = (world as CraftWorld).handle
            this.create(nmsWorld)?.bukkitEntity as T
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
            nmsEntity.level = (world as CraftWorld).handle
            nmsEntity.moveTo(x, y, z, yaw, pitch)
        }
    }

    override fun getMountedYOffset(entity: Entity): Double {
        entity as CraftEntity

        return entity.handle.passengersRidingOffset
    }

    override fun getYOffset(entity: Entity): Double {
        entity as CraftEntity

        return entity.handle.myRidingOffset
    }

    override fun createSpawnPacket(entity: Entity): NMSPacketContainer {
        entity as CraftEntity

        return NMSPacketContainer(entity.handle.addEntityPacket)
    }

    override fun createFallingBlock(blockData: BlockData): FallingBlock {
        val entity =
            FallingBlockEntity(
                (Bukkit.getWorlds().first() as CraftWorld).handle,
                0.0,
                0.0,
                0.0,
                (blockData as CraftBlockData).state
            )

        return entity.bukkitEntity as FallingBlock
    }

    override fun createItemEntity(item: ItemStack): Item {
        val entity =
            ItemEntity(
                (Bukkit.getWorlds().first() as CraftWorld).handle,
                0.0,
                0.0,
                0.0,
                CraftItemStack.asNMSCopy(item)
            )
        entity.setNeverPickUp()

        return entity.bukkitEntity as Item
    }
}