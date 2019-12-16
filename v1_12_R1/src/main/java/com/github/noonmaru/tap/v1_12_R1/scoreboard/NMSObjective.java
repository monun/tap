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
import com.github.noonmaru.tap.scoreboard.TapObjective;

import java.util.HashMap;
import java.util.Map;

public final class NMSObjective implements TapObjective
{
    final NMSScoreboard scoreboard;

    final String name;

    final Map<String, NMSScore> scoresByName = new HashMap<>();

    String displayName;

    DisplaySlot slot;

    private boolean valid;

    NMSObjective(NMSScoreboard scoreboard, String name)
    {
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = name;
        this.valid = true;
    }

    @Override
    public NMSScoreboard getScoreboard()
    {
        return this.scoreboard;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String displayName)
    {
        checkState();

        if (displayName == null)
            throw new NullPointerException("Display name cannot be null");
        if (displayName.length() > 32)
            throw new IllegalArgumentException("Display name '" + displayName + "' is longer than the limit of 32 characters");

        this.displayName = displayName;

        this.scoreboard.sendAll(FakeObjective.getInstance(this).createUpdatePacket());
    }

    @Override
    public DisplaySlot getDisplaySlot()
    {
        return this.slot;
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot)
    {
        checkState();

        if (slot == null)
            throw new NullPointerException("Slot cannot be null");

        if (this.slot == slot)
            return;

        this.slot = slot;
        this.scoreboard.setDisplaySlot(this, slot);
    }

    @Override
    public NMSScore getScore(String name)
    {
        return this.scoresByName.get(name);
    }

    @Override
    public NMSScore registerScore(String name)
    {
        return registerScore(name, 0);
    }

    @Override
    public NMSScore registerScore(String name, int i)
    {
        Map<String, NMSScore> scoresByName = this.scoresByName;
        NMSScore score = scoresByName.get(name);

        if (score == null)
        {
            score = new NMSScore(this, name, i);
            scoresByName.put(name, score);

            this.scoreboard.sendAll(FakeScore.getInstance(score).createUpdatePacket());
        }

        return score;
    }

    @Override
    public NMSScore unregisterScore(String name)
    {
        NMSScore score = this.scoresByName.remove(name);

        if (score != null)
        {
            score.remove();

            this.scoreboard.sendAll(FakeScore.getInstance(score).createRemovePacket());
        }

        return score;
    }

    void unregisterScore(NMSScore score)
    {
        this.scoresByName.remove(score.name);
        this.scoreboard.sendAll(FakeScore.getInstance(score).createRemovePacket());
    }

    NMSScore resetScore(String name)
    {
        return this.scoresByName.remove(name);
    }

    @Override
    public void clear()
    {
        NMSScoreboard scoreboard = this.scoreboard;

        for (NMSScore score : this.scoresByName.values())
        {
            scoreboard.sendAll(FakeScore.getInstance(score).createRemovePacket());
            score.remove();
        }

        this.scoresByName.clear();
    }

    private void checkState()
    {
        if (!this.valid)
            throw new IllegalStateException("Invalid TapObjective '" + this.name + "' @" + Integer.toHexString(System.identityHashCode(this)));
    }

    @Override
    public void unregister()
    {
        this.scoreboard.unregisterObjective(this);

        remove();
    }

    void remove()
    {
        this.valid = false;

        this.slot = null;

        for (NMSScore score : this.scoresByName.values())
            score.remove();

        this.scoresByName.clear();
    }
}
