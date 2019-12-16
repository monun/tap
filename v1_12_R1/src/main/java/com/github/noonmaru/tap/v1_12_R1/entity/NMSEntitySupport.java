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

package com.github.noonmaru.tap.v1_12_R1.entity;

import com.github.noonmaru.tap.entity.TapEntity;
import com.github.noonmaru.tap.entity.TapEntitySupport;
import com.google.common.collect.MapMaker;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class NMSEntitySupport implements TapEntitySupport
{

    private static NMSEntitySupport instance;

    private final Map<Class<?>, Function<Entity, ? extends NMSEntity>> wrappers = new HashMap<>();

    private final Map<Entity, NMSEntity> entities = new MapMaker().weakValues().makeMap();

    public NMSEntitySupport()
    {
        if (instance != null)
            throw new IllegalStateException();

        registerWrapper(Entity.class, NMSEntity::new);
        registerWrapper(EntityLiving.class, NMSLivingEntity::new);
        registerWrapper(EntityArmorStand.class, NMSArmorStand::new);
        registerWrapper(EntityPlayer.class, NMSPlayer::new);
    }

    public static NMSEntitySupport getInstance()
    {
        return instance;
    }

    private void registerWrapper(Class<? extends Entity> entityClass, Function<Entity, ? extends NMSEntity> wrapper)
    {
        wrappers.put(entityClass, wrapper);
    }

    private Function<Entity, ? extends NMSEntity> getWrapper(Class<?> clazz)
    {
        Function<Entity, ? extends NMSEntity> wrapper;

        do
        {
            wrapper = wrappers.get(clazz);

            if (wrapper != null)
                break;

            clazz = clazz.getSuperclass();
        }
        while (Entity.class.isAssignableFrom(clazz));

        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public <T extends TapEntity> T wrapEntity(Entity entity)
    {
        if (entity == null)
            throw new NullPointerException("Entity cannot be null");

        NMSEntity nmsEntity = entities.get(entity);

        if (nmsEntity == null)
            entities.put(entity, nmsEntity = getWrapper(entity.getClass()).apply(entity));

        return (T) nmsEntity;
    }

    @Override
    public <T extends TapEntity> T wrapEntity(org.bukkit.entity.Entity entity)
    {
        return wrapEntity(((CraftEntity) entity).getHandle());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TapEntity> T createEntity(Class<? extends org.bukkit.entity.Entity> entityClass)
    {
        Class<? extends Entity> nmsEntityClass = NMSEntityTypes.getEntityClass(entityClass);

        if (nmsEntityClass != null)
            return wrapEntity(EntityTypes.a(nmsEntityClass, ((CraftServer) Bukkit.getServer()).getServer().getWorld()));

        throw new NullPointerException();
    }

}
