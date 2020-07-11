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

import org.bukkit.event.Event;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class EventTools {

    private static final Map<Class<?>, Class<?>> REG_CLASSES = new HashMap<>();

    private static final Map<Class<?>, EventEntityProvider> CUSTOM_PROVIDERS = new HashMap<>();

    private static final EventEntityProvider[] DEFAULT_PROVIDERS;

    static {
        // 기본 개체 제공자 초기화
        Class<?>[] classes = DefaultProvider.class.getDeclaredClasses();
        List<EventEntityProvider> defaultProviders = new ArrayList<>(classes.length);

        for (Class<?> clazz : classes) {
            if (EntityProvider.class.isAssignableFrom(clazz)) {
                try {
                    defaultProviders.add(new EventEntityProvider((EntityProvider) clazz.newInstance()));
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }

        DEFAULT_PROVIDERS = defaultProviders.toArray(new EventEntityProvider[0]);
    }

    /**
     * {@link Event}를 상속한 클래스들 중 {@link org.bukkit.event.HandlerList}가 있는 클래스를 찾아서 반환합니다.
     */
    @NotNull
    static Class<?> getRegistrationClass(@NotNull final Class<?> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    /**
     * {@link DefaultProvider}에서 호환 가능한 제공자를 반환합니다.
     *
     * @param eventClass 찾아낼 클래스
     * @return 호환되는 엔티티 제공자
     * @see DefaultProvider
     */
    @NotNull
    static EventEntityProvider findDefaultProvider(@NotNull final Class<?> eventClass) {
        for (EventEntityProvider provider : DEFAULT_PROVIDERS) {
            if (provider.getEventClass().isAssignableFrom(eventClass))
                return provider;
        }

        throw new IllegalArgumentException("Not found DefaultProvider for " + eventClass);
    }

    @NotNull
    static EventEntityProvider getOrCreateCustomProvide(@NotNull final Class<?> providerClass) {
        return CUSTOM_PROVIDERS.computeIfAbsent(providerClass, clazz -> {
            try {
                return new EventEntityProvider(clazz.asSubclass(EntityProvider.class).newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        });
    }

    @NotNull
    static Class<?> getGenericEventType(@NotNull Class<?> providerClass) {
        String prefix = EntityProvider.class.getName() + "<"; //제너릭 타임 이름은 ClassName<Type>으로 반환됨
        Type[] genericInterfaces = providerClass.getGenericInterfaces();

        do {
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface.getTypeName().startsWith(prefix)) {
                    return (Class<?>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
                }
            }
        }
        while ((providerClass = providerClass.getSuperclass()) != Object.class);

        throw new IllegalArgumentException(providerClass + " is not EntityProvider");
    }
}
