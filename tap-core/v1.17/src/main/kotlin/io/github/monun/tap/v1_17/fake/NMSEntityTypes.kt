/*
 *
 *  * Copyright 2021 Monun
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package io.github.monun.tap.v1_17.fake

import net.minecraft.core.Registry
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.entity.Entity as BukkitEntity

/**
 * @author Nemo
 */
internal object NMSEntityTypes {

    private val ENTITIES = HashMap<Class<out BukkitEntity>, EntityType<*>>()

    init {
        val server: MinecraftServer = (Bukkit.getServer() as CraftServer).server
        val level = server.allLevels.first()

        Registry.ENTITY_TYPE.forEach { type ->
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