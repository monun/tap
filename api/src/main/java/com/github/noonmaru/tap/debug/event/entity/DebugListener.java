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

package com.github.noonmaru.tap.debug.event.entity;

import com.github.noonmaru.tap.event.entity.EntityHandler;
import com.github.noonmaru.tap.event.entity.EntityListener;
import com.github.noonmaru.tap.event.entity.EntityProvider;
import com.github.noonmaru.tap.event.entity.RegisteredEntityListener;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * @author Nemo
 */
public class DebugListener implements EntityListener
{
    private RegisteredEntityListener registeredEntityListener;

    public void setRegisteredEntityListener(RegisteredEntityListener registeredEntityListener)
    {
        this.registeredEntityListener = registeredEntityListener;
    }

    @EntityHandler(provider = EntityProvider.PlayerInteractEntity.Clicked.class)
    public void onClicked(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();

        registeredEntityListener.unregister();

        Location loc = entity.getLocation();
        loc.getWorld().createExplosion(loc, 0.1F);
    }
}
