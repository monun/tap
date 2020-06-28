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

package com.github.noonmaru.tap.v1_13_R2.mojang

import com.github.noonmaru.tap.mojang.MojangProfile
import com.github.noonmaru.tap.mojang.ProfileSupport
import net.minecraft.server.v1_13_R2.UserCache
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_13_R2.CraftServer
import java.util.*

/**
 * @author Nemo
 */
class NMSProfileSupport : ProfileSupport {

    private val userCache: UserCache = (Bukkit.getServer() as CraftServer).server.userCache

    override fun getProfile(name: String): MojangProfile? {
        return userCache.getProfile(name)?.let { MojangProfile(it.id, it.name) }
    }

    override fun getProfile(uniqueId: UUID): MojangProfile? {
        return userCache.a(uniqueId)?.let { MojangProfile(it.id, it.name) }
    }
}