package com.github.noonmaru.tap.item

import com.github.noonmaru.tap.loader.LibraryLoader
import org.bukkit.inventory.ItemStack

abstract class ItemSupport {
    abstract fun saveToJsonString(item: ItemStack): String
}

private val NMS = LibraryLoader.load(ItemSupport::class.java)

fun ItemStack.saveToJsonString(): String {
    return NMS.saveToJsonString(this)
}