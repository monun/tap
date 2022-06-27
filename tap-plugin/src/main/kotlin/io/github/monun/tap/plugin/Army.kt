package io.github.monun.tap.plugin

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.fake.PlayerData
import org.bukkit.Location
import org.bukkit.entity.Player

class Army(fakeServer: FakeEntityServer, center: Location) {
    private val army = ArrayList<FakeEntity<Player>>()

    init {
        val data = PlayerData("mango", "dolphin2410")
        for (x in -1..1) {
            for (z in -2..2) {
                val player = fakeServer.spawnPlayer(center.clone().add(x.toDouble(), 0.0, z.toDouble()), data)
                army.add(player)
                data.refresh()
            }
        }
    }

    fun moveCenter(location: Location) {
            var index = 0
            for (x in -5..5) {
                for (z in -5..5) {
                    army[index++].moveTo(location.clone().add(x.toDouble(), 0.0, z.toDouble()))
                }
            }
    }
}