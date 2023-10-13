package io.github.monun.tap.fake

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.EquipmentSlot

class PlayerInteractFakeEntityEvent(
    player: Player,
    val fakeEntity: FakeEntity<out Entity>,
    val isAttack: Boolean,
    val hand: EquipmentSlot,
): PlayerEvent(player) {

    val server: FakeEntityServer get() = fakeEntity.server

    companion object {
        private val handlerList = HandlerList()
        @JvmStatic
        fun getHandlerList() = handlerList
    }
    override fun getHandlers() = handlerList
}