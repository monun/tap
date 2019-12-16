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

package com.github.noonmaru.tap;

public enum Effect
{
    CLICK2(1000, Type.SOUND),
    CLICK1(1001, Type.SOUND),
    BOW_FIRE(1002, Type.SOUND),
    ENDEREYE_LAUNCH(1003, Type.SOUND),
    FIREWORK_SHOOT(1004, Type.SOUND),
    IRON_DOOR_TOGGLE(1005, Type.SOUND),
    DOOR_TOGGLE(1006, Type.SOUND),
    TRAPDOOR_TOGGLE(1007, Type.SOUND),
    FENCE_GATE_TOGGLE(1008, Type.SOUND),
    EXTINGUISH(1009, Type.SOUND),
    RECORD_PLAY(1010, Type.SOUND),
    IRON_DOOR_CLOSE(1011, Type.SOUND),
    DOOR_CLOSE(1012, Type.SOUND),
    TRAPDOOR_CLOSE(1013, Type.SOUND),
    FENCE_GATE_CLOSE(1014, Type.SOUND),
    GHAST_SHRIEK(1015, Type.SOUND),
    GHAST_SHOOT(1016, Type.SOUND),
    ENDERDRAGON_SHOOT(1017, Type.SOUND),
    BLAZE_SHOOT(1018, Type.SOUND),
    ZOMBIE_CHEW_WOODEN_DOOR(1019, Type.SOUND),
    ZOMBIE_CHEW_IRON_DOOR(1020, Type.SOUND),
    ZOMBIE_DESTROY_DOOR(1021, Type.SOUND),
    WITHER_BREAK_BLOCK(1022, Type.SOUND),
    WITHER_SPAWN(1023, Type.SOUND),
    WITHER_SHOOT(1024, Type.SOUND),
    BAT_TAKEOFF(1025, Type.SOUND),
    ZOMBIE_INFECT(1026, Type.SOUND),
    ZOMBIE_CONVERTED_VILLAGER(1027, Type.SOUND),
    ANVIL_BREAK(1029, Type.SOUND),
    ANVIL_USE(1030, Type.SOUND),
    ANVIL_LAND(1031, Type.SOUND),
    PORTAL_TRAVEL(1032, Type.SOUND),
    CHORUS_FLOWER_GROW(1033, Type.SOUND),
    CHORUS_FLOWER_DEATH(1034, Type.SOUND),
    BREWING_STAND_BREW(1035, Type.SOUND),
    IRON_TRAPDOOR_CLOSE(1036, Type.SOUND),
    IRON_TRAPDOOR_TOGGLE(1037, Type.SOUND),
    SMOKE(2000, Type.VISUAL),
    BLOCK_BREAK(2001, Type.SOUND),
    POTION_BREAK(2002, Type.VISUAL),
    ENDER_SIGNAL(2003, Type.VISUAL),
    MOBSPAWNER_FLAMES(2004, Type.VISUAL),
    PLANT_GROW(2005, Type.VISUAL),
    DRAGON_BREATH(2006, Type.VISUAL),
    END_GATEWAY_SPAWN(3000, Type.VISUAL),
    ENDERDRAGON_GROWL(3001, Type.SOUND);

    private final int id;

    private final Type type;

    Effect(int id, Type type)
    {
        this.id = id;
        this.type = type;
    }

    public int getId()
    {
        return this.id;
    }

    public Type getType()
    {
        return this.type;
    }

    public enum Type
    {
        SOUND, VISUAL
    }
}
