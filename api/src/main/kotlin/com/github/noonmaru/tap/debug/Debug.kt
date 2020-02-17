package com.github.noonmaru.tap.debug

import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import com.github.noonmaru.tap.item.saveToJsonString
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

class CommandDebug : CommandComponent {
    override val argsCount: Int
        get() = 3

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        sender.sendMessage(args.joinToString(" "))

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        return listOf("Heptagram", "ehdgh141").tabComplete(args.last())
    }
}

class CommandDebugBookMeta : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        if (sender !is Player) return true

        val book = ItemStack(Material.WRITTEN_BOOK)
        val meta = book.itemMeta as BookMeta
        val component = TextComponent().apply {
            text = "Hello"
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/say hello")
        }

        val component2 = TextComponent().apply {
            text = "hover"
            hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                ComponentBuilder(ItemStack(Material.STICK).saveToJsonString()).create()
            )
        }

        meta.spigot().addPage(arrayOf(component), arrayOf(component2))
        meta.author = "TEST"
        meta.title = "TITLE"
        meta.generation = BookMeta.Generation.ORIGINAL
        book.itemMeta = meta
        sender.inventory.addItem(book)

        return true
    }
}