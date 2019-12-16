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
import com.github.noonmaru.tap.nbt.NBTList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TapItemType
{

    private static final String ID = "id";

    private static final String DAMAGE = "Damage";

    private static final String TAG = "tag";

    private static TapItemType instance;

    private TapItem item;

    private int data;

    private NBTCompound tag;

    private int hash;

    TapItemType()
    {}

    public TapItemType(TapItem item)
    {
        this(item, 0);
    }

    public TapItemType(TapItem item, int data)
    {
        this(item, data, null);
    }

    public TapItemType(TapItem item, int data, NBTCompound tag)
    {
        this.item = item;
        this.data = item.getMaxDurability() == 0 ? data : 0;
        this.tag = tag;
    }

    public TapItemType(TapItemStack item)
    {
        apply(item);
    }

    public TapItemType(NBTCompound compound)
    {
        int id = compound.getInt(ID);
        item = Tap.ITEM.getItem(id);

        if (item == null)
            throw new NullPointerException("Unknown item id '" + id + "'");

        if (item.getMaxDurability() == 0)
            data = compound.getInt(DAMAGE);

        if (compound.contains(TAG))
            tag = compound.getCompound(TAG);
    }

    public static TapItemType instance()
    {
        if (instance == null)
            instance = new TapItemType();

        return instance;
    }

    public TapItemType copy()
    {
        TapItemType o = new TapItemType();
        o.item = this.item;
        o.data = this.data;

        if (this.tag != null)
            o.tag = this.tag.copy();

        o.hash = this.hash;

        return o;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj instanceof TapItemType)
        {
            TapItemType other = (TapItemType) obj;

            return this.item == other.item && this.data == other.data && Objects.equals(tag, other.tag);
        }

        return false;
    }

    public TapItemType apply(TapItemStack itemStack)
    {
        return apply(itemStack.getItem(), itemStack.getData(), itemStack.getTag());
    }

    public TapItemType apply(TapItem item, int data, NBTCompound tag)
    {
        this.item = item;
        this.data = item.getMaxDurability() > 0 ? 0 : data;
        this.tag = tag;
        hash = 0;

        return this;
    }

    public int getData()
    {
        return data;
    }

    public TapItem getItem()
    {
        return item;
    }

    public String getName()
    {
        String displayName = getDisplayName();

        return displayName != null ? displayName : item.getName();
    }

    public String getDisplayName()
    {
        NBTCompound tag = this.tag;

        if (tag != null)
        {
            NBTCompound display = tag.getCompound("display");

            return display == null ? null : display.getString("Name");
        }

        return null;
    }

    public List<String> getLore()
    {
        NBTCompound tag = this.tag;

        if (tag != null)
        {
            NBTCompound display = tag.getCompound("display");

            if (display != null)
            {
                NBTList lore = display.getList("Lore");

                if (lore != null)
                {
                    int size = lore.size();
                    ArrayList<String> list = new ArrayList<>(lore.size());

                    for (int i = 0; i < size; i++)
                    {
                        list.add(lore.getString(i));
                    }

                    return list;
                }
            }
        }

        return null;
    }

    public NBTCompound getTag()
    {
        return tag.copy();
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            int hash = this.item.getId() << 16 | this.data;

            if (this.tag != null)
            {
                hash ^= this.tag.hashCode();
            }

            this.hash = hash;
        }

        return this.hash;
    }

    public TapItemStack toItemStack()
    {
        return toItemStack(1);
    }

    public TapItemStack toItemStack(int amount)
    {
        TapItemStack itemStack = Tap.ITEM.newItemStack(item, amount, data);

        if (tag != null)
            itemStack.setTag(tag.copy());

        return itemStack;
    }

    public NBTCompound save()
    {
        return save(Tap.NBT.newCompound());
    }

    public NBTCompound save(NBTCompound compound)
    {
        compound.setInt(ID, item.getId());

        if (item.getMaxDurability() == 0)
            compound.setInt(DAMAGE, data);

        if (this.tag != null)
            compound.setCompound(TAG, tag.copy());

        return compound;
    }
}
