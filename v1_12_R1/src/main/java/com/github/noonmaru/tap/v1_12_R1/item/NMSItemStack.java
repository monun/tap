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
import com.github.noonmaru.tap.nbt.NBTCompound;
import com.github.noonmaru.tap.v1_12_R1.nbt.NMSNBTCompound;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public final class NMSItemStack implements TapItemStack
{

    public static final NMSItemStack EMPTY = new NMSItemStack(ItemStack.a);

    private static final String TAG_DISPLAY = "display";

    private static final String TAG_NAME = "Name";

    private static final String TAG_LORE = "Lore";

    private static final String TAG_UNBREAKABLE = "Unbreakable";

    private static final String TAG_HIDE_FLAGS = "HideFlags";

    private final ItemStack itemStack;

    NMSItemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    private static boolean isSimilar(ItemStack a, ItemStack b)
    {
        if (a == b)
            return true;

        Item item = a.getItem();

        return item == b.getItem() && (item.getMaxDurability() > 0 || a.getData() == b.getData()) && Objects.equals(a.getTag(), b.getTag());
    }

    @Override
    public NMSItem getItem()
    {
        return NMSItemSupport.getInstance().wrapItem(itemStack.getItem());
    }

    @Override
    public NMSItemStack setItem(TapItem item)
    {
        itemStack.setItem(((NMSItem) item).getHandle());

        return this;
    }

    @Override
    public NMSItemStack setItem(int itemId)
    {
        itemStack.setItem(Item.getById(itemId));

        return this;
    }

    @Override
    public int getId()
    {
        return Item.getId(itemStack.getItem());
    }

    @Override
    public int getAmount()
    {
        return itemStack.getCount();
    }

    @Override
    public NMSItemStack setAmount(int amount)
    {
        int maxStackSize = itemStack.getMaxStackSize();

        if (amount > maxStackSize)
            amount = maxStackSize;
        else if (amount < 0)
            amount = 0;

        itemStack.setCount(amount);

        return this;
    }

    @Override
    public boolean isEmpty()
    {
        return itemStack.isEmpty();
    }

    @Override
    public int getData()
    {
        return itemStack.getData();
    }

    @Override
    public NMSItemStack setData(int data)
    {
        itemStack.setData(data);

        return this;
    }

    @Override
    public String getName()
    {
        String name = getDisplayName();

        if (name != null)
            return name;

        return getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return itemStack.a();
    }

    @SuppressWarnings("unchecked")
    private <T extends NBTBase> T getDisplayTag(String name)
    {
        NBTTagCompound tag = itemStack.getTag();

        if (tag != null)
        {
            NBTTagCompound display = (NBTTagCompound) tag.get("display");

            if (display != null)
                return (T) display.get(name);
        }

        return null;
    }

    @Override
    public String getDisplayName()
    {
        NBTTagString name = getDisplayTag(TAG_NAME);

        return name == null ? null : name.c_();
    }

    @Override
    public NMSItemStack setDisplayName(String name)
    {
        if (name == null)
            removeDisplayTag(TAG_NAME);
        else
            setDisplayTag(TAG_NAME, new NBTTagString(name));

        return this;
    }

    private NBTTagList getLoreTag()
    {
        return getDisplayTag(TAG_LORE);
    }

    @Override
    public List<String> getLore()
    {
        NBTTagList lore = getLoreTag();

        if (lore != null)
            return new LoreList(lore);

        return null;
    }

    @Override
    public NMSItemStack setLore(List<String> lore)
    {
        if (lore == null)
            removeDisplayTag(TAG_LORE);
        else
        {
            NBTTagList list = new NBTTagList();

            for (String s : lore)
                list.add(new NBTTagString(s));

            setDisplayTag(TAG_LORE, list);
        }

        return this;
    }

    @Override
    public boolean hasLore(Predicate<String> p)
    {
        return findLore(p) != null;
    }

    @Override
    public String findLore(Predicate<String> p)
    {
        NBTTagList lore = getLoreTag();

        if (lore != null)
        {
            for (int i = 0, size = lore.size(); i < size; i++)
            {
                String s = lore.getString(i);

                if (p.test(s))
                    return s;
            }
        }

        return null;
    }

    @Override
    public String findLoreLast(Predicate<String> p)
    {
        NBTTagList lore = getLoreTag();

        if (lore != null)
        {
            for (int i = lore.size() - 1; i >= 0; i++)
            {
                String s = lore.getString(i);

                if (p.test(s))
                    return s;
            }
        }

        return null;
    }

    @Override
    public boolean hasTag()
    {
        NBTTagCompound tag = itemStack.getTag();

        return tag != null && !tag.isEmpty();
    }

    @Override
    public NBTCompound getTag()
    {
        NBTTagCompound tag = itemStack.getTag();

        return tag == null ? null : new NMSNBTCompound(tag);
    }

    @Override
    public NMSItemStack setTag(NBTCompound tag)
    {
        itemStack.setTag(tag == null ? null : ((NMSNBTCompound) tag).getHandle());

        return this;
    }

    @Override
    public boolean hasItemMeta()
    {
        return hasTag();
    }

    @Override
    public ItemMeta getItemMeta()
    {
        return CraftItemStack.getItemMeta(itemStack);
    }

    @Override
    public NMSItemStack setItemMeta(ItemMeta meta)
    {
        CraftItemStack.setItemMeta(itemStack, meta);

        return this;
    }

    @Override
    public CraftItemStack toItemStack()
    {
        return CraftItemStack.asCraftMirror(this.itemStack);
    }

    private void removeDisplayTag(String name)
    {
        NBTTagCompound tag = itemStack.getTag();

        if (tag != null)
        {
            NBTTagCompound display = (NBTTagCompound) tag.get(TAG_DISPLAY);

            if (display != null)
            {
                display.remove(name);

                if (display.isEmpty())
                {
                    tag.remove(TAG_DISPLAY);

                    if (tag.isEmpty())
                        itemStack.setTag(null);
                }
            }
        }
    }

    private void setDisplayTag(String name, NBTBase value)
    {
        ItemStack itemStack = this.itemStack;
        NBTTagCompound tag = itemStack.getTag();

        if (tag == null)
        {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

        NBTTagCompound display = (NBTTagCompound) tag.get(TAG_DISPLAY);

        if (display == null)
        {
            display = new NBTTagCompound();
            tag.set(TAG_DISPLAY, display);
        }

        display.set(name, value);
    }

    @Override
    public NMSItemStack addLore(List<String> lore)
    {
        if (lore.size() > 0)
        {
            ItemStack itemStack = this.itemStack;
            NBTTagCompound tag = itemStack.getTag();

            if (tag == null)
            {
                tag = new NBTTagCompound();
                itemStack.setTag(tag);
            }

            NBTTagCompound display = (NBTTagCompound) tag.get(TAG_DISPLAY);

            if (display == null)
            {
                display = new NBTTagCompound();
                tag.set(TAG_DISPLAY, display);
            }

            NBTTagList list = (NBTTagList) display.get(TAG_LORE);

            if (list == null)
            {
                list = new NBTTagList();
                display.set(TAG_LORE, list);
            }

            for (String s : lore)
                list.add(new NBTTagString(s));
        }

        return this;
    }

    @Override
    public NMSItemStack setUnbreakable(boolean flag)
    {
        ItemStack itemStack = this.itemStack;

        if (flag)
        {
            NBTTagCompound tag = itemStack.getTag();

            if (tag == null)
            {
                tag = new NBTTagCompound();
                this.itemStack.setTag(tag);
            }

            tag.setBoolean(TAG_UNBREAKABLE, true);
        }
        else
        {
            NBTTagCompound tag = this.itemStack.getTag();

            if (tag != null)
            {
                tag.remove(TAG_UNBREAKABLE);

                if (tag.isEmpty())
                    itemStack.setTag(null);
            }
        }

        return this;
    }

    @Override
    public NMSItemStack setHideFlags(int hideFlags)
    {
        ItemStack itemStack = this.itemStack;

        if (hideFlags > 0)
        {
            if (hideFlags > 63)
                hideFlags = 63;

            NBTTagCompound tag = itemStack.getTag();

            if (tag == null)
            {
                tag = new NBTTagCompound();
                this.itemStack.setTag(tag);
            }

            tag.setInt(TAG_HIDE_FLAGS, hideFlags);
        }
        else
        {
            NBTTagCompound tag = this.itemStack.getTag();

            if (tag != null)
            {
                tag.remove(TAG_HIDE_FLAGS);

                if (tag.isEmpty())
                    itemStack.setTag(null);
            }
        }

        return this;
    }

    @Override
    public CraftItem spawn(org.bukkit.World world, double x, double y, double z)
    {
        World w = ((CraftWorld) world).getHandle();

        EntityItem entity = new EntityItem(w, x, y, z, itemStack.cloneItemStack());
        entity.pickupDelay = 10;
        w.addEntity(entity);

        return (CraftItem) entity.getBukkitEntity();
    }

    @Override
    public CraftItem spawnNaturally(org.bukkit.World world, double x, double y, double z)
    {
        World w = ((CraftWorld) world).getHandle();
        Random r = w.random;

        EntityItem entity = new EntityItem(w, x + r.nextFloat() * 0.7F + 0.1500000059604645D, y + r.nextFloat() * 0.7F + 0.1500000059604645D,
                z + r.nextFloat() * 0.7F + 0.1500000059604645D, this.itemStack
        );
        entity.pickupDelay = 10;
        w.addEntity(entity);

        return (CraftItem) entity.getBukkitEntity();
    }

    @Override
    public boolean isSimilar(TapItemStack other)
    {
        return isSimilar(itemStack, ((NMSItemStack) other).itemStack);
    }

    @Override
    public NMSItemStack copy()
    {
        return new NMSItemStack(itemStack.cloneItemStack());
    }

    @Override
    public NBTCompound save(NBTCompound compound)
    {
        itemStack.save(((NMSNBTCompound) compound).getHandle());

        return compound;
    }

    public ItemStack getHandle()
    {
        return itemStack;
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = hash * 31 + getId();
        hash = hash * 31 + getAmount();
        hash = hash * 31 + getData();

        NBTTagCompound tag = this.itemStack.getTag();

        if (tag != null)
            hash = hash * 31 + tag.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof NMSItemStack)
        {
            ItemStack a = itemStack;
            ItemStack b = ((NMSItemStack) obj).itemStack;

            return isSimilar(a, b) && a.getCount() == b.getCount();
        }

        return false;
    }

    private static class LoreList extends AbstractList<String> implements RandomAccess
    {

        private final NBTTagList lore;

        LoreList(NBTTagList lore)
        {
            this.lore = lore;
        }

        @Override
        public String get(int index)
        {
            return this.lore.getString(index);
        }

        @Override
        public int size()
        {
            return this.lore.size();
        }

    }

}
