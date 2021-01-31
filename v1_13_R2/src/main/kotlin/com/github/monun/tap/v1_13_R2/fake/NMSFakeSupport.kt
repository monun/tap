/*
 * Copyright (c) 2020 Noonmaru
 *  
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.v1_13_R2.fake

import com.github.monun.tap.fake.FakeSupport
import net.minecraft.server.v1_13_R2.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Entity as BukkitEntity


/**
 * @author Nemo
 */
class NMSFakeSupport : FakeSupport {

    override fun getNetworkId(entity: BukkitEntity): Int {
        entity as CraftEntity

        return IRegistry.ENTITY_TYPE.a(entity.handle.P())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : org.bukkit.entity.Entity> createEntity(
        entityClass: Class<out org.bukkit.entity.Entity>,
        world: org.bukkit.World
    ): T? {
        return NMSEntityTypes.findType(entityClass)?.run {
            val nmsWorld = (world as CraftWorld).handle
            this.a(nmsWorld)?.bukkitEntity as T
        }
    }

    override fun isInvisible(entity: BukkitEntity): Boolean {
        entity as CraftEntity
        val nmsEntity = entity.handle

        return nmsEntity.isInvisible
    }

    override fun setInvisible(entity: BukkitEntity, invisible: Boolean) {
        entity as CraftEntity
        val nmsEntity = entity.handle

        nmsEntity.isInvisible = invisible
    }

    override fun setLocation(
        entity: BukkitEntity,
        loc: Location
    ) {
        entity as CraftEntity
        val nmsEntity = entity.handle

        loc.run {
            nmsEntity.world = (world as CraftWorld).handle
            nmsEntity.setPositionRotation(x, y, z, yaw, pitch)
        }
    }

//    Entity.class 1801
//    public void k(Entity entity) {
//        if (this.w(entity)) {
//            entity.setPosition(this.locX, this.locY + this.aJ() + entity.aI(), this.locZ);
//        }
//    }

    override fun getMountedYOffset(entity: BukkitEntity): Double {
        entity as CraftEntity

        return entity.handle.aJ()
    }

    override fun getYOffset(entity: BukkitEntity): Double {
        entity as CraftEntity

        return entity.handle.aI()
    }


    //code from EntityTrackerEntry.class 412 private Packet<?> e()
    override fun createSpawnPacket(entity: BukkitEntity): Any {
        entity as CraftEntity

        return when (val handle = entity.handle) {
            is EntityPlayer -> {
                PacketPlayOutNamedEntitySpawn(handle as EntityHuman?)
            }
            is IAnimal -> {
                PacketPlayOutSpawnEntityLiving(handle as EntityLiving?)
            }
            is EntityPainting -> {
                PacketPlayOutSpawnEntityPainting(handle as EntityPainting?)
            }
            is EntityItem -> {
                PacketPlayOutSpawnEntity(handle, 2, 1)
            }
            is EntityMinecartAbstract -> {
                val entityminecartabstract: EntityMinecartAbstract = handle
                PacketPlayOutSpawnEntity(handle, 10, entityminecartabstract.v().a())
            }
            is EntityBoat -> {
                PacketPlayOutSpawnEntity(handle, 1)
            }
            is EntityExperienceOrb -> {
                PacketPlayOutSpawnEntityExperienceOrb(handle as EntityExperienceOrb?)
            }
            is EntityFishingHook -> {
                val entityhuman: EntityHuman? = handle.i()
                PacketPlayOutSpawnEntity(handle, 90,entityhuman?.id ?: handle.getId())
            }
            else -> {
                val shooter: Entity?
                when (handle) {
                    is EntitySpectralArrow -> {
                        shooter = handle.getShooter()
                        PacketPlayOutSpawnEntity(handle, 91, 1 + (shooter?.id ?: handle.getId()))
                    }
                    is EntityTippedArrow -> {
                        shooter = (handle as EntityArrow).getShooter()
                        PacketPlayOutSpawnEntity(handle, 60, 1 + (shooter?.id ?: handle.getId()))
                    }
                    is EntitySnowball -> {
                        PacketPlayOutSpawnEntity(handle, 61)
                    }
                    is EntityThrownTrident -> {
                        shooter = (handle as EntityArrow).getShooter()
                        PacketPlayOutSpawnEntity(handle, 94, 1 + (shooter?.id ?: handle.getId()))
                    }
                    is EntityLlamaSpit -> {
                        PacketPlayOutSpawnEntity(handle, 68)
                    }
                    is EntityPotion -> {
                        PacketPlayOutSpawnEntity(handle, 73)
                    }
                    is EntityThrownExpBottle -> {
                        PacketPlayOutSpawnEntity(handle, 75)
                    }
                    is EntityEnderPearl -> {
                        PacketPlayOutSpawnEntity(handle, 65)
                    }
                    is EntityEnderSignal -> {
                        PacketPlayOutSpawnEntity(handle, 72)
                    }
                    is EntityFireworks -> {
                        PacketPlayOutSpawnEntity(handle, 76)
                    }
                    is EntityFireball -> {
                        val entityfireball: EntityFireball = handle
                        var b0: Byte = 63
                        when (handle) {
                            is EntitySmallFireball -> {
                                b0 = 64
                            }
                            is EntityDragonFireball -> {
                                b0 = 93
                            }
                            is EntityWitherSkull -> {
                                b0 = 66
                            }
                        }
                        val packetplayoutspawnentity: PacketPlayOutSpawnEntity
                        packetplayoutspawnentity = if (entityfireball.shooter == null) {
                            PacketPlayOutSpawnEntity(handle, b0.toInt(), 0)
                        } else {
                            PacketPlayOutSpawnEntity(handle, b0.toInt(), handle.shooter.id)
                        }
                        packetplayoutspawnentity.a((entityfireball.dirX * 8000.0).toInt())
                        packetplayoutspawnentity.b((entityfireball.dirY * 8000.0).toInt())
                        packetplayoutspawnentity.c((entityfireball.dirZ * 8000.0).toInt())
                        packetplayoutspawnentity
                    }
                    is EntityShulkerBullet -> {
                        val packetplayoutspawnentity1 = PacketPlayOutSpawnEntity(handle, 67, 0)
                        packetplayoutspawnentity1.a((handle.motX * 8000.0).toInt())
                        packetplayoutspawnentity1.b((handle.motY * 8000.0).toInt())
                        packetplayoutspawnentity1.c((handle.motZ * 8000.0).toInt())
                        packetplayoutspawnentity1
                    }
                    is EntityEgg -> {
                        PacketPlayOutSpawnEntity(handle, 62)
                    }
                    is EntityEvokerFangs -> {
                        PacketPlayOutSpawnEntity(handle, 79)
                    }
                    is EntityTNTPrimed -> {
                        PacketPlayOutSpawnEntity(handle, 50)
                    }
                    is EntityEnderCrystal -> {
                        PacketPlayOutSpawnEntity(handle, 51)
                    }
                    is EntityFallingBlock -> {
                        val entityfallingblock: EntityFallingBlock = handle
                        PacketPlayOutSpawnEntity(handle, 70, Block.getCombinedId(entityfallingblock.block))
                    }
                    is EntityArmorStand -> {
                        PacketPlayOutSpawnEntity(handle, 78)
                    }
                    is EntityItemFrame -> {
                        val entityitemframe: EntityItemFrame = handle
                        PacketPlayOutSpawnEntity(handle, 71, entityitemframe.direction!!.a(), entityitemframe.getBlockPosition())
                    }
                    is EntityLeash -> {
                        val entityleash: EntityLeash = handle
                        PacketPlayOutSpawnEntity(handle, 77, 0, entityleash.getBlockPosition())
                    }
                    is EntityAreaEffectCloud -> {
                        PacketPlayOutSpawnEntity(handle, 3)
                    }
                    else -> {
                        throw IllegalArgumentException("Don't know how to add " + handle.javaClass.toString() + "!")
                    }
                }
            }
        }
    }


    override fun createFallingBlock(blockData: BlockData): FallingBlock {
        val entity =
            EntityFallingBlock(
                (Bukkit.getWorlds().first() as CraftWorld).handle,
                0.0,
                0.0,
                0.0,
                (blockData as CraftBlockData).state
            )
        entity.ticksLived = 1

        return entity.bukkitEntity as FallingBlock
    }
}