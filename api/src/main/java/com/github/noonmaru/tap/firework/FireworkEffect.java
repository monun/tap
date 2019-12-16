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

package com.github.noonmaru.tap.firework;

import com.github.noonmaru.tap.LibraryLoader;

import java.util.ArrayList;
import java.util.List;

public interface FireworkEffect
{

    static Builder builder()
    {
        return new Builder();
    }

    enum Type
    {
        SMALL_BALL(0), LARGE_BALL(1), STAR(2), CREEPER(3), BURST(4);

        final byte id;

        Type(int id)
        {
            this.id = (byte) id;
        }

        public static Type getById(byte b)
        {
            Type[] types = values();
            for (Type type : types)
                if (type.id == b)
                    return type;
            return null;
        }

        public final byte getId()
        {
            return this.id;
        }
    }

    final class Builder
    {
        private static final FireworkSupport SUPPORT = LibraryLoader.load(FireworkSupport.class);

        private final List<Integer> colors = new ArrayList<>();

        private final List<Integer> fadeColors = new ArrayList<>();

        private boolean trail;

        private boolean flicker;

        private Type type = Type.SMALL_BALL;

        private Builder() {}

        public Builder trail(boolean trail)
        {
            this.trail = trail;

            return this;
        }

        public Builder flicker(boolean flicker)
        {
            this.flicker = flicker;

            return this;
        }

        public Builder type(Type type)
        {
            this.type = type == null ? Type.SMALL_BALL : type;

            return this;
        }

        public Builder color(int rgb)
        {
            colors.add(rgb);

            return this;
        }

        public Builder color(int... rgbs)
        {
            List<Integer> colors = this.colors;

            for (int rgb : rgbs)
                colors.add(rgb);

            return this;
        }

        public Builder fadeColor(int rgb)
        {
            fadeColors.add(rgb);

            return this;
        }

        public Builder fadeColor(int... rgbs)
        {
            List<Integer> fadeColors = this.fadeColors;

            for (int rgb : rgbs)
                colors.add(rgb);

            return this;
        }

        public boolean isFlicker()
        {
            return this.flicker;
        }

        public boolean isTrail()
        {
            return this.trail;
        }

        public Type getType()
        {
            return this.type;
        }

        public int[] getColors()
        {
            return colors.stream().mapToInt(Integer::intValue).toArray();
        }

        public int[] getFadeColors()
        {
            return fadeColors.stream().mapToInt(Integer::intValue).toArray();
        }

        public FireworkEffect build()
        {
            return SUPPORT.build(this);
        }

    }

}
