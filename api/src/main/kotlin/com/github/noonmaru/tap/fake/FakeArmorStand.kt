/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.fake

import com.github.noonmaru.tap.protocol.EntityPacket
import com.github.noonmaru.tap.protocol.sendServerPacket
import com.github.noonmaru.tap.protocol.sendServerPacketAll
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * @author Nemo
 */
class FakeArmorStand(private val armorStand: ArmorStand) : FakeLivingEntity(armorStand) {

    var mark
        get() = armorStand.isMarker
        set(value) {
            armorStand.isMarker = value
            updateMeta = true
            enqueue()
        }

    var arms
        get() = armorStand.hasArms()
        set(value) {
            armorStand.setArms(value)
            updateMeta = true
            enqueue()
        }

    var basePlate
        get() = armorStand.hasBasePlate()
        set(value) {
            armorStand.setBasePlate(value)
            updateMeta = true
            enqueue()
        }

    var headPose
        get() = armorStand.headPose
        set(value) {
            armorStand.headPose = value
            updateMeta = true
            enqueue()
        }

    var bodyPose
        get() = armorStand.bodyPose
        set(value) {
            armorStand.bodyPose = value
            updateMeta = true
            enqueue()
        }

    var leftArmPose
        get() = armorStand.leftArmPose
        set(value) {
            armorStand.leftArmPose = value
            updateMeta = true
            enqueue()
        }

    var rightArmPose
        get() = armorStand.rightArmPose
        set(value) {
            armorStand.rightArmPose = value
            updateMeta = true
            enqueue()
        }

    var leftLegPose
        get() = armorStand.leftLegPose
        set(value) {
            armorStand.leftLegPose = value
            updateMeta = true
            enqueue()
        }

    var rightLegPose
        get() = armorStand.rightLegPose
        set(value) {
            armorStand.rightLegPose = value
            updateMeta = true
            enqueue()
        }

    private var updateEquipment = false

    override fun spawnTo(player: Player) {
        super.spawnTo(player)

        EntityPacket.equipment(armorStand).forEach { packet ->
            player.sendServerPacket(packet)
        }
    }

    fun setItem(slot: EquipmentSlot, item: ItemStack) {
        armorStand.setItem(slot, item)
        updateEquipment = true
        enqueue()
    }

    fun getItem(slot: EquipmentSlot): ItemStack {
        return armorStand.getItem(slot)
    }

    override fun onUpdate() {
        super.onUpdate()

        if (updateEquipment) {
            updateEquipment = false

            EquipmentSlot.values().forEach { slot ->
                val item = armorStand.getItem(slot)

                if (item.amount > 0) {
                    val packet = EntityPacket.equipment(armorStand.entityId, slot, item)

                    trackers.sendServerPacketAll(packet)
                }
            }
        }
    }
}