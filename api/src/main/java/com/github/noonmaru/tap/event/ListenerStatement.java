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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Nemo
 */
public final class ListenerStatement {

    private static final HashMap<Class<?>, ListenerStatement> STATEMENTS = new HashMap<>();
    private final Class<?> listenerClass;
    private final ImmutableList<HandlerStatement> handlerSta;


    public ListenerStatement(Class<?> listenerClass, ArrayList<HandlerStatement> handlerStatements) {
        this.listenerClass = listenerClass;
        this.handlerSta = ImmutableList.copyOf(handlerStatements);
    }

    @NotNull
    static ListenerStatement getOrCreate(@NotNull final Class<?> listenerClass) {
        return STATEMENTS.computeIfAbsent(listenerClass, clazz -> {
            int mod = listenerClass.getModifiers();

            if (!Modifier.isPublic(mod))
                throw new IllegalArgumentException("EntityListener modifier must be public");

            ArrayList<HandlerStatement> handlerStatements = new ArrayList<>();
            Method[] methods = listenerClass.getMethods();
            @SuppressWarnings("UnstableApiUsage") Set<? extends Class<?>> supers = TypeToken.of(listenerClass).getTypes().rawTypes();

            for (Method method : methods) {
                for (Class<?> superClass : supers) {
                    if (!Listener.class.isAssignableFrom(superClass))
                        break;

                    try {
                        Method real = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());

                        if (real.isAnnotationPresent(EventHandler.class)) {
                            try {
                                handlerStatements.add(createHandlerStatement(method));
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Failed to create HandlerStatement for " + real);
                            }
                            break;
                        }
                    } catch (NoSuchMethodException | SecurityException ignored) {
                    }
                }
            }

            return new ListenerStatement(listenerClass, handlerStatements);
        });
    }

    @NotNull
    private static HandlerStatement createHandlerStatement(@NotNull final Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1)
            throw new IllegalArgumentException("EntityHandler methods must require a single argument: " + method);

        Class<?> eventClass = parameterTypes[0];

        if (!Event.class.isAssignableFrom(eventClass))
            throw new IllegalArgumentException("'" + eventClass.getName() + "' is not event class : " + method);

        EventHandler handler = method.getAnnotation(EventHandler.class);
        Class<?> registrationClass = EventTools.getRegistrationClass(eventClass);

        TargetEntity targetEntity = method.getAnnotation(TargetEntity.class);
        EventEntityProvider provider = targetEntity == null ? EventTools.findDefaultProvider(eventClass) : EventTools.findDefaultProvider(eventClass);

        EventExecutor executor = EventExecutor.create(method, eventClass.asSubclass(Event.class));

        return new HandlerStatement(eventClass, registrationClass, provider, handler.priority(), handler.ignoreCancelled(), executor);
    }

    @NotNull
    public Class<?> getListenerClass() {
        return listenerClass;
    }

    @NotNull
    public ImmutableList<HandlerStatement> getHandlerStatements() {
        return handlerSta;
    }
}
