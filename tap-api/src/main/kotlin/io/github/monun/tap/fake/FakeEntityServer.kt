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

    /**
     * 스폰거리
     *
     * [Player]와 [FakeEntity]의 거리가 [spawnDistance]보다 작을 때 클라이언트에 스폰됨
     *
     * 디스폰 거리보다 작아야함
     *
     * @exception IllegalArgumentException
     */
    var spawnDistance: Double

    /**
     * [FakeEntity] 디스폰 거리
     *
     * [Player]와 [FakeEntity]의 거리가 [spawnDistance]보다 클 때 클라이언트에서 디스폰됨
     *
     * 스폰 거리보다 커야함
     *
     * @exception IllegalArgumentException
     */
    var despawnDistance: Double

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