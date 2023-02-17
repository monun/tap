package io.github.monun.tap.plugin.test.unit.simple

import io.github.monun.tap.data.PersistentDataKeychain
import io.github.monun.tap.data.getValue
import io.github.monun.tap.data.persistentData
import io.github.monun.tap.plugin.test.SimpleTestUnit
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.time.Instant

class TestPersistentDataSupport : SimpleTestUnit() {

    companion object {
        private val item = ItemStack(Material.STONE)
    }

    // level = 테스트 마다 +1
    // power = 테스트 마다 +0.15
    // timestamp = 테스트 마다 현재 시간
    // complex = 테스트 마다 numbers에 count 추가, float에 1.1 곱함
    // message = 테스트 마다 문자 하나씩 추가
    override fun test() {
        item.apply {
            val prevData = itemMeta!!.persistentData
            val prevLevel = prevData[TestKeychain.level] ?: 0
            val prevPower = prevData[TestKeychain.power] ?: 0.0
            val prevComplex = prevData[TestKeychain.complex] ?: TestComplexData(emptyList(), 1.0f)
            val prevMessage = prevData["message"] ?: ""

            //add data
            editMeta { meta ->
                meta.persistentData.let { data ->

                    data[TestKeychain.level] = prevLevel + 1
                    data[TestKeychain.power] = prevPower + 0.15
                    data[TestKeychain.timestamp] = Instant.now().toString()
                    data[TestKeychain.complex] =
                        TestComplexData(
                            prevComplex.numbers + listOf(prevComplex.numbers.count()),
                            prevComplex.float * 1.1f
                        )
                    data["message"] = prevMessage + (prevMessage.lastOrNull()?.inc() ?: "A")
                }
            }

            itemMeta!!.persistentData.let { data ->
                message("level: ${data[TestKeychain.level]}")
                message("power: ${data[TestKeychain.power]}")
                message("timestamp: ${data[TestKeychain.timestamp]}")
                message("complex: ${data[TestKeychain.complex]}")
                val message: String? by data
                message("message: $message")

                require(data[TestKeychain.level] == prevLevel + 1) { "level" }
                require(data[TestKeychain.power] == prevPower + 0.15) { "power" }
            }
        }
    }
}

object TestKeychain : PersistentDataKeychain() {
    val level = primitive<Int>("level")
    val power = primitive<Double>("power")
    val timestamp = primitive<String>("timestamp")
    val complex = complex<TestComplexData>("complex")
}

@Serializable
data class TestComplexData(
    val numbers: List<Int>,
    val float: Float
)