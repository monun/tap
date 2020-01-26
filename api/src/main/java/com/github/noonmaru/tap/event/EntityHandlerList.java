/*
 * Copyright (c) 2020 Noonmaru
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

package com.github.noonmaru.tap.event;

import com.github.noonmaru.tap.collection.SortedList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nemo
 */
public final class EntityHandlerList {

    private final SortedList<RegisteredEntityHandler> handlerList = new SortedList<>();

    private RegisteredEntityHandler[] handlers;

    public synchronized void register(@NotNull final RegisteredEntityHandler handler) {
        handlers = null;
        handlerList.add(handler);
    }

    @NotNull
    public synchronized void bake() {
        if (handlers != null) return;
        handlers = handlerList.toArray(new RegisteredEntityHandler[0]);
    }

    @NotNull
    public RegisteredEntityHandler[] getRegisteredHandlers() {
        RegisteredEntityHandler[] handlers;
        while ((handlers = this.handlers) == null) bake();
        return handlers;
    }

    public void callEvent(@NotNull final Event event, @NotNull final EventEntityProvider provider) {
        for (RegisteredEntityHandler handler : handlerList) {
            if (provider == handler.getStatement().getProvider()) {
                handler.callEvent(event);
            }
        }
    }

}
