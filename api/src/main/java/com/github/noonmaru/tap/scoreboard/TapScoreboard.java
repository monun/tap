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

package com.github.noonmaru.tap.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public interface TapScoreboard
{
    TapObjective registerObjective(String name);

    TapObjective unregisterObjective(String name);

    TapObjective getObjective(String name);

    TapObjective[] getObjectives();

    void clearSlot(DisplaySlot slot);

    Set<TapScore> resetScores(String name);

    TapTeam registerTeam(String name);

    TapTeam unregisterTeam(String name);

    TapTeam getTeam(String name);

    TapTeam getEntryTeam(String name);

    TapTeam[] getTeams();

    void registerPlayer(Player player);

    void unregisterPlayer(Player player);

    void unregisterAllPlayers();

    List<Player> getRegisteredPlayers();

    void clear();
}
