/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.monun.tap.fake

import io.github.monun.tap.fake.internal.FakeEntityServerImpl
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

interface FakeEntityServer {
    companion object {
        fun create(plugin: JavaPlugin): FakeEntityServer {
            return FakeEntityServerImpl(plugin)
        }
    }

    val entities: List<FakeEntity>

    fun spawnEntity(location: Location, clazz: Class<out Entity>): FakeEntity

    fun spawnFallingBlock(location: Location, blockData: BlockData): FakeEntity

    fun dropItem(location: Location, item: ItemStack): FakeEntity

    fun addPlayer(player: Player)

    fun removePlayer(player: Player)

    fun update()

    fun clear()

    fun shutdown()
}