package io.github.monun.tap.plugin.test.unit.simple

import io.github.monun.tap.config.Config
import io.github.monun.tap.config.ConfigSupport
import io.github.monun.tap.config.compute
import io.github.monun.tap.plugin.test.SimpleTestUnit
import org.bukkit.configuration.file.YamlConfiguration

class TestConfigSupport : SimpleTestUnit() {
    override fun test() {
        val separated = YamlConfiguration()
        val nonSeparated = YamlConfiguration()
        val config = TestParrotConfig()

        ConfigSupport.compute(config, separated, true)
        ConfigSupport.compute(config, nonSeparated, false)

        message("클래스 분리\n${separated.saveToString()}")
        message("클래스 미분리\n${nonSeparated.saveToString()}")

        message("데이터 변경")
        message("name: 구구")
        message("age: 30")
        message("color: blue")

        nonSeparated["name"] = "구구"
        nonSeparated["age"] = 30
        nonSeparated["color"] = "blue"

        ConfigSupport.compute(config, nonSeparated, false)
        message("${config.name} ${config.age} ${config.color}")
    }
}

open class TestBirdConfig {
    @Config
    var name: String = "bird"

    @Config
    var age: Int = 15
}

class TestParrotConfig : TestBirdConfig() {
    @Config
    var color: String = "red"
}