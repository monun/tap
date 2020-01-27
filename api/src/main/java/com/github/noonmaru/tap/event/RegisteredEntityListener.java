/*
 * Copyright (c) 2020 Noonmaru
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

package com.github.noonmaru.tap.event;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.Listener;

import java.util.ArrayList;

/**
 * @author Nemo
 */
public final class RegisteredEntityListener {

    private final EventEntity eventEntity;

    private final ListenerStatement statement;

    private final Listener listener;

    private final ImmutableList<RegisteredEntityHandler> handlers;

    public RegisteredEntityListener(EventEntity eventEntity, ListenerStatement statement, Listener listener) {
        this.eventEntity = eventEntity;
        this.statement = statement;
        this.listener = listener;

        ImmutableList<HandlerStatement> handlerStatements = statement.getHandlerStatements();
        ArrayList<RegisteredEntityHandler> handlers = new ArrayList<>(handlerStatements.size());

        for (HandlerStatement handlerStatement : handlerStatements) {
            handlers.add(new RegisteredEntityHandler(handlerStatement, listener));
        }

        this.handlers = ImmutableList.copyOf(handlers);
    }

    public Listener getListener() {
        return listener;
    }

    public ImmutableList<RegisteredEntityHandler> getHandlers() {
        return handlers;
    }

    public void unregister() {
        eventEntity.unregister(this);
    }
}
