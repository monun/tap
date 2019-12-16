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

package com.github.noonmaru.tap.v1_12_R1.scoreboard;

import com.github.noonmaru.tap.scoreboard.CollisionRule;
import com.github.noonmaru.tap.scoreboard.NameTagVisibility;
import net.minecraft.server.v1_12_R1.EnumChatFormat;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import org.bukkit.ChatColor;

import java.util.Collection;

public final class FakeTeam extends ScoreboardTeam
{

    private static final EnumNameTagVisibility[] NAME_TAG_VISIBILITIES;

    private static final EnumTeamPush[] TEAM_PUSHES;

    private static final EnumChatFormat[] CHAT_FORMATS;

    private static final FakeTeam INSTANCE = new FakeTeam();

    static
    {
        EnumNameTagVisibility[] visibilities = new EnumNameTagVisibility[NameTagVisibility.list().size()];
        visibilities[NameTagVisibility.ALWAYS.ordinal()] = EnumNameTagVisibility.ALWAYS;
        visibilities[NameTagVisibility.NEVER.ordinal()] = EnumNameTagVisibility.NEVER;
        visibilities[NameTagVisibility.HIDE_FOR_OTHER_TEAMS.ordinal()] = EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS;
        visibilities[NameTagVisibility.HIDE_FOR_OWN_TEAM.ordinal()] = EnumNameTagVisibility.HIDE_FOR_OWN_TEAM;
        NAME_TAG_VISIBILITIES = visibilities;

        EnumTeamPush[] pushes = new EnumTeamPush[EnumTeamPush.values().length];
        pushes[CollisionRule.ALWAYS.ordinal()] = EnumTeamPush.ALWAYS;
        pushes[CollisionRule.NEVER.ordinal()] = EnumTeamPush.NEVER;
        pushes[CollisionRule.PUSH_FOR_OTHER_TEAMS.ordinal()] = EnumTeamPush.HIDE_FOR_OTHER_TEAMS;
        pushes[CollisionRule.PUSH_FOR_OWN_TEAM.ordinal()] = EnumTeamPush.HIDE_FOR_OWN_TEAM;
        TEAM_PUSHES = pushes;

        EnumChatFormat[] formats = new EnumChatFormat[ChatColor.values().length];
        formats[ChatColor.BLACK.ordinal()] = EnumChatFormat.BLACK;
        formats[ChatColor.DARK_BLUE.ordinal()] = EnumChatFormat.DARK_BLUE;
        formats[ChatColor.DARK_GREEN.ordinal()] = EnumChatFormat.DARK_GREEN;
        formats[ChatColor.DARK_AQUA.ordinal()] = EnumChatFormat.DARK_AQUA;
        formats[ChatColor.DARK_RED.ordinal()] = EnumChatFormat.DARK_RED;
        formats[ChatColor.DARK_PURPLE.ordinal()] = EnumChatFormat.DARK_PURPLE;
        formats[ChatColor.GOLD.ordinal()] = EnumChatFormat.GOLD;
        formats[ChatColor.GRAY.ordinal()] = EnumChatFormat.GRAY;
        formats[ChatColor.DARK_GRAY.ordinal()] = EnumChatFormat.DARK_GRAY;
        formats[ChatColor.BLUE.ordinal()] = EnumChatFormat.BLUE;
        formats[ChatColor.GREEN.ordinal()] = EnumChatFormat.GREEN;
        formats[ChatColor.AQUA.ordinal()] = EnumChatFormat.AQUA;
        formats[ChatColor.RED.ordinal()] = EnumChatFormat.RED;
        formats[ChatColor.LIGHT_PURPLE.ordinal()] = EnumChatFormat.LIGHT_PURPLE;
        formats[ChatColor.YELLOW.ordinal()] = EnumChatFormat.YELLOW;
        formats[ChatColor.WHITE.ordinal()] = EnumChatFormat.WHITE;
        formats[ChatColor.MAGIC.ordinal()] = EnumChatFormat.OBFUSCATED;
        formats[ChatColor.BOLD.ordinal()] = EnumChatFormat.BOLD;
        formats[ChatColor.STRIKETHROUGH.ordinal()] = EnumChatFormat.STRIKETHROUGH;
        formats[ChatColor.UNDERLINE.ordinal()] = EnumChatFormat.UNDERLINE;
        formats[ChatColor.ITALIC.ordinal()] = EnumChatFormat.ITALIC;
        formats[ChatColor.RESET.ordinal()] = EnumChatFormat.RESET;
        CHAT_FORMATS = formats;
    }

    private NMSTeam team;

    private FakeTeam()
    {
        super(null, null);
    }

    public static FakeTeam getInstance(NMSTeam team)
    {
        FakeTeam fakeTeam = INSTANCE;

        fakeTeam.team = team;

        return fakeTeam;
    }

    @Override
    public String getName()
    {
        return this.team.name;
    }

    @Override
    public String getDisplayName()
    {
        return this.team.displayName;
    }

    @Override
    public String getPrefix()
    {
        return this.team.prefix;
    }

    @Override
    public String getSuffix()
    {
        return this.team.suffix;
    }

    @Override
    public boolean allowFriendlyFire()
    {
        return this.team.allowFriendlyFire;
    }

    @Override
    public boolean canSeeFriendlyInvisibles()
    {
        return this.team.canSeeFriendlyInvisibles;
    }

    @Override
    public EnumNameTagVisibility getNameTagVisibility()
    {
        return NAME_TAG_VISIBILITIES[this.team.getNameTagVisibility().ordinal()];
    }

    @Override
    public EnumTeamPush getCollisionRule()
    {
        return TEAM_PUSHES[this.team.getCollisionRule().ordinal()];
    }

    @Override
    public Collection<String> getPlayerNameSet()
    {
        return this.team.entries;
    }

    @Override
    public EnumChatFormat getColor()
    {
        return CHAT_FORMATS[this.team.getColor().ordinal()];
    }

    PacketPlayOutScoreboardTeam createCreatePacket()
    {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(this, 0);

        this.team = null;

        return packet;
    }

    PacketPlayOutScoreboardTeam createRemovePacket()
    {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(this, 1);

        this.team = null;

        return packet;
    }

    PacketPlayOutScoreboardTeam createUpdatePacket()
    {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(this, 2);

        this.team = null;

        return packet;
    }

    PacketPlayOutScoreboardTeam createAddMemberPacket(Collection<String> members)
    {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(this, members, 3);

        this.team = null;

        return packet;
    }

    PacketPlayOutScoreboardTeam createRemoveMemberPacket(Collection<String> members)
    {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(this, members, 4);

        this.team = null;

        return packet;
    }
}
