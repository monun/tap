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

package com.github.noonmaru.tap.v1_12_R1.firework;


import com.github.noonmaru.tap.firework.FireworkEffect;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

public final class NMSFireworkEffect implements FireworkEffect
{

    public static final EntityFireworks ENTITY_FIRE_WORK = new EntityFireworks(((CraftServer) Bukkit.getServer()).getServer().getWorld());

    private static final Item ITEM_FIRE_WORK = Item.getById(401);

    private final DataWatcher watcher;

    public NMSFireworkEffect(FireworkEffect.Builder builder)
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound fw = new NBTTagCompound();
        NBTTagCompound ex = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        fw.set("Explosions", list);
        tag.set("Fireworks", fw);
        ex.setByte("Trail", (byte) (builder.isTrail() ? 1 : 0));
        ex.setByte("Flicker", (byte) (builder.isFlicker() ? 1 : 0));
        ex.setByte("Type", builder.getType().getId());
        ex.setIntArray("Colors", builder.getColors());
        ex.setIntArray("FadeColors", builder.getFadeColors());
        list.add(ex);

        ItemStack item = new ItemStack(ITEM_FIRE_WORK, 1, 0);
        item.setTag(tag);

        DataWatcher watcher = new DataWatcher(ENTITY_FIRE_WORK);
        watcher.register(EntityFireworks.FIREWORK_ITEM, item);

        this.watcher = watcher;
    }

    public DataWatcher getHandle()
    {
        return watcher;
    }

}
