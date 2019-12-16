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

package com.github.noonmaru.tap.init;

import com.github.noonmaru.tools.gson.config.ConfigAdapter;
import com.github.noonmaru.tools.gson.config.ConfigUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class MCConfigAdapters
{
    private MCConfigAdapters()
    {}

    static void init()
    {
        ConfigUtils.registerAdapter(World.class, new WorldAdapter());
        ConfigUtils.registerAdapter(Location.class, new LocationAdapter());
        ConfigUtils.registerAdapter(Vector.class, new VectorAdapter());
    }

    private static class WorldAdapter extends ConfigAdapter<World>
    {
        @Override
        public World fromJson(JsonElement json)
        {
            return Bukkit.getWorld(json.getAsString());
        }

        @Override
        public JsonElement toJson(World o)
        {
            return new JsonPrimitive(o.getName());
        }
    }

    private static class LocationAdapter extends ConfigAdapter<Location>
    {
        @Override
        public Location fromJson(JsonElement json)
        {
            JsonObject object = json.getAsJsonObject();

            World world = Bukkit.getWorld(object.get("world").getAsString());
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double z = object.get("z").getAsDouble();

            JsonElement yawJson = object.get("yaw");
            JsonElement pitchJson = object.get("pitch");

            float yaw = yawJson == null ? 0.0F : yawJson.getAsFloat();
            float pitch = pitchJson == null ? 0.0F : pitchJson.getAsFloat();

            return new Location(world, x, y, z, yaw, pitch);
        }

        @Override
        public JsonElement toJson(Location o)
        {
            JsonObject json = new JsonObject();

            json.addProperty("world", o.getWorld().getName());
            json.addProperty("x", o.getX());
            json.addProperty("y", o.getY());
            json.addProperty("z", o.getZ());
            json.addProperty("yaw", o.getYaw());
            json.addProperty("pitch", o.getPitch());

            return json;
        }
    }

    private static class VectorAdapter extends ConfigAdapter<Vector>
    {
        @Override
        public Vector fromJson(JsonElement json)
        {
            JsonObject object = json.getAsJsonObject();
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double z = object.get("z").getAsDouble();

            return new Vector(x, y, z);
        }

        @Override
        public JsonElement toJson(Vector o)
        {
            JsonObject json = new JsonObject();
            json.addProperty("x", o.getX());
            json.addProperty("y", o.getY());
            json.addProperty("z", o.getZ());

            return json;
        }
    }
}
