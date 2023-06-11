/*
 * Copyright (C) 2023 Monun
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

package io.github.monun.tap.v1_20.fake

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R1.CraftServer
import org.bukkit.entity.Entity as BukkitEntity

/**
 * @author Nemo
 */
internal object NMSEntityTypes {
    private val ENTITIES = HashMap<Class<out BukkitEntity>, EntityType<*>>()

    init {
        val server: MinecraftServer = (Bukkit.getServer() as CraftServer).server
        val level = server.allLevels.first()

        BuiltInRegistries.ENTITY_TYPE.forEach { type ->
            type.create(level)?.let { entity ->
                val bukkitClass = entity.bukkitEntity.javaClass
                val interfaces = bukkitClass.interfaces

                ENTITIES[bukkitClass.asSubclass(BukkitEntity::class.java)] = type

                for (i in interfaces) {
                    if (BukkitEntity::class.java.isAssignableFrom(i)) {
                        ENTITIES[i.asSubclass(BukkitEntity::class.java)] = type
                    }
                }
            }
        }
    }

    @JvmStatic
    fun findType(bukkitClass: Class<out BukkitEntity>): EntityType<*> {
        return ENTITIES[bukkitClass] ?: error("Unknown entity type $bukkitClass")
    }
}