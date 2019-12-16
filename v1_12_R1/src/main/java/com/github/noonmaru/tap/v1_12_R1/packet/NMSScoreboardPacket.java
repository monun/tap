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

package com.github.noonmaru.tap.v1_12_R1.packet;

import com.github.noonmaru.tap.packet.ScoreboardPacket;
import net.minecraft.server.v1_12_R1.IScoreboardCriteria;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_12_R1.ScoreboardObjective;

public class NMSScoreboardPacket implements ScoreboardPacket
{
    private static final FakeObjecitve SCOREBOARD_OBJECTIVE = new FakeObjecitve();

    @Override
    public NMSPacket scoreboardDisplayName(String name, String displayName)
    {
        FakeObjecitve objective = SCOREBOARD_OBJECTIVE;

        objective.name = name;
        objective.displayName = displayName;

        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective(objective, 2);

        objective.name = null;
        objective.displayName = null;

        return new NMSPacketFixed(packet);
    }

    private static class FakeObjecitve extends ScoreboardObjective
    {
        String name;

        String displayName;

        FakeObjecitve()
        {
            super(null, "", IScoreboardCriteria.b);
        }

        @Override
        public String getDisplayName()
        {
            return this.displayName;
        }

        @Override
        public String getName()
        {
            return this.name;
        }
    }

}
