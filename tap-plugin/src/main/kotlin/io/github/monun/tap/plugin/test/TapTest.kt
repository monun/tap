package io.github.monun.tap.plugin.test

import io.github.monun.tap.plugin.test.unit.simple.TestConfigSupport
import io.github.monun.tap.plugin.test.unit.simple.TestPersistentDataSupport
import org.bukkit.plugin.Plugin

object TapTest {
    private lateinit var plugin: Plugin

    private val simpleTestMap: Map<String, () -> SimpleTestUnit> = mapOf(
        "persistent-data" to ::TestPersistentDataSupport,
        "config-test" to ::TestConfigSupport
    )

    private val complexTestList: List<ComplexTestUnit> = listOf(

    )

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

}

