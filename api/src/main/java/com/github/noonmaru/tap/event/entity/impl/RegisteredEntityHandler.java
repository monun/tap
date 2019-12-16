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
import com.github.noonmaru.tap.event.entity.EntityHandler;
import com.github.noonmaru.tap.event.entity.EntityListener;
import com.github.noonmaru.tap.event.entity.RegisteredEntityListener;

/**
 * {@link EventEntity}에 {@link EntityHandler}를 등록하기 위한 클래스입니다.
 *
 * @author Nemo
 */
final class RegisteredEntityHandler
{

    private final EntityListener listener;

    private final HandlerStatement statement;

    Node<RegisteredEntityHandler> node;

    RegisteredEntityHandler(EntityListener listener, HandlerStatement statement)
    {
        this.listener = listener;
        this.statement = statement;
    }

    public EntityListener getListener()
    {
        return listener;
    }

    public HandlerStatement getStatement()
    {
        return statement;
    }

    /**
     * 핸들러의 노드 연결을 해제합니다.
     * {@link org.bukkit.entity.Entity}가 메모리에서 해제되거나 더 이상 유효하지 않을때 호출됩니다.
     */
    void clear()
    {
        node.clear();
        node = null;
    }

    /**
     * 핸들러의 노드 연결을 약하게 해제합니다.
     * 외부에서 {@link RegisteredEntityListener}를 등록 해제 할 때 호출됩니다.
     */
    void unregister()
    {
        node.unlink();
        node = null;
    }

}
