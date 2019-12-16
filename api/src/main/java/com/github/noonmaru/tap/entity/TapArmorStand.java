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


import com.github.noonmaru.math.Vector;
import org.bukkit.entity.ArmorStand;

public interface TapArmorStand extends TapLivingEntity
{

    @Override
    ArmorStand getBukkitEntity();

    Vector getHeadPos();

    boolean isMarker();

    void setMarker(boolean marker);

    void setBasePlate(boolean basePlate);

    boolean hasBasePlate();

    void setArms(boolean arms);

    boolean hasArms();

    boolean isSmall();

    void setSmall(boolean small);

    void setHeadPose(float x, float y, float z);

    Vector getHeadPose();

    void setBodyPose(float x, float y, float z);

    Vector getBodyPose();

    void setLeftArmPose(float x, float y, float z);

    Vector getLeftArmPose();

    void setRightArmPose(float x, float y, float z);

    Vector getRightArmPose();

    void setLeftLegPose(float x, float y, float z);

    Vector getLeftLegPose();

    void setRightLegPose(float x, float y, float z);

    Vector getRightLegPose();

}
