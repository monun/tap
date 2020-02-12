package com.github.noonmaru.tap.debug

import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.command.CommandSender

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
        return listOf("Heptagram", "ehdgh141").tabComplete(args.next())
    }
}