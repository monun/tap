package io.github.monun.tap.v1_19_3.util

import io.github.monun.tap.util.DamageCalculator
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.enchantment.EnchantmentHelper
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class NMSDamageCalculator: DamageCalculator {
    override fun getDamage(player: Player, target: Entity): Float {

        player as CraftPlayer
        target as CraftLivingEntity

        val playerHandle = player.handle
        val targetHandle = target.handle

        var attackDamage = playerHandle.getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()

        var damageBonus = EnchantmentHelper.getDamageBonus(playerHandle.mainHandItem, targetHandle.mobType)

        val attackStrengthScale = playerHandle.getAttackStrengthScale(0.5f)


        attackDamage *= 0.2f + attackStrengthScale * attackStrengthScale * 0.8f
        damageBonus *= attackStrengthScale
        if (attackDamage > 0.0f || damageBonus > 0.0f) {
            val isCharged = attackStrengthScale > 0.9f
            var knockBackBonus = EnchantmentHelper.getKnockbackBonus(playerHandle)
            if (playerHandle.isSprinting && isCharged) {
                ++knockBackBonus
            }
            var isCrit = isCharged &&
                        playerHandle.fallDistance > 0.0f &&
                        !playerHandle.onGround &&
                        !playerHandle.onClimbable() &&
                        !playerHandle.isInWater &&
                        !playerHandle.hasEffect(MobEffects.BLINDNESS) &&
                        !playerHandle.isPassenger

            isCrit = isCrit && !playerHandle.level.paperConfig().entities.behavior.disablePlayerCrits
            isCrit = isCrit && !playerHandle.isSprinting
            if (isCrit) {
                attackDamage *= 1.5f
            }
            attackDamage += damageBonus


            val canHurt: Boolean = !targetHandle.isInvulnerableTo(
                DamageSource.playerAttack(playerHandle).critical(isCrit)
            )

            if (!canHurt) attackDamage = -1f

        }
        return attackDamage
    }
}