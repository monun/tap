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


import com.github.noonmaru.collections.mut.EventNodeList;
import com.github.noonmaru.tap.event.entity.EntityListener;

import java.util.HashMap;

/**
 * {@link EntityListener}가 등록되는 클래스입니다.
 *
 * @author Nemo
 */
final class EventEntity
{

    private final EventNodeList<RegisteredEntityListenerImpl> listeners = new EventNodeList<>();

    private final HashMap<Class<?>, EntityHandlerList> slots = new HashMap<>();

    void register(RegisteredEntityListenerImpl listener)
    {
        listener.node = listeners.addNode(listener);

        for (RegisteredEntityHandler handler : listener.getHandlers())
        {
            HandlerStatement statement = handler.getStatement();
            EntityHandlerList handlers = slots.compute(statement.getRegistrationClass(), (handlerClass, handlerList) -> new EntityHandlerList());
            handlers.register(handler);
        }
    }

    /**
     * 등록된 모든 리스너를 해제합니다.
     */
    void unregisterAll()
    {
        listeners.clear();

        for (EntityHandlerList handlerList : slots.values())
        {
            handlerList.clear();
        }

        slots.clear();
    }

    EntityHandlerList getHandlers(Class<?> handlerClass)
    {
        return slots.get(handlerClass);
    }

}
