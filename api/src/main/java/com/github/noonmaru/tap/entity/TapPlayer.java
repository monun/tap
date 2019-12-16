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

package com.github.noonmaru.tap.entity;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.inventory.TapPlayerInventory;
import com.github.noonmaru.tap.item.TapItemStack;
import org.bukkit.entity.Player;

public interface TapPlayer extends TapLivingEntity
{

    static TapPlayer wrapPlayer(Player player)
    {
        return Tap.ENTITY.wrapEntity(player);
    }

    Player getBukkitEntity();

    int getLevel();

    int getFoodLevel();

    TapPlayerInventory getInventory();

    TapItemStack getHeldItemMainHand();

    TapItemStack getHeldItemOffHand();

}
