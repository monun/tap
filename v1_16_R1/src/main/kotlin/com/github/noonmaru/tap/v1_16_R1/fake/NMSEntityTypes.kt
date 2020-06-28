/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.v1_16_R1.fake

import net.minecraft.server.v1_16_R1.EntityTypes
import net.minecraft.server.v1_16_R1.IRegistry
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_16_R1.CraftServer
import java.util.*
import org.bukkit.entity.Entity as BukkitEntity

/**
 * @author Nemo
 */
internal object NMSEntityTypes {

    private val ENTITIES: MutableMap<Class<out BukkitEntity>, EntityTypes<*>> = HashMap()

    init {
        val world = (Bukkit.getServer() as CraftServer).server.worlds.first()

        IRegistry.ENTITY_TYPE.forEach { type ->
            type.a(world)?.let { entity ->
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

    fun findType(bukkitClass: Class<out BukkitEntity>): EntityTypes<*>? {
        return ENTITIES[bukkitClass]
    }

}