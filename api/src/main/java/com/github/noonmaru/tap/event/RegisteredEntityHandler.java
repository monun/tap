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

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * @author Nemo
 */
public final class RegisteredEntityHandler implements Comparable<RegisteredEntityHandler> {

    private final long generatedTime = System.currentTimeMillis();

    private final HandlerStatement statement;

    private final Listener listener;

    private boolean valid;

    public RegisteredEntityHandler(@NotNull final HandlerStatement statement, @NotNull final Listener listener) {
        this.statement = statement;
        this.listener = listener;
        this.valid = true;
    }

    @NotNull
    public HandlerStatement getStatement() {
        return statement;
    }

    @NotNull
    public Listener getListener() {
        return listener;
    }

    @Override
    public int compareTo(@NotNull final RegisteredEntityHandler o) {
        int comp = statement.getPriority().compareTo(o.getStatement().getPriority());

        if (comp != 0)
            return comp;

        return Long.compare(generatedTime, o.generatedTime);
    }

    public void callEvent(@NotNull final Event event) {
        if (statement.isIgnoreCancelled() && event instanceof Cancellable && ((Cancellable) event).isCancelled())
            return;

        try {
            statement.getExecutor().execute(listener, event);
        } catch (EventException e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }
}
