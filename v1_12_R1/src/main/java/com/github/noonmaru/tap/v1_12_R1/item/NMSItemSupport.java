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

package com.github.noonmaru.tap.v1_12_R1.item;


import com.github.noonmaru.tap.item.TapItem;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.item.TapItemSupport;
import com.github.noonmaru.tap.nbt.NBTCompound;
import com.github.noonmaru.tap.v1_12_R1.nbt.NMSNBTCompound;
import com.google.common.collect.Iterables;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

public final class NMSItemSupport implements TapItemSupport
{

    private static NMSItemSupport instance;

    private final Map<Item, NMSItem> items = new IdentityHashMap<>(32768);

    public NMSItemSupport()
    {
        if (instance != null)
            throw new IllegalStateException();

        instance = this;
    }

    public static NMSItemSupport getInstance()
    {
        return instance;
    }

    public static NMSItemStack wrapItemStack(ItemStack itemStack)
    {
        return itemStack == null || itemStack.isEmpty() ? NMSItemStack.EMPTY : new NMSItemStack(itemStack);
    }

    public static ItemStack unwrapItemStack(TapItemStack nmsItemStack)
    {
        return nmsItemStack == null || nmsItemStack.isEmpty() ? ItemStack.a : ((NMSItemStack) nmsItemStack).getHandle();
    }

    public NMSItem wrapItem(Item item)
    {
        if (item == null)
            return null;

        NMSItem nmsItem = items.get(item);

        if (nmsItem == null)
            items.put(item, nmsItem = new NMSItem(item));

        return nmsItem;
    }

    @Override
    public NMSItem getItem(int id)
    {
        return wrapItem(Item.getById(id));
    }

    @Override
    public NMSItem getItem(String name)
    {
        return wrapItem(Item.REGISTRY.get(new MinecraftKey(name)));
    }

    @Override
    public Iterable<NMSItem> getItems()
    {
        return Iterables.transform(Item.REGISTRY, this::wrapItem);
    }

    @Override
    public NMSItemStack newItemStack(TapItem item, int amount, int data)
    {
        return new NMSItemStack(new ItemStack(((NMSItem) item).getHandle(), amount, data));
    }

    @Override
    public NMSItemStack fromItemStack(org.bukkit.inventory.ItemStack bukkitItemStack)
    {
        return wrapItemStack(CraftItemStack.asNMSCopy(bukkitItemStack));
    }

    @Override
    public NMSItemStack loadItemStack(NBTCompound compound)
    {
        return wrapItemStack(new ItemStack(((NMSNBTCompound) compound).getHandle()));
    }

}
