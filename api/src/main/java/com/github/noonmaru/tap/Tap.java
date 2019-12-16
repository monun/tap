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

package com.github.noonmaru.tap;

import com.github.noonmaru.tap.block.TapBlockSupport;
import com.github.noonmaru.tap.command.EntitySelector;
import com.github.noonmaru.tap.entity.TapEntitySupport;
import com.github.noonmaru.tap.inventory.InventorySupport;
import com.github.noonmaru.tap.item.TapItemSupport;
import com.github.noonmaru.tap.math.MathSupport;
import com.github.noonmaru.tap.nbt.NBTSupport;
import com.github.noonmaru.tap.profile.ProfileSupport;
import com.github.noonmaru.tap.scoreboard.ScoreboardSupport;
import com.github.noonmaru.tap.sound.SoundSupport;
import com.github.noonmaru.tap.text.TextSupport;
import com.github.noonmaru.tap.world.WorldSupport;

public final class Tap
{
    public static final boolean DEBUG = false;

    public static final TapBlockSupport BLOCK = LibraryLoader.load(TapBlockSupport.class);

    public static final EntitySelector ENTITY_SELECTOR = LibraryLoader.load(EntitySelector.class);

    public static final TapEntitySupport ENTITY = LibraryLoader.load(TapEntitySupport.class);

    public static final InventorySupport INVENTORY = LibraryLoader.load(InventorySupport.class);

    public static final TapItemSupport ITEM = LibraryLoader.load(TapItemSupport.class);

    public static final MathSupport MATH = LibraryLoader.load(MathSupport.class);

    public static final SoundSupport SOUND = LibraryLoader.load(SoundSupport.class);

    public static final ProfileSupport PROFILE = LibraryLoader.load(ProfileSupport.class);

    public static final ScoreboardSupport SCOREBOARD = LibraryLoader.load(ScoreboardSupport.class);

    public static final TextSupport TEXT = LibraryLoader.load(TextSupport.class);

    public static final WorldSupport WORLD = LibraryLoader.load(WorldSupport.class);

    public static final NBTSupport NBT = LibraryLoader.load(NBTSupport.class);

    private Tap() {}

}
