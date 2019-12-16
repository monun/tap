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

package com.github.noonmaru.tap.event.entity.impl;

import com.github.noonmaru.collections.mut.CleanableWeakHashMap;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.event.ASMEventExecutor;
import com.github.noonmaru.tap.event.entity.*;
import com.google.common.reflect.TypeToken;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link EntityEventManager}의 구현체입니다.
 * 지정된 {@link Entity}에게 {@link EntityListener}를 등록 할 수 있습니다.
 *
 * @author Nemo
 */
public final class EntityEventManagerImpl implements EntityEventManager
{

    private final Plugin plugin;

    private final EventPriority priority;

    private final HashMap<Class<?>, EventEntityProvider> customProviders = new HashMap<>();

    private final HashMap<Class<?>, ListenerStatement> statements = new HashMap<>();

    private final HashMap<Class<?>, EventListener> listeners = new HashMap<>();

    private final CleanableWeakHashMap<Entity, EventEntity> entities = new CleanableWeakHashMap<>(Tap.DEBUG ? eventEntity -> {
        System.out.println("Clean up: " + eventEntity);
    } : EventEntity::unregisterAll);

    private final EventExecutor EVENT_EXECUTOR = (listener, event) -> ((EventListener) listener).onEvent(event);

    private final UnregisterListener unregisterListener;

    private boolean valid;

    public EntityEventManagerImpl(Plugin plugin, EventPriority priority)
    {
        this.plugin = plugin;
        this.priority = priority;
        this.unregisterListener = new UnregisterListener(this);
        ASMEventExecutor.registerEvents(this.unregisterListener, plugin);
        this.valid = true;
    }

    @Override
    public RegisteredEntityListener registerEvents(Entity entity, EntityListener listener)
    {
        checkState();

        EventEntity eventEntity = entities.computeIfAbsent(entity, target -> new EventEntity());
        RegisteredEntityListenerImpl registeredEntityListener = createRegisteredEntityListener(listener);
        eventEntity.register(registeredEntityListener);

        return registeredEntityListener;
    }

    private RegisteredEntityListenerImpl createRegisteredEntityListener(EntityListener listener)
    {
        ListenerStatement statement = getOrRegisterListenerStatement(listener.getClass());
        return new RegisteredEntityListenerImpl(statement, listener);
    }

