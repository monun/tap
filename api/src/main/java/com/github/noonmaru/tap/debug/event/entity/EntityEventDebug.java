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

import com.github.noonmaru.tap.debug.DebugProcess;
import com.github.noonmaru.tap.event.entity.EntityEventManager;
import com.github.noonmaru.tap.plugin.TapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * @author Nemo
 */
public class EntityEventDebug extends DebugProcess
{
    EntityEventManager entityEventManager;

    @Override
    public void onStart()
    {
        entityEventManager = EntityEventManager.create(TapPlugin.getInstance());

        //모든 엔티티에게 우클릭시 1회성 폭발 이벤트 추가
        World world = Bukkit.getWorlds().get(0);

        for (Entity entity : world.getEntities())
        {
            DebugListener listener = new DebugListener();

            listener.setRegisteredEntityListener(entityEventManager.registerEvents(entity, listener));
        }
    }

    @Override
    public void onStop()
    {
        entityEventManager.destroy();
        entityEventManager = null;
    }
}
