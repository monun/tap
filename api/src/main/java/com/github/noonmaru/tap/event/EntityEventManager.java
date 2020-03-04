/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author Nemo
 */
public final class EntityEventManager {

    private final Plugin plugin;

    private final EventPriority priority;

    private final HashMap<Class<?>, ListenerStatement> statements = new HashMap<>();

    private final HashMap<Class<?>, EventListener> listeners = new HashMap<>();

    private final Map<Entity, EventEntity> entities = new MapMaker().weakKeys().makeMap();

    private final EventExecutor eventExecutor = (listener, event) -> ((EventListener) listener).onEvent(event);

    public EntityEventManager(@NotNull final Plugin plugin) {
        this(plugin, EventPriority.NORMAL);
    }

    public EntityEventManager(@NotNull final Plugin plugin, @NotNull final EventPriority priority) {
        this.plugin = plugin;
        this.priority = priority;
    }

    @NotNull
    public RegisteredEntityListener registerEvents(@NotNull final Entity entity, @NotNull final Listener listener) {

        Preconditions.checkArgument(entity.isValid(), "Invalid entity: " + entity);

        ListenerStatement listenerStatement = createRegisteredListenerStatement(listener.getClass());
        EventEntity eventEntity = entities.computeIfAbsent(entity, target -> new EventEntity());

        RegisteredEntityListener registeredEntityListener = new RegisteredEntityListener(eventEntity, listenerStatement, listener);
        eventEntity.register(registeredEntityListener);

        return registeredEntityListener;
    }

    @NotNull
    private ListenerStatement createRegisteredListenerStatement(@NotNull final Class<?> listenerClass) {
        return statements.computeIfAbsent(listenerClass, clazz -> {
            ListenerStatement statement = ListenerStatement.getOrCreate(clazz);

            for (HandlerStatement statementStatement : statement.getHandlerStatements()) {
                registerEvent(statementStatement);
            }

            return statement;
        });
    }

    private void registerEvent(@NotNull final HandlerStatement statement) {
        Class<?> registrationClass = statement.getRegistrationClass();

        EventListener listener = listeners.computeIfAbsent(registrationClass, clazz -> {
            EventListener newListener = new EventListener();
            plugin.getServer().getPluginManager().registerEvent(clazz.asSubclass(Event.class), newListener, priority, eventExecutor, plugin, false);

            return newListener;
        });

        listener.addProvider(statement.getProvider());
    }

    public void unregisterEvent(@NotNull final Entity entity, @NotNull final Listener listener) {
        EventEntity eventEntity = entities.get(entity);

        if (eventEntity != null) {
            ListenerStatement statement = statements.get(listener.getClass());

            if (statement != null) {
                eventEntity.unregister(statement, listener);
            }
        }
    }

    private class EventListener implements Listener {
        private final LinkedHashSet<EventEntityProvider> providers = new LinkedHashSet<>();

        private EventEntityProvider[] bake;

        @SuppressWarnings("unchecked")
        void onEvent(@NotNull final Event event) {
            for (EventEntityProvider provider : getBake()) {
                Class<?> eventClass = event.getClass();

                if (provider.getEventClass().isAssignableFrom(eventClass)) {
                    Entity entity = provider.getProvider().getFrom(event);

                    if (entity != null) {
                        EventEntity eventEntity = entities.get(entity);

                        if (eventEntity != null) {
                            Class<?> regClass = EventTools.getRegistrationClass(eventClass);
                            EntityHandlerList handlers = eventEntity.getHandlerList(regClass);

                            if (handlers != null)
                                handlers.callEvent(event, provider);
                        }
                    }
                }
            }
        }

        @NotNull
        private EventEntityProvider[] getBake() {
            EventEntityProvider[] bake = this.bake;

            if (bake != null)
                return bake;

            return this.bake = providers.toArray(new EventEntityProvider[0]);
        }

        private void addProvider(@NotNull final EventEntityProvider provider) {
            this.providers.add(provider);
            this.bake = null;
        }
    }

}
