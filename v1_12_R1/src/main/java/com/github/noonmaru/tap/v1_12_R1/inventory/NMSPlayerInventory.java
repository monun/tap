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

import com.github.noonmaru.tap.inventory.Slot;
import com.github.noonmaru.tap.inventory.TapPlayerInventory;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.v1_12_R1.entity.NMSPlayer;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemStack;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemSupport;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryPlayer;
import org.bukkit.entity.HumanEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class NMSPlayerInventory extends NMSInventory implements TapPlayerInventory
{

    private static final int OFFSET_HELMET = -2;

    private static final int OFFSET_CHESTPLATE = -3;

    private static final int OFFSET_LEGGINGS = -4;

    private static final int OFFSET_BOOTS = -5;

    public final NMSPlayer holder;

    public final PlayerInventory inv;

    public final CraftInventoryPlayer bukkitInv;

    public NMSPlayerInventory(CraftInventoryPlayer inv, NMSPlayer holder)
    {
        super(inv);

        this.holder = holder;
        this.inv = inv.getInventory();
        this.bukkitInv = inv;
    }

    @Override
    public List<NMSItemStack> getInventoryContents()
    {
        return inv.items.stream().map(NMSItemSupport::wrapItemStack).collect(Collectors.toList());
    }

    @Override
    public NMSPlayer getHolder()
    {
        return holder;
    }

    @Override
    public int getFirstEmpty()
    {
        return inv.getFirstEmptySlotIndex();
    }

    @Override
    public NMSItemStack getItem(int slot)
    {
        return NMSItemSupport.wrapItemStack(inv.getItem(slot));
    }

    @Override
    int getInventorySize()
    {
        return inv.items.size();
    }

    @Override
    public void update()
    {
        EntityPlayer player = (EntityPlayer) inv.player;
        player.updateInventory(player.activeContainer);
    }

    @Override
    public void update(int slot)
    {
        List<HumanEntity> viewers = inv.getViewers();

        if (viewers.isEmpty())
            return;

        ItemStack itemStack = inv.getItem(slot);
        slot = Slot.getRawSlot(slot);

        ((EntityPlayer) inv.player).playerConnection.sendPacket(new PacketPlayOutSetSlot(0, slot, itemStack));
    }

    @Override
    public void updateCursor()
    {
        ((EntityPlayer) inv.player).playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, inv.getCarried()));
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getBukkitInventory()
    {
        return this.bukkitInv;
    }

    @Override
    public NMSItemStack getHelmet()
    {
        return getItem(getSize() + OFFSET_HELMET);
    }

    @Override
    public void setHelmet(TapItemStack helmet)
    {
        setItem(getSize() + OFFSET_HELMET, helmet);
    }

    @Override
    public NMSItemStack getChestplate()
    {
        return getItem(getSize() + OFFSET_CHESTPLATE);
    }

    @Override
    public void setChestplate(TapItemStack chestplate)
    {
        setItem(getSize() + OFFSET_CHESTPLATE, chestplate);
    }

    @Override
    public NMSItemStack getLeggings()
    {
        return getItem(getSize() + OFFSET_LEGGINGS);
    }

    @Override
    public NMSItemStack getBoots()
    {
        return getItem(getSize() + OFFSET_BOOTS);
    }

    @Override
    public void setBoots(TapItemStack boots)
    {
        setItem(getSize() + OFFSET_BOOTS, boots);
    }

    @Override
    public List<NMSItemStack> getArmorContents()
    {
        return inv.armor.stream().map(NMSItemSupport::wrapItemStack).collect(Collectors.toList());
    }

    @Override
    public void setArmorContents(List<? extends TapItemStack> contents)
    {
        List<ItemStack> armor = inv.armor;

        if (contents == null || contents.isEmpty())
            Collections.fill(armor, ItemStack.a);
        else
            for (int i = 0, length = Math.min(armor.size(), contents.size()); i < length; i++)
                armor.set(i, NMSItemSupport.unwrapItemStack(contents.get(i)));
    }

    @Override
    public NMSItemStack getHeldItemMainHand()
    {
        return NMSItemSupport.wrapItemStack(inv.getItemInHand());
    }

    @Override
    public void setHeldItemMainHand(TapItemStack item)
    {
        setItem(getHeldItemSlot(), item);
    }

    @Override
    public NMSItemStack getHeldItemOffHand()
    {
        return NMSItemSupport.wrapItemStack(inv.extraSlots.get(0));
    }

    @Override
    public void setHeldItemOffHand(TapItemStack item)
    {
        inv.extraSlots.set(0, NMSItemSupport.unwrapItemStack(item));
    }

    @Override
    public NMSItemStack getCursor()
    {
        return NMSItemSupport.wrapItemStack(inv.getCarried());
    }

    @Override
    public void setCursor(TapItemStack cursor)
    {
        inv.setCarried(NMSItemSupport.unwrapItemStack(cursor));
    }

    @Override
    public int getHeldItemSlot()
    {
        int index = inv.itemInHandIndex;

        if (index < 0)
            index = 0;
        else if (index >= 9)
            index = 8;

        return index;
    }

    @Override
    public void setHeldItemSlot(int slot)
    {
        if (slot < 0 || slot >= PlayerInventory.getHotbarSize())
            throw new IllegalArgumentException("Slot is not between 0 and 8 inclusive");

        inv.itemInHandIndex = slot;
        holder.getHandle().playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(slot));
    }

    @Override
    public void setLeggins(TapItemStack leggings)
    {
        setItem(getSize() + OFFSET_LEGGINGS, leggings);
    }

}
