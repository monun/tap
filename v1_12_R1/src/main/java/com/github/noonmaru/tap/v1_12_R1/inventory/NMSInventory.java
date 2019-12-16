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

package com.github.noonmaru.tap.v1_12_R1.inventory;

import com.github.noonmaru.tap.inventory.TapInventory;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemStack;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemSupport;
import com.google.common.base.Objects;
import net.minecraft.server.v1_12_R1.IInventory;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.stream.Collectors;

public class NMSInventory implements TapInventory
{

    private final CraftInventory craftInventory;

    private final IInventory nmsInventory;

    NMSInventory(CraftInventory inventory)
    {
        craftInventory = inventory;
        nmsInventory = inventory.getInventory();
    }

    NMSInventory(IInventory nmsInventory, CraftInventory craftInventory)
    {
        this.nmsInventory = nmsInventory;
        this.craftInventory = craftInventory;
    }

    @Override
    public int getSize()
    {
        return nmsInventory.getSize();
    }

    @Override
    public int getFirstEmpty()
    {
        IInventory nmsInventory = this.nmsInventory;

        for (int i = 0, size = nmsInventory.getSize(); i < size; i++)
        {
            if (nmsInventory.getItem(i).isEmpty())
                return i;
        }

        return -1;
    }

    @Override
    public NMSItemStack getItem(int slot)
    {
        return NMSItemSupport.wrapItemStack(nmsInventory.getItem(slot));
    }

    @Override
    public List<NMSItemStack> getContents()
    {
        return nmsInventory.getContents().stream().map(NMSItemSupport::wrapItemStack).collect(Collectors.toList());
    }

    @Override
    public void setContents(List<? extends TapItemStack> contents)
    {
        IInventory inv = this.nmsInventory;

        for (int i = 0, size = Math.min(inv.getSize(), contents.size()); i < size; i++)
            inv.setItem(i, NMSItemSupport.unwrapItemStack(contents.get(i)));
    }

    @Override
    public int indexOf(TapItemStack tapItem)
    {
        ItemStack itemStack = ((NMSItemStack) tapItem).getHandle();
        Item item = itemStack.getItem();
        int amount = itemStack.getCount();
        int data = itemStack.getData();
        NBTTagCompound tag = itemStack.getTag();
        IInventory nmsInventory = this.nmsInventory;

        for (int i = 0, size = nmsInventory.getSize(); i < size; i++)
        {
            ItemStack current = nmsInventory.getItem(i);

            if (current == itemStack || (!current.isEmpty() && item == current.getItem() && amount == current.getCount() && data == current.getData() && Objects.equal(tag, current.getTag())))
                return i;
        }

        return -1;
    }

    @Override
    public boolean contains(TapItemStack tapItem)
    {
        ItemStack itemStack = ((NMSItemStack) tapItem).getHandle();
        Item item = itemStack.getItem();
        int remain = itemStack.getCount();
        int data = itemStack.getData();
        NBTTagCompound tag = itemStack.getTag();
        IInventory inv = this.nmsInventory;

        for (int i = 0, size = inv.getSize(); i < size; i++)
        {
            ItemStack current = inv.getItem(i);

            if (!current.isEmpty() && item == current.getItem() && data == current.getData() && Objects.equal(tag, current.getTag()))
            {
                remain -= Math.min(remain, current.getCount());

                if (remain == 0)
                    return true;
            }
        }

        return false;
    }

    @Override
    public void setItem(int slot, TapItemStack item)
    {
        this.nmsInventory.setItem(slot, NMSItemSupport.unwrapItemStack(item));
    }

    int getInventorySize()
    {
        return this.nmsInventory.getSize();
    }

    @Override
    public int canHold(TapItemStack tapItem)
    {
        ItemStack itemStack = ((NMSItemStack) tapItem).getHandle();
        int remain = itemStack.getCount();
        Item item = itemStack.getItem();
        int maxStackSize = item.getMaxStackSize();
        int data = itemStack.getData();
        NBTTagCompound tag = itemStack.getTag();

        for (int i = 0, size = getInventorySize(); i < size; i++)
        {
            ItemStack current = nmsInventory.getItem(i);

            if (current.isEmpty())
                return itemStack.getCount();

            if (item == current.getItem() && data == current.getData())
            {
                int amount = current.getCount();

                if (amount < maxStackSize && Objects.equal(tag, current.getTag()))
                {
                    remain -= Math.min(remain, maxStackSize - amount);

                    if (remain == 0)
                        return itemStack.getCount();
                }
            }
        }

        return itemStack.getCount() - remain;
    }

    @Override
    public int addItem(TapItemStack tapItem)
    {
        ItemStack itemStack = ((NMSItemStack) tapItem).getHandle();
        int remain = itemStack.getCount();
        Item item = itemStack.getItem();
        int maxStackSize = item.getMaxStackSize();
        int data = itemStack.getData();
        NBTTagCompound tag = itemStack.getTag();
        int firstEmpty = -1;

        for (int i = 0, size = getInventorySize(); i < size; i++)
        {
            ItemStack current = nmsInventory.getItem(i);

            if (current.isEmpty())
            {
                if (firstEmpty == -1)
                    firstEmpty = i;
            }
            else
            {
                if (item == current.getItem() && data == current.getData())
                {
                    int amount = current.getCount();

                    if (amount < maxStackSize && Objects.equal(tag, current.getTag()))
                    {
                        int pass = Math.min(remain, maxStackSize - amount);

                        current.add(pass);
                        remain -= pass;

                        if (remain == 0)
                            break;
                    }
                }
            }
        }

        if (remain > 0 && firstEmpty != -1)
        {
            ItemStack clone = itemStack.cloneItemStack();
            clone.setCount(remain);
            nmsInventory.setItem(firstEmpty, clone);
            return itemStack.getCount();
        }

        return itemStack.getCount() - remain;
    }

    @Override
    public int removeItem(TapItemStack tapItem)
    {
        ItemStack itemStack = ((NMSItemStack) tapItem).getHandle();
        int remain = itemStack.getCount();
        Item item = itemStack.getItem();
        int data = itemStack.getData();
        NBTTagCompound tag = itemStack.getTag();

        for (int i = 0, size = nmsInventory.getSize(); i < size; i++)
        {
            ItemStack current = nmsInventory.getItem(i);

            if (!current.isEmpty() && item == current.getItem() && data == current.getData() && Objects.equal(tag, current.getTag()))
            {
                int pass = Math.min(remain, current.getCount());
                current.subtract(pass);
                remain -= pass;

                if (current.getCount() == 0)
                    nmsInventory.setItem(i, ItemStack.a);

                if (remain == 0)
                    return itemStack.getCount();
            }
        }

        return itemStack.getCount() - remain;
    }

    @Override
    public void clear()
    {
        this.nmsInventory.clear();
    }

    @Override
    public Inventory getBukkitInventory()
    {
        return this.craftInventory;
    }

}
