package io.github.monun.tap.plugin.test.unit.complex

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.fake.tap
import io.github.monun.tap.plugin.test.ComplexTestUnit
import io.github.monun.tap.protocol.AnimationType
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.random.Random.Default.nextInt

class TestFakeEntityPose : ComplexTestUnit() {

    private lateinit var fakeEntityServer: FakeEntityServer
    private val map = mutableMapOf<UUID, FakeEntity<Player>>()

    override fun register(plugin: Plugin) {
        fakeEntityServer = FakeEntityServer.create(plugin as JavaPlugin)
        Bukkit.getOnlinePlayers().forEach {
            fakeEntityServer.addPlayer(it)
            spawnBody(it)
        }
    }

    override fun unregister() {
        fakeEntityServer.clear()
    }

    override fun run() {
        if (nextInt(100) == 0) {
            map.values.forEach { entity ->
//                entity.playEffect(EntityEffect.HURT)
//                entity.playAnimation(AnimationType.TAKE_DAMAGE)
                entity.playHurtAnimation()
            }
        }

        fakeEntityServer.update()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.let {
            spawnBody(it)
            fakeEntityServer.addPlayer(it)
        }

    }

    @EventHandler
    fun onPlayerQuit(event: PlayerJoinEvent) {
        event.player.let {
            despawnBody(it)
            fakeEntityServer.removePlayer(it)
        }
    }

    private fun spawnBody(player: Player) {
        fakeEntityServer.spawnPlayer(player.location, "●▅▇█▇▆▅▄▇", player.playerProfile.properties).apply {
            updateMetadata {
                tap().pose = Pose.SLEEPING
            }
        }.also {
            map[player.uniqueId] = it
        }
    }

    private fun despawnBody(player: Player) {
        map.remove(player.uniqueId)?.remove()
    }
}
