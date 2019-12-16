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

import com.github.noonmaru.tap.event.entity.EntityHandler;
import com.github.noonmaru.tap.event.entity.EntityListener;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

/**
 * {@link EntityHandler} 메서드의 정보를 관리하는 클래스입니다.
 *
 * @author Nemo
 */
final class HandlerStatement
{

    private final Class<?> eventClass;

    private final Class<?> registrationClass;

    private final EventEntityProvider provider;

    private final EventPriority priority;

    private final boolean ignoreCancelled;

    private final HandlerExecutor executor;

    HandlerStatement(Class<?> eventClass, Class<?> registrationClass, EventEntityProvider provider, EventPriority priority, boolean ignoreCancelled, HandlerExecutor executor)
    {
        this.eventClass = eventClass;
        this.registrationClass = registrationClass;
        this.provider = provider;
        this.priority = priority;
        this.ignoreCancelled = ignoreCancelled;
        this.executor = executor;
    }

    public Class<?> getRegistrationClass()
    {
        return registrationClass;
    }

    public Class<?> getEventClass()
    {
        return eventClass;
    }

    public EventEntityProvider getProvider()
    {
        return provider;
    }

    public EventPriority getPriority()
    {
        return priority;
    }

    public boolean isIgnoreCancelled()
    {
        return ignoreCancelled;
    }

    public HandlerExecutor getExecutor()
    {
        return executor;
    }

    /**
     * 이벤트를 호출합니다.
     *
     * @param entity
     * @param provider
     * @param event
     * @param listener
     */
    void callEvent(Entity entity, EventEntityProvider provider, Event event, EntityListener listener)
    {
        if (provider != this.provider || (ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()))
            return;

        executor.execute(listener, event);
    }

}