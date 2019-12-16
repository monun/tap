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

package com.github.noonmaru.tap.item;

public final class HideFlag
{
    public static final int ENCHANTMENTS = 1;

    public static final int ATTRIBUTE_MODIFIERS = 2;

    public static final int UNBREAKABLE = 4;

    public static final int CAN_DESTROY = 8;

    public static final int CAN_PLACE_ON = 16;

    public static final int INFORMATION = 32;

    public static final int ALL = 63;

    private HideFlag()
    {}
}
