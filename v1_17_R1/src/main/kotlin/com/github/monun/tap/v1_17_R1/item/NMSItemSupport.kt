/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.monun.tap.v1_17_R1.item

import com.github.monun.tap.item.ItemSupport
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.player.PlayerInventory
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.v1_17_R1.CraftEquipmentSlot
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryPlayer
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack as BukkitItemStack
import org.bukkit.inventory.PlayerInventory as BukkitPlayerInventory

class NMSItemSupport : ItemSupport {
    override fun saveToJsonString(item: BukkitItemStack): String {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        return nmsItem.save(NBTTagCompound()).toString()
    }

    override fun damageArmor(playerInventory: BukkitPlayerInventory, attackDamage: Double) {
        val nmsInventory = (playerInventory as CraftInventoryPlayer).inventory

        nmsInventory.a(DamageSource.d, attackDamage.toFloat(), PlayerInventory.f)
    }

    override fun damageSlot(playerInventory: BukkitPlayerInventory, slot: EquipmentSlot, damage: Int) {
        val nmsInventory = (playerInventory as CraftInventoryPlayer).inventory
        val nmsSlot = CraftEquipmentSlot.getNMS(slot)
        val nmsItem = nmsInventory.getItem(slot)

        if (!nmsItem.isEmpty) {
            nmsItem.damage(damage, (playerInventory.holder as CraftPlayer).handle) { player ->
                player.broadcastItemBreak(nmsSlot)
            }
        }
    }
}

internal fun PlayerInventory.getItem(slot: EquipmentSlot): ItemStack {
    return when (slot) {
        EquipmentSlot.HAND -> itemInHand
        EquipmentSlot.OFF_HAND -> j[0]
        EquipmentSlot.FEET -> armorContents[0]
        EquipmentSlot.LEGS -> armorContents[1]
        EquipmentSlot.CHEST -> armorContents[2]
        EquipmentSlot.HEAD -> armorContents[3]
    }
}