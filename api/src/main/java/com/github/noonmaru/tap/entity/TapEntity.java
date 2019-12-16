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

package com.github.noonmaru.tap.entity;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.math.BoundingBox;
import org.bukkit.entity.Entity;

public interface TapEntity
{

    static TapEntity wrapEntity(Entity entity)
    {
        return Tap.ENTITY.wrapEntity(entity);
    }

    Entity getBukkitEntity();

    int getId();

    float getWidth();

    float getHeight();

    double getPrevX();

    double getPrevY();

    double getPrevZ();

    double getPosX();

    double getPosY();

    double getPosZ();

    float getYaw();

    float getPitch();

    boolean isInvisible();

    void setInvisible(boolean invisible);

    boolean isGlowing();

    void setGlowing(boolean glowing);

    boolean isOnGround();

    boolean isDead();

    BoundingBox getBoundingBox();

    void setPosition(double x, double y, double z);

    void setPositionAndRotation(double x, double y, double z, float yaw, float pitch);

    void setCustomName(String name);

    void setCustomNameVisible(boolean visible);

    void setGravity(boolean gravity);

}
