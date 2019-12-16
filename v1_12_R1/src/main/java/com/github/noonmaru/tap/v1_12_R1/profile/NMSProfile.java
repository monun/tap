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
import com.mojang.authlib.GameProfile;

import java.util.UUID;

public final class NMSProfile implements Profile
{

    private final GameProfile profile;

    NMSProfile(GameProfile profile)
    {
        this.profile = profile;
    }

    @Override
    public UUID getUniqueId()
    {
        return this.profile.getId();
    }

    @Override
    public String getName()
    {
        return this.profile.getName();
    }

    @Override
    public boolean isLegacy()
    {
        return this.profile.isLegacy();
    }

    public GameProfile getHandle()
    {
        return profile;
    }

    @Override
    public String toString()
    {
        return this.profile.toString();
    }


}
