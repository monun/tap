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

import com.github.noonmaru.tap.LibraryLoader;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface Packet
{

    CustomPacket CUSTOM = LibraryLoader.load(CustomPacket.class);

    EffectPacket EFFECT = LibraryLoader.load(EffectPacket.class);

    EntityPacket ENTITY = LibraryLoader.load(EntityPacket.class);

    InfoPacket INFO = LibraryLoader.load(InfoPacket.class);

    ItemPacket ITEM = LibraryLoader.load(ItemPacket.class);

    ScoreboardPacket SCOREBOARD = LibraryLoader.load(ScoreboardPacket.class);

    StatusPacket STATUS = LibraryLoader.load(StatusPacket.class);

    TitlePacket TITLE = LibraryLoader.load(TitlePacket.class);

    @Deprecated
    default void send(Player player)
    {
        sendTo(player);
    }

    void sendTo(Player player);

    default void sendTo(Iterable<? extends Player> players)
    {
        for (Player player : players)
            sendTo(player);
    }

    void sendAll();

    void sendNearBy(World world, double x, double y, double z, double radius);

    default void sendNearBy(Location loc, double radius)
    {
        sendNearBy(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), radius);
    }

}
