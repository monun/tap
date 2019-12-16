/*
 * Copyright (c) 2019 Noonmaru
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

package com.github.noonmaru.tap.v1_12_R1.packet;

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.math.VectorSpace;
import com.github.noonmaru.tap.Effect;
import com.github.noonmaru.tap.Particle;
import com.github.noonmaru.tap.firework.FireworkEffect;
import com.github.noonmaru.tap.packet.EffectPacket;
import com.github.noonmaru.tap.sound.Sound;
import com.github.noonmaru.tap.v1_12_R1.firework.NMSFireworkEffect;
import com.github.noonmaru.tap.v1_12_R1.sound.NMSSound;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NMSEffectPacket implements EffectPacket
{

    private static final EnumParticle[] PARTICLES;

    private static final SoundCategory[] SOUND_CATEGORIES;

    private static final EntityLightning LIGHTNING = new EntityLightning(((CraftServer) Bukkit.getServer()).getServer().getWorld(), 0.0D, 0.0D, 0.0D, true);

    static
    {
        EnumParticle[] particles = new EnumParticle[Math.max(Particle.values().length, EnumParticle.values().length)];

        particles[Particle.EXPLOSION_NORMAL.ordinal()] = EnumParticle.EXPLOSION_NORMAL;
        particles[Particle.EXPLOSION_LARGE.ordinal()] = EnumParticle.EXPLOSION_LARGE;
        particles[Particle.EXPLOSION_HUGE.ordinal()] = EnumParticle.EXPLOSION_HUGE;
        particles[Particle.FIREWORKS_SPARK.ordinal()] = EnumParticle.FIREWORKS_SPARK;
        particles[Particle.WATER_BUBBLE.ordinal()] = EnumParticle.WATER_BUBBLE;
        particles[Particle.WATER_SPLASH.ordinal()] = EnumParticle.WATER_SPLASH;
        particles[Particle.WATER_WAKE.ordinal()] = EnumParticle.WATER_WAKE;
        particles[Particle.SUSPENDED.ordinal()] = EnumParticle.SUSPENDED;
        particles[Particle.SUSPENDED_DEPTH.ordinal()] = EnumParticle.SUSPENDED_DEPTH;
        particles[Particle.CRIT.ordinal()] = EnumParticle.CRIT;
        particles[Particle.CRIT_MAGIC.ordinal()] = EnumParticle.CRIT_MAGIC;
        particles[Particle.SMOKE_NORMAL.ordinal()] = EnumParticle.SMOKE_NORMAL;
        particles[Particle.SMOKE_LARGE.ordinal()] = EnumParticle.SMOKE_LARGE;
        particles[Particle.SPELL.ordinal()] = EnumParticle.SPELL;
        particles[Particle.SPELL_INSTANT.ordinal()] = EnumParticle.SPELL_INSTANT;
        particles[Particle.SPELL_MOB.ordinal()] = EnumParticle.SPELL_MOB;
        particles[Particle.SPELL_MOB_AMBIENT.ordinal()] = EnumParticle.SPELL_MOB_AMBIENT;
        particles[Particle.SPELL_WITCH.ordinal()] = EnumParticle.SPELL_WITCH;
        particles[Particle.DRIP_WATER.ordinal()] = EnumParticle.DRIP_WATER;
        particles[Particle.DRIP_LAVA.ordinal()] = EnumParticle.DRIP_LAVA;
        particles[Particle.VILLAGER_ANGRY.ordinal()] = EnumParticle.VILLAGER_ANGRY;
        particles[Particle.VILLAGER_HAPPY.ordinal()] = EnumParticle.VILLAGER_HAPPY;
        particles[Particle.TOWN_AURA.ordinal()] = EnumParticle.TOWN_AURA;
        particles[Particle.NOTE.ordinal()] = EnumParticle.NOTE;
        particles[Particle.PORTAL.ordinal()] = EnumParticle.PORTAL;
        particles[Particle.ENCHANTMENT_TABLE.ordinal()] = EnumParticle.ENCHANTMENT_TABLE;
        particles[Particle.FLAME.ordinal()] = EnumParticle.FLAME;
        particles[Particle.LAVA.ordinal()] = EnumParticle.LAVA;
        particles[Particle.FOOTSTEP.ordinal()] = EnumParticle.FOOTSTEP;
        particles[Particle.CLOUD.ordinal()] = EnumParticle.CLOUD;
        particles[Particle.REDSTONE.ordinal()] = EnumParticle.REDSTONE;
        particles[Particle.SNOWBALL.ordinal()] = EnumParticle.SNOWBALL;
        particles[Particle.SNOW_SHOVEL.ordinal()] = EnumParticle.SNOW_SHOVEL;
        particles[Particle.SLIME.ordinal()] = EnumParticle.SLIME;
        particles[Particle.HEART.ordinal()] = EnumParticle.HEART;
        particles[Particle.BARRIER.ordinal()] = EnumParticle.BARRIER;
        particles[Particle.ITEM_CRACK.ordinal()] = EnumParticle.ITEM_CRACK;
        particles[Particle.BLOCK_CRACK.ordinal()] = EnumParticle.BLOCK_CRACK;
        particles[Particle.BLOCK_DUST.ordinal()] = EnumParticle.BLOCK_DUST;
        particles[Particle.WATER_DROP.ordinal()] = EnumParticle.WATER_DROP;
        particles[Particle.ITEM_TAKE.ordinal()] = EnumParticle.ITEM_TAKE;
        particles[Particle.MOB_APPEARANCE.ordinal()] = EnumParticle.MOB_APPEARANCE;
        particles[Particle.DRAGON_BREATH.ordinal()] = EnumParticle.DRAGON_BREATH;
        particles[Particle.END_ROD.ordinal()] = EnumParticle.END_ROD;
        particles[Particle.DAMAGE_INDICATOR.ordinal()] = EnumParticle.DAMAGE_INDICATOR;
        particles[Particle.SWEEP_ATTACK.ordinal()] = EnumParticle.SWEEP_ATTACK;
        particles[Particle.FALLING_DUST.ordinal()] = EnumParticle.FALLING_DUST;
        particles[Particle.TOTEM.ordinal()] = EnumParticle.TOTEM;
        particles[Particle.SPIT.ordinal()] = EnumParticle.SPIT;
        PARTICLES = particles;

        SoundCategory[] soundCategories = new SoundCategory[Math.max(com.github.noonmaru.tap.sound.SoundCategory.values().length, SoundCategory.values().length)];
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.MASTER.ordinal()] = SoundCategory.MASTER;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.MUSIC.ordinal()] = SoundCategory.MUSIC;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.RECORDS.ordinal()] = SoundCategory.RECORDS;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.WEATHER.ordinal()] = SoundCategory.WEATHER;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.BLOCKS.ordinal()] = SoundCategory.BLOCKS;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.HOSTILE.ordinal()] = SoundCategory.HOSTILE;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.NEUTRAL.ordinal()] = SoundCategory.NEUTRAL;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.PLAYERS.ordinal()] = SoundCategory.PLAYERS;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.AMBIENT.ordinal()] = SoundCategory.AMBIENT;
        soundCategories[com.github.noonmaru.tap.sound.SoundCategory.VOICE.ordinal()] = SoundCategory.VOICE;
        SOUND_CATEGORIES = soundCategories;
    }

    @Override
    public NMSPacket effect(Effect effect, int x, int y, int z, int data)
    {
        return new NMSPacketFixed(new PacketPlayOutWorldEvent(effect.getId(), new BlockPosition(x, y, z), data, false));
    }

    @Override
    public NMSPacket particle(Particle particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float particleData, int particleCount, int... data)
    {
        return new NMSPacketFixed(new PacketPlayOutWorldParticles(PARTICLES[particle.ordinal()], true, x, y, z, offsetX, offsetY, offsetZ, particleData, particleCount, data));
    }

    @Override
    public NMSPacket explosion(double x, double y, double z, float radius, VectorSpace records, Vector push)
    {
        List<BlockPosition> positions;

        if (records == null || records.size() == 0)
            positions = Collections.emptyList();
        else
        {
            positions = new ArrayList<>(records.size());

            for (Vector v : records.getVectors())
                positions.add(new BlockPosition(x + v.x, y + v.y, z + v.z));
        }

        Vec3D vec = push == null ? null : new Vec3D(push.x, push.y, push.z);

        return new NMSPacketFixed(new PacketPlayOutExplosion(x, y, z, radius, positions, vec));
    }

    @Override
    public NMSPacket firework(FireworkEffect firework, double x, double y, double z)
    {
        EntityFireworks entity = NMSFireworkEffect.ENTITY_FIRE_WORK;
        entity.locX = x;
        entity.locY = y;
        entity.locZ = z;

        int id = entity.getId();
        Packet<?>[] packets = new Packet[4];
        packets[0] = new PacketPlayOutSpawnEntity(entity, 76);
        packets[1] = new PacketPlayOutEntityMetadata(id, ((NMSFireworkEffect) firework).getHandle(), true);
        packets[2] = new PacketPlayOutEntityStatus(entity, (byte) 17);
        packets[3] = new PacketPlayOutEntityDestroy(id);

        return new NMSPacketMulti(packets);
    }

    @Override
    public NMSPacket namedSound(Sound sound, com.github.noonmaru.tap.sound.SoundCategory category, double x, double y, double z, float volume, float pitch)
    {
        return new NMSPacketFixed(new PacketPlayOutNamedSoundEffect(((NMSSound) sound).getHandle(), SOUND_CATEGORIES[category.ordinal()], x, y, z, volume, pitch));
    }

    @Override
    public NMSPacket customSound(String sound, com.github.noonmaru.tap.sound.SoundCategory category, double x, double y, double z, float volume, float pitch)
    {
        return new NMSPacketFixed(new PacketPlayOutCustomSoundEffect(sound, SOUND_CATEGORIES[category.ordinal()], x, y, z, volume, pitch));
    }

    @Override
    public NMSPacket stopSound(com.github.noonmaru.tap.sound.SoundCategory category, String sound)
    {
        String categoryName = SOUND_CATEGORIES[category.ordinal()].a();

        return (NMSPacketLazy) () -> {
            PacketDataSerializer buffer = new PacketDataSerializer(Unpooled.buffer());
            buffer.a(categoryName);
            buffer.a(sound);

            return new PacketPlayOutCustomPayload("MC|StopSound", buffer);
        };
    }

    @Override
    public NMSPacket thunderbolt(double x, double y, double z)
    {
        EntityLightning lightning = LIGHTNING;
        lightning.setPosition(x, y, z);

        return new NMSPacketMulti(new PacketPlayOutSpawnEntityWeather(lightning), new PacketPlayOutEntityDestroy(lightning.getId()));
    }

}
