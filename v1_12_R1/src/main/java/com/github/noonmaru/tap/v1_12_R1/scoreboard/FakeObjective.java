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

import net.minecraft.server.v1_12_R1.IScoreboardCriteria;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_12_R1.ScoreboardObjective;

public final class FakeObjective extends ScoreboardObjective
{
    private static final FakeObjective INSTANCE = new FakeObjective();

    NMSObjective objective;

    private FakeObjective()
    {
        super(null, null, IScoreboardCriteria.b);
    }

    static FakeObjective getInstance()
    {
        return INSTANCE;
    }

    public static FakeObjective getInstance(NMSObjective objective)
    {
        FakeObjective fakeObjective = INSTANCE;

        fakeObjective.objective = objective;

        return fakeObjective;
    }

    @Override
    public String getName()
    {
        return this.objective.name;
    }

    @Override
    public String getDisplayName()
    {
        return this.objective.displayName;
    }

    PacketPlayOutScoreboardObjective createCreatePacket()
    {
        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective(this, 0);

        this.objective = null;

        return packet;
    }

    PacketPlayOutScoreboardObjective createUpdatePacket()
    {
        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective(this, 2);

        this.objective = null;

        return packet;
    }

    PacketPlayOutScoreboardDisplayObjective createDisplayPacket()
    {
        PacketPlayOutScoreboardDisplayObjective packet = new PacketPlayOutScoreboardDisplayObjective(this.objective.getDisplaySlot().ordinal(), this);

        this.objective = null;

        return packet;
    }

    PacketPlayOutScoreboardObjective createRemovePacket()
    {
        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective(this, 1);

        this.objective = null;

        return packet;
    }
}
