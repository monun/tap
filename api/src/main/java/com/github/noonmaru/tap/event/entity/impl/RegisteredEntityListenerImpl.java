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

import com.github.noonmaru.collections.Node;
import com.github.noonmaru.tap.event.entity.EntityListener;
import com.github.noonmaru.tap.event.entity.RegisteredEntityListener;

/**
 * {@link RegisteredEntityListener}의 구현체입니다.
 *
 * @author Nemo
 */
final class RegisteredEntityListenerImpl implements RegisteredEntityListener
{

    private final EntityListener listener;

    private final RegisteredEntityHandler[] handlers;

    Node<RegisteredEntityListenerImpl> node;

    RegisteredEntityListenerImpl(ListenerStatement statement, EntityListener listener)
    {
        this.listener = listener;

        HandlerStatement[] handlerStatements = statement.getHandlerStatements();
        RegisteredEntityHandler[] entityHandlers = new RegisteredEntityHandler[handlerStatements.length];

        for (int i = 0, length = handlerStatements.length; i < length; i++)
        {
            entityHandlers[i] = new RegisteredEntityHandler(listener, handlerStatements[i]);
        }

        this.handlers = entityHandlers;
    }

    @Override
    public EntityListener getListener()
    {
        return listener;
    }

    RegisteredEntityHandler[] getHandlers()
    {
        return handlers;
    }

    @Override
    public void unregister()
    {
        node.clear();
        node = null;

        for (RegisteredEntityHandler handler : handlers)
        {
            handler.unregister();
        }
    }

}
