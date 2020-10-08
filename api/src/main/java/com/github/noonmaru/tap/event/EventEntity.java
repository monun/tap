/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.event;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nemo
 */
public final class EventEntity {

    private final ConcurrentHashMap<Class<?>, EntityHandlerList> slots = new ConcurrentHashMap<>();

    public void register(@NotNull final RegisteredEntityListener registeredEntityListener) {

        for (RegisteredEntityHandler handler : registeredEntityListener.getHandlers()) {
            HandlerStatement statement = handler.getStatement();
            EntityHandlerList handlerList = slots.computeIfAbsent(statement.getRegistrationClass(), registrationClass -> new EntityHandlerList());
            handlerList.register(handler);
        }
    }

    public void unregister(@NotNull final RegisteredEntityListener registeredListener) {
        for (RegisteredEntityHandler handler : registeredListener.getHandlers()) {
            handler.remove();
            slots.get(handler.getStatement().getRegistrationClass()).unregister(handler);
        }
    }

    public void unregister(@NotNull final ListenerStatement statement, @NotNull final Listener listener) {
        for (HandlerStatement handlerStatement : statement.getHandlerStatements()) {
            EntityHandlerList handlerList = slots.get(handlerStatement.getRegistrationClass());

            if (handlerList != null) {
                handlerList.unregister(listener);
            }
        }
    }

    @Nullable
    public EntityHandlerList getHandlerList(@NotNull final Class<?> eventClass) {
        return slots.get(eventClass);
    }

    void unregisterAll() {
        for (EntityHandlerList handlerList : slots.values()) {
            handlerList.unregisterAll();
        }

        slots.clear();
    }

}
