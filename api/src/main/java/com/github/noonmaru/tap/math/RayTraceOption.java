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

package com.github.noonmaru.tap.math;

public final class RayTraceOption
{
    public static final int STOP_ON_LIQUID = 1;

    public static final int IGNORE_BLOCK_WITHOUT_BOUNDING_BOX = 2;

    public static final int RETURN_LAST_UNCOLLIDABLE_BLOCK = 4;

    private RayTraceOption()
    {}

    public static boolean is(int option, int flag)
    {
        return (option & flag) != 0;
    }
}
