package io.github.monun.tap.plugin.test

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

abstract class TestUnit {
    lateinit var name: String
    lateinit var logger: Logger

    fun message(message: String) {
        Bukkit.broadcast(Component.text(message).color(NamedTextColor.GRAY))
    }

    fun error(message: String) {
        Bukkit.broadcast(Component.text(message).color(NamedTextColor.RED))
    }
}

abstract class SimpleTestUnit : TestUnit() {
    abstract fun test()
}

abstract class ComplexTestUnit : TestUnit(), Listener, Runnable {
    abstract fun register(plugin: Plugin)

    abstract fun unregister()
}