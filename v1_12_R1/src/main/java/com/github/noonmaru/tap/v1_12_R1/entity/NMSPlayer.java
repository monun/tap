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

package com.github.noonmaru.tap.v1_12_R1.entity;

import com.github.noonmaru.tap.entity.TapPlayer;
import com.github.noonmaru.tap.inventory.TapPlayerInventory;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.v1_12_R1.inventory.NMSPlayerInventory;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;

public class NMSPlayer extends NMSLivingEntity implements TapPlayer
{

    private final EntityPlayer player;

    private TapPlayerInventory inv;

    NMSPlayer(Entity entity)
    {
        super(entity);

        this.player = (EntityPlayer) entity;
    }

    public EntityPlayer getHandle()
    {
        return this.player;
    }

    @Override
    public Player getBukkitEntity()
    {
        return this.player.getBukkitEntity();
    }

    @Override
    public int getLevel()
    {
        return this.player.getExpToLevel();
    }

    @Override
    public int getFoodLevel()
    {
        return this.player.getFoodData().foodLevel;
    }

    @Override
    public TapPlayerInventory getInventory()
    {
        TapPlayerInventory inv = this.inv;

        return inv == null ? this.inv = new NMSPlayerInventory((CraftInventoryPlayer) this.player.getBukkitEntity().getInventory(), this) : inv;
    }

    @Override
    public TapItemStack getHeldItemMainHand()
    {
        return getInventory().getHeldItemMainHand();
    }

    @Override
    public TapItemStack getHeldItemOffHand()
    {
        return getInventory().getHeldItemOffHand();
    }

}
