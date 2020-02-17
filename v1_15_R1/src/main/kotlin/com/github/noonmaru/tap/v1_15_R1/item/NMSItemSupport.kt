package com.github.noonmaru.tap.v1_15_R1.item

import com.github.noonmaru.tap.item.ItemSupport
import net.minecraft.server.v1_15_R1.NBTTagCompound
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

class NMSItemSupport : ItemSupport() {
    override fun saveToJsonString(item: ItemStack): String {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        return nmsItem.save(NBTTagCompound()).toString()
    }
}