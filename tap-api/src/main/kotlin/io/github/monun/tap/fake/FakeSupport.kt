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

package io.github.monun.tap.fake

import io.github.monun.tap.loader.LibraryLoader
import io.github.monun.tap.protocol.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

/**
 * @author Nemo
 */
interface FakeSupport {
    fun getNetworkId(entity: Entity): Int

    fun <T : Entity> createEntity(entityClass: Class<out Entity>, world: World): T

    fun setLocation(entity: Entity, loc: Location)

    fun setInvisible(entity: Entity, invisible: Boolean)

    fun isInvisible(entity: Entity): Boolean

    fun getMountedYOffset(entity: Entity): Double

    fun getYOffset(entity: Entity): Double

    fun createSpawnPacket(entity: Entity): PacketContainer

    fun createFallingBlock(blockData: BlockData): FallingBlock

    fun createItemEntity(item: ItemStack): Item
}

internal val FakeSupportNMS = LibraryLoader.loadNMS(FakeSupport::class.java)

val Entity.networkId
    get() = FakeSupportNMS.getNetworkId(this)

var Entity.invisible
    get() = FakeSupportNMS.isInvisible(this)
    set(value) {
        FakeSupportNMS.setInvisible(this, value)
    }

val Entity.mountedYOffset
    get() = FakeSupportNMS.getMountedYOffset(this)

val Entity.yOffset
    get() = FakeSupportNMS.getYOffset(this)

//this.locY() + this.aS() + entity.aR()

fun <T : Entity> Class<T>.createFakeEntity(world: World = Bukkit.getWorlds().first()): T {
    return FakeSupportNMS.createEntity(this, world)
}

fun Entity.setLocation(loc: Location) {
    FakeSupportNMS.setLocation(this, loc)
}

fun Entity.createSpawnPacket(): PacketContainer {
    return FakeSupportNMS.createSpawnPacket(this)
}

fun createFallingBlock(blockData: BlockData): FallingBlock {
    return FakeSupportNMS.createFallingBlock(blockData)
}

fun createItemEntity(item: ItemStack): Item {
    return FakeSupportNMS.createItemEntity(item)
}