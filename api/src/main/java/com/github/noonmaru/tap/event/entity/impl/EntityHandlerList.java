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
import com.github.noonmaru.collections.mut.LinkedNodeList;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

/**
 * @author Nemo
 */
final class EntityHandlerList
{
    private final LinkedNodeList<RegisteredEntityHandler> handlers = new LinkedNodeList<>();

    void register(RegisteredEntityHandler handler)
    {
        LinkedNodeList<RegisteredEntityHandler> handlers = this.handlers;

        if (handlers.size() > 0)
        {
            EventPriority priority = handler.getStatement().getPriority();

            Node<RegisteredEntityHandler> node = handlers.getFirstNode();

            do
            {
                if (priority.compareTo(node.getItem().getStatement().getPriority()) < 0)
                {
                    handler.node = node.linkBefore(handler);
                    return;
                }
            }
            while ((node = node.next()) != null);
        }

        handler.node = handlers.addNode(handler);
    }

    void callEvent(Event event, EventEntityProvider provider, Class<?> eventClass, Entity entity)
    {
        if (handlers.isEmpty())
            return;

        Node<RegisteredEntityHandler> node = handlers.getFirstNode();

        do
        {
            RegisteredEntityHandler handler = node.getItem();
            HandlerStatement statement = handler.getStatement();

            if (statement.getProvider() == provider && statement.getEventClass().isAssignableFrom(eventClass))
            {
                statement.getExecutor().execute(handler.getListener(), event);
            }
        }
        while ((node = node.next()) != null);
    }

    void clear()
    {
        this.handlers.clear();
    }
}
