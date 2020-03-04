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

import org.bukkit.event.EventPriority;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nemo
 */
public class HandlerStatement {

    private final Class<?> eventClass;

    private final Class<?> registrationClass;

    private final EventEntityProvider provider;

    private final EventPriority priority;

    private final boolean ignoreCancelled;

    private final EventExecutor executor;

    public HandlerStatement(@NotNull final Class<?> eventClass, @NotNull final Class<?> registrationClass, @NotNull final EventEntityProvider provider, @NotNull final EventPriority priority, final boolean ignoreCancelled, @NotNull final EventExecutor executor) {
        this.eventClass = eventClass;
        this.registrationClass = registrationClass;
        this.provider = provider;
        this.priority = priority;
        this.executor = executor;
        this.ignoreCancelled = ignoreCancelled;
    }

    @NotNull
    public Class<?> getEventClass() {
        return eventClass;
    }

    @NotNull
    public Class<?> getRegistrationClass() {
        return registrationClass;
    }

    @NotNull
    public EventEntityProvider getProvider() {
        return provider;
    }

    @NotNull
    public EventPriority getPriority() {
        return priority;
    }

    @NotNull
    public EventExecutor getExecutor() {
        return executor;
    }

    public boolean isIgnoreCancelled() {
        return ignoreCancelled;
    }
}
