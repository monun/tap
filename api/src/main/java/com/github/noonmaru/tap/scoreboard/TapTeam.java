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

import org.bukkit.ChatColor;

import java.util.Set;

public interface TapTeam
{
    TapScoreboard getScoreboard();

    String getName();

    String getDisplayName();

    void setDisplayName(String displayName);

    String getPrefix();

    void setPrefix(String prefix);

    String getSuffix();

    void setSuffix(String suffix);

    boolean allowFriendlyFire();

    void setAllowFriendlyFire(boolean allowFriendlyFire);

    boolean canSeeFriendlyInvisibles();

    void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles);

    NameTagVisibility getNameTagVisibility();

    void setNameTagVisibility(NameTagVisibility visibility);

    CollisionRule getCollisionRule();

    void setCollisionRule(CollisionRule rule);

    void addEntry(String name);

    boolean removeEntry(String name);

    boolean hasEntry(String name);

    Set<String> getEntries();

    int getSize();

    ChatColor getColor();

    void setColor(ChatColor chatColor);

    void unregister();
}
