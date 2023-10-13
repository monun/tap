package io.github.monun.tap.plugin.test.unit.simple

import io.github.monun.tap.plugin.test.SimpleTestUnit
import org.bukkit.Bukkit

class TestNewVersionFetchSupport : SimpleTestUnit() {
    override fun test() {
        val oldMinecraftVersionMethod = with("(?<=\\(MC: )[\\d.]+?(?=\\))".toPattern().matcher(Bukkit.getVersion())) {
            when {
                find() -> group()
                else -> throw NoSuchElementException("No such minecraft version exists")
            }
        }

        val oldBukkitVersionMethod = with("v\\d+_\\d+_R\\d+".toPattern().matcher(Bukkit.getServer()::class.java.`package`.name)) {
            when {
                find() -> group()
                else -> throw NoSuchElementException("No such bukkit version exists")
            }
        }


        message("fetched minecraft version (old): $oldMinecraftVersionMethod")
        message("fetched minecraft version (new): ${Bukkit.getServer().minecraftVersion}")

        message("fetched bukkit version (old): $oldBukkitVersionMethod")
        message("fetched bukkit version (new): ${Bukkit.getServer().bukkitVersion}")
    }
}