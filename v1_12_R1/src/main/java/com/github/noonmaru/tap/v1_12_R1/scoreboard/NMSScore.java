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

import com.github.noonmaru.tap.scoreboard.TapScore;

public final class NMSScore implements TapScore
{

    final NMSObjective objective;

    final String name;

    int score;

    private boolean valid;

    NMSScore(NMSObjective objective, String name, int score)
    {
        this.objective = objective;
        this.name = name;
        this.score = score;
        this.valid = true;
    }

    @Override
    public NMSObjective getObjective()
    {
        return this.objective;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public int getScore()
    {
        return this.score;
    }

    @Override
    public void setScore(int score)
    {
        checkState();

        if (this.score == score)
            return;

        this.score = score;

        this.objective.scoreboard.sendAll(FakeScore.getInstance(this).createUpdatePacket());
    }

    @Override
    public void unregister()
    {
        checkState();

        this.objective.unregisterScore(this);
        remove();
    }

    void remove()
    {
        this.valid = false;
    }

    private void checkState()
    {
        if (!this.valid)
            throw new IllegalStateException("Invalid TapScore '" + this.name + "' @" + Integer.toHexString(System.identityHashCode(this)));
    }

}
