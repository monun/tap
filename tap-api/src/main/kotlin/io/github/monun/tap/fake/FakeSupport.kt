/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.fake

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import io.github.monun.tap.loader.LibraryLoader
import io.github.monun.tap.protocol.PacketContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

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

    fun createSpawnPacket(entity: Entity): Array<out PacketContainer>

    fun createFallingBlock(blockData: BlockData): FallingBlock

    fun createItemEntity(item: ItemStack): Item
    fun createPlayerEntity(
        name: String,
        profileProperties: Set<ProfileProperty>,
        skinParts: FakeSkinParts,
        uniqueId: UUID
    ): Player

    fun setSkinParts(player: Player, raw: Int)
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

fun Entity.createSpawnPacket(): Array<out PacketContainer> {
    return FakeSupportNMS.createSpawnPacket(this)
}

fun BlockData.createFallingBlock(): FallingBlock {
    return FakeSupportNMS.createFallingBlock(this)
}

fun ItemStack.createItemEntity(): Item {
    return FakeSupportNMS.createItemEntity(this)
}

fun PlayerProfile.createPlayerEntity(
    name: String = this.name ?: error("PlayerProfile.name is null"),
    profileProperties: Set<ProfileProperty> = this.properties,
    skinParts: FakeSkinParts = FakeSkinParts().apply { enableAll() },
    uniqueId: UUID = UUID.randomUUID()
): Player {
    return FakeSupportNMS.createPlayerEntity(name, profileProperties, skinParts, uniqueId)
}

fun createPlayerEntity(
    name: String,
    profileProperties: Set<ProfileProperty>,
    skinParts: FakeSkinParts,
    uniqueId: UUID = UUID.randomUUID()
): Player {
    return FakeSupportNMS.createPlayerEntity(name, profileProperties, skinParts, uniqueId)
}