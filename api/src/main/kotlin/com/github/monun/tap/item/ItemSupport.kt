/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.item

import com.github.monun.tap.loader.LibraryLoader
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

interface ItemSupport {
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

private val NMS = LibraryLoader.load(ItemSupport::class.java)

fun ItemStack.saveToJsonString(): String {
    return NMS.saveToJsonString(this)
}

fun PlayerInventory.damageArmor(attackDamage: Double) {
    NMS.damageArmor(this, attackDamage)
}

fun PlayerInventory.damageSlot(slot: EquipmentSlot, damage: Int = 1) {
    NMS.damageSlot(this, slot, damage)
}