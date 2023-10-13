package io.github.monun.tap.plugin.test

import io.github.monun.tap.plugin.test.unit.complex.TestFakeEntityPose
import io.github.monun.tap.plugin.test.unit.simple.TestConfigSupport
import io.github.monun.tap.plugin.test.unit.simple.TestPersistentDataSupport
import io.github.monun.tap.plugin.test.unit.simple.TestNewVersionFetchSupport
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin

object TapTest {
    private lateinit var plugin: Plugin

    private val simpleTestMap: Map<String, () -> SimpleTestUnit> = mapOf(
        "persistent-data" to ::TestPersistentDataSupport,
        "config-test" to ::TestConfigSupport,
        "version-fetch-test" to ::TestNewVersionFetchSupport
    )

    private val complexTestMap: Map<String, () -> ComplexTestUnit> = mapOf(
        "fake-entity-pose" to ::TestFakeEntityPose
    )
    private val runningComplexTestList = mutableListOf<ComplexTestUnit>()

    fun init(plugin: Plugin) {
        this.plugin = plugin
    }

    fun testSimpleAll() {
        simpleTestMap.forEach { (name, factory) ->
            val test = factory()
            test.name = name
            test.logger = plugin.logger

            test.message(buildString {
                repeat(10) { append('=') }
                append(" $name ")
                repeat(10) { append('=') }
            })
            test.runCatching { test() }.onFailure { e ->
                e.printStackTrace()
                test.error("$name // TEST FAILED!")
            }
        }
    }

    fun runTestComplexTestAll() {
        runningComplexTestList += complexTestMap.map { (name, factory) ->
            val test = factory()
            test.name = name
            test.logger = plugin.logger
            test.register(plugin)
            test.task = Bukkit.getScheduler().runTaskTimer(plugin, test, 1L, 1L)
            Bukkit.getPluginManager().registerEvents(test, plugin)
            test
        }
    }

    fun cancelTestComplexTestAll() {
        runningComplexTestList.onEach {
            HandlerList.unregisterAll(it)
            it.task?.cancel()
            it.unregister()
        }.clear()
    }
}

