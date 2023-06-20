/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.v1_20_1.item

import io.github.monun.tap.item.ItemSupport
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryPlayer
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack as BukkitItemStack
import org.bukkit.inventory.PlayerInventory as BukkitPlayerInventory

class NMSItemSupport : ItemSupport {
    override fun saveToJsonString(item: BukkitItemStack): String {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        return nmsItem.save(CompoundTag()).toString()
    }

    override fun damageArmor(playerInventory: BukkitPlayerInventory, attackDamage: Double) {
        val nmsInventory = (playerInventory as CraftInventoryPlayer).inventory
        
        nmsInventory.hurtArmor(
            nmsInventory.player.damageSources().lava(),
            attackDamage.toFloat(),
            Inventory.ALL_ARMOR_SLOTS
        )
    }

    override fun damageSlot(playerInventory: BukkitPlayerInventory, slot: EquipmentSlot, damage: Int) {
        val nmsInventory = (playerInventory as CraftInventoryPlayer).inventory
        val nmsSlot = CraftEquipmentSlot.getNMS(slot)
        val nmsItem = nmsInventory.getItem(slot)

        if (!nmsItem.isEmpty) {
            nmsItem.hurtAndBreak(damage, (playerInventory.holder as CraftPlayer).handle) { player ->
                player.broadcastBreakEvent(nmsSlot)
            }
        }
    }
}

internal fun Inventory.getItem(slot: EquipmentSlot): ItemStack {
    return when (slot) {
        EquipmentSlot.HAND -> getSelected()
        EquipmentSlot.OFF_HAND -> offhand[0]
        EquipmentSlot.FEET -> armorContents[0]
        EquipmentSlot.LEGS -> armorContents[1]
        EquipmentSlot.CHEST -> armorContents[2]
        EquipmentSlot.HEAD -> armorContents[3]
    }
}