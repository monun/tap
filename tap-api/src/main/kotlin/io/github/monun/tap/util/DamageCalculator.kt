package io.github.monun.tap.util

import io.github.monun.tap.loader.LibraryLoader
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface DamageCalculator {
    companion object: DamageCalculator by LibraryLoader.loadNMS(DamageCalculator::class.java)

    /*
    * 공격할수 없을시 -1을 리턴함
    */
    fun getDamage(player: Player, target: Entity): Float
}


fun Player.getDamage(target: Entity) = DamageCalculator.getDamage(this, target)