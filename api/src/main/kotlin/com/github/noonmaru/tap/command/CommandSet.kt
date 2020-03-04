/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.command

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSortedMap
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.math.max
import kotlin.math.min


class CommandBuilder internal constructor(init: CommandBuilder.() -> Unit) {

    internal val inits = HashMap<String, CommandContainer.() -> CommandComponent>()

    internal var help: Pair<String, (CommandContainer.() -> Unit)?>? = null

    init {
        init()
    }

    private fun checkLabel(label: String) {
        Preconditions.checkArgument(
            label !in inits && help?.let { it.first != label } ?: true,
            "Already registered label $label")
    }

    fun component(label: String, init: CommandContainer.() -> CommandComponent) {
        checkLabel(label)

        inits[label] = init
    }

    fun help(label: String, init: (CommandContainer.() -> Unit)? = null) {
        checkLabel(label)

        help = Pair(label, init)
    }

    fun build(): CommandSet {
        return CommandSet(this)
    }
}

fun JavaPlugin.command(label: String, init: CommandBuilder.() -> Unit): CommandSet? {
    return getCommand(label)?.let { command ->
        CommandBuilder(init).build().apply {
            command.setExecutor(this)
            command.tabCompleter = this
        }
    }
}

class CommandSet internal constructor(builder: CommandBuilder) : TabExecutor {

    private val containers: Map<String, CommandContainer>

    init {
        val components = TreeMap<String, CommandContainer>(String.CASE_INSENSITIVE_ORDER)
        for ((label, init) in builder.inits) {
            components[label] = CommandContainer(label, init)
        }
        builder.help?.let { (label, init) ->
            components[label] = CommandContainer(label) {
                usage = "<Page | Command>"
                description = "명령 사용 방법을 확인합니다."
                init?.let { it() }
                CommandHelp()
            }
        }
        this.containers = ImmutableSortedMap.copyOfSorted(components)
    }

    private inner class CommandHelp : CommandComponent {
        override fun onCommand(
            sender: CommandSender,
            label: String,
            componentLabel: String,
            args: ArgumentList
        ): Boolean {

            val next = if (args.hasNext()) args.next() else null

            try {
                val page = next?.run {
                    max(next.toInt() - 1, 0)
                } ?: 0

                val info = pageInformation(
                    name = "Help",
                    list = sender.getExecutablesByPermission(),
                    describer = { _, o -> createHelp(label, o.label, o.usage, o.description) },
                    _page = page,
                    length = 9
                )
                info.forEach(sender::sendMessage)
            } catch (e: NumberFormatException) {
                val container = containers[next!!] ?: return false
                sender.sendMessage(container.let { createHelp(label, it.label, it.usage, it.description) })
            }

            return true
        }
    }

    private fun CommandSender.getExecutablesByPermission(): List<CommandContainer> {
        return containers.values.filter {
            it.component.test(this) == null
                    && it.permission?.let { perm -> perm.isBlank() || hasPermission(perm) } ?: true
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val executables = sender.getExecutablesByPermission()

        if (executables.isEmpty()) {
            sender.sendMessage("/$label 실행 가능한 명령이 없습니다. 명령을 등록해주세요.")
            return true
        }

        //명령어 목록 출력
        if (args.isEmpty()) {
            val labels = executables.joinToString(separator = " ", transform = { it.label })

            sender.sendMessage("/$label $labels")
            return true
        }

        val componentLabel = args[0]
        containers[componentLabel]?.run {
            component.test(sender)?.let {
                sender.sendMessage(it())
                return true
            }
            permission?.let { permission ->
                sender.sendMessage(permissionMessage?.replace("<permission>", permission) ?: "권한이 없습니다.")
                return true
            }

            component.let {
                if (args.count() - 1 >= it.argsCount && it.onCommand(
                        sender,
                        label,
                        componentLabel,
                        ArgumentList(args, 1)
                    )
                ) {
                    return true
                }

                sender.sendMessage(createHelp(label, this.label, this.usage, this.description))
                return true
            }
        }

        sender.sendMessage("/$label $componentLabel 등록되지 않은 명령입니다.")
        executables.getNearestCommand(componentLabel)?.let {
            sender.sendMessage("${ChatColor.GRAY}  혹시 이 명령을 찾으셨나요? -> /$label ${it.label}")

            return true
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        val componentLabel = args[0]

        if (args.count() == 1) return containers.keys.tabComplete(componentLabel)

        containers[componentLabel]?.run {
            return component.onTabComplete(sender, label, componentLabel, ArgumentList(args, 1))
        }

        return emptyList()
    }
}

private fun Iterable<CommandContainer>.getNearestCommand(label: String): CommandContainer? =
    minBy { calcLevenshteinDistance(label, it.label) }

fun createHelp(
    label: String,
    componentLabel: String,
    usage: String?,
    description: String?
): String {
    val builder = StringBuilder()
    builder.append('/').append(label).append(' ').append(componentLabel)

    usage?.let { builder.append(' ').append(it) }
    description?.let { builder.append(' ').append(it) }

    return builder.toString()
}

private fun calcLevenshteinDistance(a: String, b: String): Int {
    val len0 = a.length + 1
    val len1 = b.length + 1
    var cost = IntArray(len0)
    var newCost = IntArray(len0)
    for (i in 0 until len0) cost[i] = i
    for (j in 1 until len1) {
        newCost[0] = j
        for (i in 1 until len0) {
            val match = if (a[i - 1] == b[j - 1]) 0 else 1
            val costReplace = cost[i - 1] + match
            val costInsert = cost[i] + 1
            val costDelete = newCost[i - 1] + 1
            newCost[i] = min(min(costInsert, costDelete), costReplace)
        }
        val swap = cost
        cost = newCost
        newCost = swap
    }
    return cost[len0 - 1]
}