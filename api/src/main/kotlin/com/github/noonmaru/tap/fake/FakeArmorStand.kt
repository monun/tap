/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap.fake

import com.github.noonmaru.tap.packet.EntityPacket
import com.github.noonmaru.tap.packet.sendPacket
import com.github.noonmaru.tap.packet.sendPacketAll
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * @author Nemo
 */
class FakeArmorStand(override val entity: ArmorStand) : FakeLivingEntity(entity) {

    var mark
        get() = entity.isMarker
        set(value) {
            entity.isMarker = value
            updateMeta = true
            enqueue()
        }

    var headPose
        get() = entity.headPose
        set(value) {
            entity.headPose = value
            updateMeta = true
            enqueue()
        }

    var bodyPose
        get() = entity.bodyPose
        set(value) {
            entity.bodyPose = value
            updateMeta = true
            enqueue()
        }

    var leftArmPose
        get() = entity.leftArmPose
        set(value) {
            entity.leftArmPose = value
            updateMeta = true
            enqueue()
        }

    var rightArmPose
        get() = entity.rightArmPose
        set(value) {
            entity.rightArmPose = value
            updateMeta = true
            enqueue()
        }

    var leftLegPose
        get() = entity.leftLegPose
        set(value) {
            entity.leftLegPose = value
            updateMeta = true
            enqueue()
        }

    var rightLegPose
        get() = entity.rightLegPose
        set(value) {
            entity.rightLegPose = value
            updateMeta = true
            enqueue()
        }

    private var updateEquipment = false

    override fun spawnTo(player: Player) {
        super.spawnTo(player)

        EquipmentSlot.values().forEach { slot ->
            val item = entity.getItem(slot)

            if (item.amount > 0) {
                val packet = EntityPacket.equipment(entity.entityId, slot, item)

                player.sendPacket(packet)
            }
        }
    }

    fun setItem(slot: EquipmentSlot, item: ItemStack) {
        entity.setItem(slot, item)
        updateEquipment = true
        enqueue()
    }

    override fun onUpdate() {
        super.onUpdate()

        if (updateEquipment) {
            updateEquipment = false

            EquipmentSlot.values().forEach { slot ->
                val item = entity.getItem(slot)

                if (item.amount > 0) {
                    val packet = EntityPacket.equipment(entity.entityId, slot, item)

                    trackers.sendPacketAll(packet)
                }
            }
        }
    }
}