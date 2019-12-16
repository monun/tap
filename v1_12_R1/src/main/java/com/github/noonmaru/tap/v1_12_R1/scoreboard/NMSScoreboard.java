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

import com.github.noonmaru.tap.scoreboard.DisplaySlot;
import com.github.noonmaru.tap.scoreboard.TapScore;
import com.github.noonmaru.tap.scoreboard.TapScoreboard;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public final class NMSScoreboard implements TapScoreboard
{
    static final Scoreboard FAKE_SCOREBOARD = new Scoreboard();

    private final Map<String, NMSObjective> objectivesByName = new HashMap<>();

    private final NMSObjective[] objectivesBySlot = new NMSObjective[DisplaySlot.list().size()];

    private final Map<String, NMSTeam> teamsByName = new HashMap<>();

    private final Map<String, NMSTeam> teamsByEntry = new HashMap<>();

    private final List<EntityPlayer> players = new ArrayList<>();

    @Override
    public NMSObjective registerObjective(String name)
    {
        Map<String, NMSObjective> objectivesByName = this.objectivesByName;
        NMSObjective objective = objectivesByName.get(name);

        if (objective != null)
            throw new IllegalArgumentException("Name '" + name + "' is already in use");

        objective = new NMSObjective(this, name);

        objectivesByName.put(name, objective);

        sendAll(FakeObjective.getInstance(objective).createCreatePacket());

        return objective;
    }

    @Override
    public NMSObjective unregisterObjective(String name)
    {
        NMSObjective objective = this.objectivesByName.remove(name);

        if (objective != null)
        {
            objective.remove();

            sendAll(FakeObjective.getInstance(objective).createRemovePacket());
        }

        return objective;
    }

    void unregisterObjective(NMSObjective objective)
    {
        this.objectivesByName.remove(objective.name);

        sendAll(FakeObjective.getInstance(objective).createRemovePacket());
    }

    @Override
    public NMSObjective getObjective(String name)
    {
        return this.objectivesByName.get(name);
    }

    @Override
    public NMSObjective[] getObjectives()
    {
        Map<String, NMSObjective> objectivesByName = this.objectivesByName;

        return objectivesByName.values().toArray(new NMSObjective[0]);
    }

    void setDisplaySlot(NMSObjective objective, DisplaySlot slot)
    {
        this.objectivesBySlot[slot.ordinal()] = objective;

        sendAll(FakeObjective.getInstance(objective).createDisplayPacket());
    }

    @Override
    public void clearSlot(DisplaySlot slot)
    {
        NMSObjective[] objectivesBySlot = this.objectivesBySlot;
        int index = slot.ordinal();
        NMSObjective objective = objectivesBySlot[index];

        if (objective != null)
        {
            objective.slot = null;
            objectivesBySlot[index] = null;

            sendAll(new PacketPlayOutScoreboardDisplayObjective(index, null));
        }
    }

    @Override
    public Set<TapScore> resetScores(String name)
    {
        Map<String, NMSObjective> objectivesByName = this.objectivesByName;
        Set<TapScore> tapScores = new HashSet<>(objectivesByName.size());

        for (NMSObjective objective : objectivesByName.values())
        {
            NMSScore score = objective.resetScore(name);

            if (score != null)
            {
                score.remove();
                tapScores.add(score);
            }
        }

        if (tapScores.size() > 0)
            sendAll(new PacketPlayOutScoreboardScore(name));

        return tapScores;
    }

    @Override
    public NMSTeam registerTeam(String name)
    {
        if (name == null)
            throw new NullPointerException("Name cannot be null");
        if (name.length() > 16)
            throw new IllegalArgumentException("Name '" + name + "' is longer than the limit of 16 characters");

        Map<String, NMSTeam> teamsByName = this.teamsByName;

        if (teamsByName.containsKey(name))
            throw new IllegalArgumentException("Name '" + name + "' is already in use");

        NMSTeam team = new NMSTeam(this, name);

        teamsByName.put(name, team);

        sendAll(FakeTeam.getInstance(team).createCreatePacket());

        return team;
    }

    @Override
    public NMSTeam unregisterTeam(String name)
    {
        NMSTeam team = this.teamsByName.remove(name);

        if (team != null)
        {
            team.remove();

            Map<String, NMSTeam> teamsByEntry = this.teamsByEntry;

            for (String entry : team.entries)
                teamsByEntry.remove(entry);

            sendAll(FakeTeam.getInstance(team).createRemovePacket());
        }

        return team;
    }

    void unregisterTeam(NMSTeam team)
    {
        this.teamsByName.remove(team.name);

        Map<String, NMSTeam> teamsByEntry = this.teamsByEntry;

        for (String entry : team.entries)
            teamsByEntry.remove(entry);

        sendAll(FakeTeam.getInstance(team).createRemovePacket());
    }

    @Override
    public NMSTeam getTeam(String name)
    {
        return this.teamsByName.get(name);
    }

    @Override
    public NMSTeam getEntryTeam(String name)
    {
        return this.teamsByEntry.get(name);
    }

    void setEntryTeam(String name, NMSTeam team)
    {
        NMSTeam oldTeam = this.teamsByEntry.put(name, team);

        if (oldTeam != null)
            oldTeam.removeEntryWithoutUpdate(name);

        sendAll(FakeTeam.getInstance(team).createAddMemberPacket(Collections.singletonList(name)));
    }

    void removeEntryTeam(String name, NMSTeam team)
    {
        this.teamsByEntry.remove(name);

        sendAll(FakeTeam.getInstance(team).createRemoveMemberPacket(Collections.singletonList(name)));
    }

    @Override
    public NMSTeam[] getTeams()
    {
        Map<String, NMSTeam> teamsByName = this.teamsByName;

        return teamsByName.values().toArray(new NMSTeam[0]);
    }

    void sendAll(Packet<?> packet)
    {
        List<EntityPlayer> players = this.players;

        for (EntityPlayer player : players)
            player.playerConnection.sendPacket(packet);
    }

    @Override
    public void registerPlayer(Player player)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PlayerConnection conn = entityPlayer.playerConnection;

        if (conn == null)
            return;

        for (NMSObjective objective : this.objectivesByName.values())
        {
            conn.sendPacket(FakeObjective.getInstance(objective).createCreatePacket());

            for (NMSScore score : objective.scoresByName.values())
                conn.sendPacket(FakeScore.getInstance(score).createUpdatePacket());
        }

        List<DisplaySlot> slots = DisplaySlot.list();
        NMSObjective[] objectivesBySlot = this.objectivesBySlot;

        for (int i = 0, size = slots.size(); i < size; i++)
        {
            NMSObjective objective = objectivesBySlot[i];

            if (objective != null)
                conn.sendPacket(FakeObjective.getInstance(objective).createDisplayPacket());
        }

        for (NMSTeam team : this.teamsByName.values())
            conn.sendPacket(FakeTeam.getInstance(team).createCreatePacket());

        this.players.add(entityPlayer);
    }

    @Override
    public void unregisterPlayer(Player player)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        if (this.players.remove(entityPlayer))
        {
            PlayerConnection conn = entityPlayer.playerConnection;

            if (conn != null)
            {
                for (NMSObjective objective : this.objectivesByName.values())
                    conn.sendPacket(FakeObjective.getInstance(objective).createRemovePacket());

                for (NMSTeam team : this.teamsByName.values())
                    conn.sendPacket(FakeTeam.getInstance(team).createRemovePacket());
            }
        }
    }

    @Override
    public void unregisterAllPlayers()
    {
        for (EntityPlayer entityPlayer : this.players)
        {
            PlayerConnection conn = entityPlayer.playerConnection;

            if (conn != null)
            {
                for (NMSObjective objective : this.objectivesByName.values())
                    conn.sendPacket(FakeObjective.getInstance(objective).createRemovePacket());

                for (NMSTeam team : this.teamsByName.values())
                    conn.sendPacket(FakeTeam.getInstance(team).createRemovePacket());
            }
        }

        this.players.clear();
    }

    @Override
    public List<Player> getRegisteredPlayers()
    {
        List<EntityPlayer> players = this.players;
        List<Player> ret = new ArrayList<>();

        for (EntityPlayer player : players)
            ret.add(player.getBukkitEntity());

        return ret;
    }

    @Override
    public void clear()
    {
        for (EntityPlayer entityPlayer : this.players)
        {
            PlayerConnection conn = entityPlayer.playerConnection;

            if (conn != null)
            {
                for (NMSObjective objective : this.objectivesByName.values())
                    conn.sendPacket(FakeObjective.getInstance(objective).createRemovePacket());

                for (NMSTeam team : this.teamsByName.values())
                    conn.sendPacket(FakeTeam.getInstance(team).createRemovePacket());
            }
        }

        for (NMSObjective objective : this.objectivesByName.values())
            objective.remove();

        this.objectivesByName.clear();

        Arrays.fill(this.objectivesBySlot, null);

        for (NMSTeam team : this.teamsByName.values())
            team.remove();

        this.teamsByName.clear();
        this.teamsByEntry.clear();
    }
}
