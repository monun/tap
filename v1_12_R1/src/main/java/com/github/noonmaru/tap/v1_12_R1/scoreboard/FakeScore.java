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

import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_12_R1.ScoreboardScore;

public final class FakeScore extends ScoreboardScore
{
    private static final FakeScore INSTANCE = new FakeScore();

    private NMSScore score;

    private FakeScore()
    {
        super(null, FakeObjective.getInstance(), null);
    }

    public static FakeScore getInstance(NMSScore score)
    {
        FakeScore fakeScore = INSTANCE;

        fakeScore.score = score;

        return fakeScore;
    }

    @Override
    public FakeObjective getObjective()
    {
        return FakeObjective.getInstance();
    }

    @Override
    public String getPlayerName()
    {
        return this.score.name;
    }

    @Override
    public int getScore()
    {
        return this.score.score;
    }

    PacketPlayOutScoreboardScore createUpdatePacket()
    {
        NMSScore score = this.score;
        FakeObjective objective = FakeObjective.getInstance(score.objective);
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(this);

        this.score = null;
        objective.objective = null;

        return packet;
    }

    PacketPlayOutScoreboardScore createRemovePacket()
    {
        NMSScore score = this.score;
        FakeObjective objective = FakeObjective.getInstance(score.objective);
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(this.score.name, objective);

        this.score = null;
        objective.objective = null;

        return packet;
    }
}
