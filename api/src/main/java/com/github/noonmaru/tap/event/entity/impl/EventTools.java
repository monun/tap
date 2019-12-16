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

import com.github.noonmaru.tap.event.entity.EntityEventManager;
import com.github.noonmaru.tap.event.entity.EntityListener;
import com.github.noonmaru.tap.event.entity.EntityProvider;
import org.bukkit.event.Event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link EntityEventManager}에 {@link EntityListener}를 등록하기 위해서 사용하는 도구 모음 클래스입니다.
 *
 * @author Nemo
 */
final class EventTools
{
    private static final Map<Class<?>, Class<?>> REG_CLASSES = new HashMap<>();

    private static final EventEntityProvider[] DEFAULT_PROVIDERS;

    static
    {
        // 기본 개체 제공자 초기화
        Class<?>[] classes = DefaultProvider.class.getDeclaredClasses();
        List<EventEntityProvider> defaultProviders = new ArrayList<>(classes.length);

        for (Class<?> clazz : classes)
        {
            if (EntityProvider.class.isAssignableFrom(clazz))
            {
                try
                {
                    defaultProviders.add(new EventEntityProvider((EntityProvider) clazz.newInstance()));
                }
                catch (Exception e)
                {
                    throw new AssertionError(e);
                }
            }
        }

        DEFAULT_PROVIDERS = defaultProviders.toArray(new EventEntityProvider[0]);
    }

    /**
     * {@link Event}를 상속한 클래스들 중 {@link org.bukkit.event.HandlerList}가 있는 클래스를 찾아서 반환합니다.
     *
     * @param eventClass 찾아낼 클래스
     * @return {@link org.bukkit.event.HandlerList}가 있는 클래스
     */
    static Class<?> getRegistrationClass(Class<?> eventClass)
    {
        Class<?> handlerClass = REG_CLASSES.get(eventClass);

        if (handlerClass == null)
        {
            try
            {
                eventClass.getDeclaredMethod("getHandlerList");
                REG_CLASSES.put(eventClass, handlerClass = eventClass);
            }
            catch (NoSuchMethodException e)
            {
                Class<?> superClass = eventClass.getSuperclass();

                if (superClass != null && !superClass.equals(Event.class) && Event.class.isAssignableFrom(eventClass.getSuperclass()))
                {
                    handlerClass = getRegistrationClass(eventClass.getSuperclass().asSubclass(Event.class));
                    REG_CLASSES.put(eventClass, handlerClass);
                }
                else
                {
                    throw new IllegalArgumentException("Unable to find handler list for event " + eventClass.getName() + ". Static getHandlerList method required!");
                }
            }
        }

        return handlerClass;
    }

    /**
     * {@link DefaultProvider}에서 호환 가능한 제공자를 반환합니다.
     *
     * @param eventClass 찾아낼 클래스
     * @return 호환되는 엔티티 제공자
     * @see DefaultProvider
     */
    static EventEntityProvider findDefaultProvider(Class<?> eventClass)
    {
        for (EventEntityProvider provider : DEFAULT_PROVIDERS)
        {
            if (provider.getEventClass().isAssignableFrom(eventClass))
                return provider;
        }

        throw new IllegalArgumentException("Not found DefaultProvider for " + eventClass);
    }

    static Class<?> getGenericEventType(Class<?> providerClass)
    {
        String prefix = EntityProvider.class.getName() + "<"; //제너릭 타임 이름은 ClassName<Type>으로 반환됨
        Type[] genericInterfaces = providerClass.getGenericInterfaces();

        do
        {
            for (Type genericInterface : genericInterfaces)
            {
                if (genericInterface.getTypeName().startsWith(prefix))
                {
                    return (Class<?>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
                }
            }
        }
        while ((providerClass = providerClass.getSuperclass()) != Object.class);

        throw new IllegalArgumentException(providerClass + " is not EntityProvider");
    }
}