    private ListenerStatement getOrRegisterListenerStatement(Class<?> listenerClass)
    {
        return statements.computeIfAbsent(listenerClass, clazz -> {
            int mod = listenerClass.getModifiers();

            if (!Modifier.isPublic(mod))
                throw new IllegalArgumentException("EntityListener modifier must be public");

            ArrayList<HandlerStatement> handlerStatements = new ArrayList<>();
            Method[] methods = listenerClass.getMethods();
            Set<? extends Class<?>> supers = TypeToken.of(listenerClass).getTypes().rawTypes();

            for (Method method : methods)
            {
                for (Class<?> superClass : supers)
                {
                    if (!EntityListener.class.isAssignableFrom(superClass))
                        break;

                    try
                    {
                        Method real = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());

                        if (real.isAnnotationPresent(EntityHandler.class))
                        {
                            try
                            {
                                handlerStatements.add(createHandlerStatement(method));
                            }
                            catch (Exception e)
                            {
                                throw new IllegalArgumentException("Failed to create HandlerStatement for " + real);
                            }
                            break;
                        }
                    }
                    catch (NoSuchMethodException | SecurityException ignored)
                    {
                    }
                }
            }

            ListenerStatement statement = new ListenerStatement(listenerClass, handlerStatements.toArray(new HandlerStatement[0]));

            for (HandlerStatement handlerStatement : statement.getHandlerStatements())
            {
                registerEvent(handlerStatement);
            }

            return statement;
        });
    }

    private void registerEvent(HandlerStatement statement)
    {
        Class<?> registrationClass = statement.getRegistrationClass();

        EventListener listener = listeners.computeIfAbsent(registrationClass, clazz -> {
            EventListener newListener = new EventListener();
            plugin.getServer().getPluginManager().registerEvent(clazz.asSubclass(Event.class), newListener, priority, EVENT_EXECUTOR, plugin, false);

            return newListener;
        });

        listener.addProvider(statement.getProvider());
    }

    private HandlerStatement createHandlerStatement(Method method)
    {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1)
            throw new IllegalArgumentException("EntityHandler methods must require a single argument: " + method);

        if (method.getReturnType() != void.class)
            throw new IllegalArgumentException("EntityHandler methods must return 'void': " + method);

        Class<?> eventClass = parameterTypes[0];

        if (!Event.class.isAssignableFrom(eventClass))
            throw new IllegalArgumentException("'" + eventClass.getName() + "' is not event class : " + method);

        EntityHandler handler = method.getAnnotation(EntityHandler.class);
        Class<?> registrationClass = EventTools.getRegistrationClass(eventClass);
        Class<?> providerClass = handler.provider();
        EventEntityProvider provider = providerClass == DefaultProvider.class ? EventTools.findDefaultProvider(eventClass) : createEntityProvider(providerClass);
        HandlerExecutor executor = ASMHandlerExecutor.create(method);

        return new HandlerStatement(eventClass, registrationClass, provider, handler.priority(), handler.ignoreCancelled(), executor);
    }

    private EventEntityProvider createEntityProvider(Class<?> providerClass)
    {
        return customProviders.computeIfAbsent(providerClass, clazz -> {
            try
            {
                return new EventEntityProvider((EntityProvider) clazz.newInstance());
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(e);
            }
        });
    }

    @Override
    public void unregister(Entity entity)
    {
        checkState();

        EventEntity eventEntity = entities.remove(entity);

        if (eventEntity != null)
            eventEntity.unregisterAll();
    }

    @Override
    public void unregisterAll()
    {
        checkState();

        for (EventEntity eventEntity : entities.values())
        {
            eventEntity.unregisterAll();
        }

        entities.clear();
    }

    @Override
    public void destroy()
    {
        checkState();

        unregisterAll();

        HandlerList.unregisterAll(this.unregisterListener);

        for (EventListener listener : listeners.values())
        {
            HandlerList.unregisterAll(listener);
        }

        listeners.clear();
        statements.clear();
        customProviders.clear();

        valid = false;
    }

    private void checkState()
    {
        if (!valid)
            throw new IllegalStateException("Invalid " + getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)));
    }

    @Override
    public boolean isValid()
    {
        return valid;
    }

    /**
     * {@link org.bukkit.event.entity.EntityEvent} 에게 이벤트를 전달하기 위한 {@link Listener}
     */
    private class EventListener implements Listener
    {
        private final LinkedHashSet<EventEntityProvider> providers = new LinkedHashSet<>();

        private EventEntityProvider[] bake;

        @SuppressWarnings("unchecked")
        void onEvent(Event event)
        {
            for (EventEntityProvider provider : getBake())
            {
                Class<?> eventClass = event.getClass();

                if (provider.getEventClass().isAssignableFrom(eventClass))
                {
                    Entity entity = provider.getProvider().getFrom(event);

                    if (entity != null)
                    {
                        EventEntity eventEntity = entities.get(entity);

                        if (eventEntity != null)
                        {
                            Class<?> regClass = EventTools.getRegistrationClass(eventClass);
                            EntityHandlerList handlers = eventEntity.getHandlers(regClass);
                            handlers.callEvent(event, provider, eventClass, entity);
                        }
                    }
                }
            }
        }

        private EventEntityProvider[] getBake()
        {
            EventEntityProvider[] bake = this.bake;

            if (bake != null)
                return bake;

            return this.bake = providers.toArray(new EventEntityProvider[0]);
        }

        private void addProvider(EventEntityProvider provider)
        {
            this.providers.add(provider);
            this.bake = null;
        }
    }

}
