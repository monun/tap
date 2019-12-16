/*
 * Copyright (c) 2019 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.v1_12_R1.profile;

import com.github.noonmaru.tap.profile.Profile;
import com.github.noonmaru.tap.profile.ProfileSupport;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.UserCache;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

import java.util.UUID;

public final class NMSProfileSupport implements ProfileSupport
{

    private final UserCache userCache = ((CraftServer) Bukkit.getServer()).getServer().getUserCache();

    @Override
    public Profile getProfile(String name)
    {
        GameProfile profile = userCache.getProfile(name);

        return profile == null ? null : new NMSProfile(profile);
    }

    @Override
    public Profile getProfile(UUID uniqueId)
    {
        GameProfile profile = userCache.a(uniqueId);

        return profile == null ? null : new NMSProfile(profile);
    }

}
