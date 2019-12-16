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

import com.github.noonmaru.tap.event.entity.EntityListener;

/**
 * {@link EntityListener}의 정보를 관리하는 클래스입니다.
 *
 * @author Nemo
 */
final class ListenerStatement
{

    private final Class<?> listenerClass;

    private final HandlerStatement[] statements;

    public ListenerStatement(Class<?> listenerClass, HandlerStatement[] statements)
    {
        this.listenerClass = listenerClass;
        this.statements = statements;
    }

    public Class<?> getListenerClass()
    {
        return listenerClass;
    }

    public HandlerStatement[] getHandlerStatements()
    {
        return statements;
    }

}
