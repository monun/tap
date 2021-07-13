/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.item

import io.github.monun.tap.loader.LibraryLoader
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

interface ItemSupport {
    companion object: ItemSupport by  LibraryLoader.loadNMS(ItemSupport::class.java)

    fun saveToJsonString(item: ItemStack): String {
        error("Unsupported nms version")
    }

    fun damageArmor(playerInventory: PlayerInventory, attackDamage: Double) {
        error("Unsupported nms version")
    }

    fun damageSlot(playerInventory: PlayerInventory, slot: EquipmentSlot, damage: Int) {
        error("Unsupported nms version")
    }
}

fun ItemStack.saveToJsonString(): String {
    return ItemSupport.saveToJsonString(this)
}

fun PlayerInventory.damageArmor(attackDamage: Double) {
    ItemSupport.damageArmor(this, attackDamage)
}

fun PlayerInventory.damageSlot(slot: EquipmentSlot, damage: Int = 1) {
    ItemSupport.damageSlot(this, slot, damage)
}