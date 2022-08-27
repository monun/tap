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
 *
 * Modified - octomarine
 */

package io.github.monun.tap.fake

import com.destroystokyo.paper.profile.ProfileProperty
import io.github.monun.tap.loader.LibraryLoader
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

interface FakeEntityServer {
    companion object : FakeInternal by LibraryLoader.loadImplement(FakeInternal::class.java)

    val entities: List<FakeEntity<*>>

    fun <T : Entity> spawnEntity(location: Location, clazz: Class<T>): FakeEntity<T>

    fun spawnFallingBlock(location: Location, blockData: BlockData): FakeEntity<FallingBlock>

    fun spawnItem(location: Location, item: ItemStack): FakeEntity<Item>

    fun spawnPlayer(
        location: Location,
        name: String,
        profileProperties: Set<ProfileProperty> = emptySet(),
        skinParts: FakeSkinParts = defaultFakeSkinParts,
        uniqueId: UUID = UUID.randomUUID()
    ): FakeEntity<Player>

    fun addPlayer(player: Player)

    fun removePlayer(player: Player)

    fun update()

    fun clear()

    fun shutdown()
}

private val defaultFakeSkinParts = FakeSkinParts()

interface FakeInternal {
    fun create(plugin: JavaPlugin): FakeEntityServer
}