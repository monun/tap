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

package com.github.noonmaru.tap.item;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.nbt.NBTCompound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public interface TapItemStack
{

    TapItem getItem();

    TapItemStack setItem(TapItem item);

    TapItemStack setItem(int itemId);

    int getId();

    int getAmount();

    TapItemStack setAmount(int amount);

    boolean isEmpty();

    int getData();

    TapItemStack setData(int data);

    String getName();

    String getUnlocalizedName();

    String getDisplayName();

    TapItemStack setDisplayName(String name);

    List<String> getLore();

    TapItemStack setLore(List<String> lore);

    boolean hasLore(Predicate<String> p);

    String findLore(Predicate<String> p);

    String findLoreLast(Predicate<String> p);

    boolean hasTag();

    NBTCompound getTag();

    TapItemStack setTag(NBTCompound tag);

    boolean hasItemMeta();

    ItemMeta getItemMeta();

    TapItemStack setItemMeta(ItemMeta meta);

    ItemStack toItemStack();

    TapItemStack addLore(List<String> lore);

    default TapItemStack addLore(String lore)
    {
        return addLore(Collections.singletonList(lore));
    }

    default TapItemStack addLore(String... lore)
    {
        return addLore(Arrays.asList(lore));
    }

    TapItemStack setUnbreakable(boolean flag);

    TapItemStack setHideFlags(int hideFlags);

    Item spawn(World world, double x, double y, double z);

    Item spawnNaturally(World world, double x, double y, double z);

    boolean isSimilar(TapItemStack other);

    TapItemStack copy();

    default NBTCompound save()
    {
        return save(Tap.NBT.newCompound());
    }

    NBTCompound save(NBTCompound compound);

}
