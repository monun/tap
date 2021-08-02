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
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

interface FakeEntityServer {
    companion object: FakeInternal by LibraryLoader.loadImplement(FakeInternal::class.java)

    val entities: List<FakeEntity>

    fun spawnEntity(location: Location, clazz: Class<out Entity>): FakeEntity

    fun spawnFallingBlock(location: Location, blockData: BlockData): FakeEntity

    fun spawnItem(location: Location, item: ItemStack): FakeEntity

    fun addPlayer(player: Player)

    fun removePlayer(player: Player)

    fun update()

    fun clear()

    fun shutdown()
}

interface FakeInternal {
    fun create(plugin: JavaPlugin): FakeEntityServer
}