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

package com.github.noonmaru.tap.packet;

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.math.VectorSpace;
import com.github.noonmaru.tap.Effect;
import com.github.noonmaru.tap.Particle;
import com.github.noonmaru.tap.firework.FireworkEffect;
import com.github.noonmaru.tap.sound.Sound;
import com.github.noonmaru.tap.sound.SoundCategory;

public interface EffectPacket
{

    Packet effect(Effect effect, int x, int y, int z, int data);

    Packet explosion(double x, double y, double z, float radius, VectorSpace records, Vector push);

    Packet particle(Particle particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float particleData, int particleCount, int... data);

    Packet firework(FireworkEffect firework, double x, double y, double z);

    @Deprecated
    default Packet sound(Sound sound, SoundCategory category, double x, double y, double z, float volume, float pitch)
    {
        return namedSound(sound, category, x, y, z, volume, pitch);
    }

    Packet namedSound(Sound sound, SoundCategory category, double x, double y, double z, float volume, float pitch);

    Packet customSound(String sound, SoundCategory category, double x, double y, double z, float volume, float pitch);

    Packet stopSound(SoundCategory category, String sound);

    Packet thunderbolt(double x, double y, double z);

}
