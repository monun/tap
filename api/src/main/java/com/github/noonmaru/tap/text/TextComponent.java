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

package com.github.noonmaru.tap.text;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.item.TapItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.Validate;

import java.util.regex.Pattern;

public abstract class TextComponent
{

    public static Builder builder()
    {
        return new BuilderImpl();
    }

    public enum Color
    {
        BLACK("black"),
        DARK_BLUE("dark_blue"),
        DARK_GREEN("dark_green"),
        DARK_AQUA("dark_aqua"),
        DARK_RED("dark_red"),
        DARK_PURPLE("dark_purple"),
        GOLD("gold"),
        GRAY("gray"),
        DARK_GRAY("dark_gray"),
        BLUE("blue"),
        GREEN("green"),
        AQUA("aqua"),
        RED("red"),
        LIGHT_PURPLE("light_purple"),
        YELLOW("yellow"),
        WHITE("white");

        final String value;

        Color(String value)
        {
            this.value = value;
        }
    }

    public enum Style
    {
        BOLD("bold"),
        ITALIC("italic"),
        UNDERLINED("underlined"),
        STRIKE_THROUGH("strikethrough"),
        OBFUSCATED("obfuscated");

        final String key;

        Style(String key)
        {
            this.key = key;
        }
    }

    public interface Builder
    {

        Builder next();

        Input input();

        Builder color(Color color);

        Builder style(Style style);

        Builder styles(Style... styles);

        Builder insertion(String insertion);

        Click click();

        Hover hover();

        Builder extra();

        Builder extra(Builder extra);

        TextComponent build();

    }

    public interface Input
    {

        Builder text(String text);

        Builder translate(String conversion, String... replaces);

        Builder score(String name, String objective);

        Builder selector(String selector);

        Builder keybind(String keybind);

    }

    public interface Click
    {

        Builder openURL(String url);

        Builder runCommand(String command);

        Builder suggestCommand(String command);

        Builder changePage(int page);

    }

    public interface Hover
    {

        Builder showText(String text);

        Builder showItem(TapItemStack itemStack);

        Builder showEntity(String name, String type, String id);

    }

    private final static class BuilderImpl implements Builder, Input, Click, Hover
    {

        private static final Pattern SELECTOR_PATTERN = Pattern.compile("^@([pares])(?:\\[([^ ]*)\\])?$");

        private final JsonArray array = new JsonArray();

        private JsonObject json;

        BuilderImpl()
        {
            next();
        }

        private static boolean isSelector(String token)
        {
            return SELECTOR_PATTERN.matcher(token).matches();
        }

        public Builder next()
        {
            array.add(json = new JsonObject());

            return this;
        }

        public Input input()
        {
            return this;
        }

        @Override
        public Builder text(String text)
        {
            Validate.notNull(text, "Input cannot be null");

            json.addProperty("text", text);

            return this;
        }

        private JsonArray with()
        {
            JsonObject json = this.json;
            JsonArray with = (JsonArray) json.get("with");

            if (with == null)
                json.add("with", with = new JsonArray());

            return with;
        }

        @Override
        public Builder translate(String conversion, String... replaces)
        {
            Validate.notNull(conversion, "Conversion cannot be null");

            json.addProperty("translate", conversion);

            if (replaces.length > 0)
            {
                JsonArray with = with();

                for (String arg : replaces)
                {
                    if (isSelector(arg))
                    {
                        JsonObject selector = new JsonObject();
                        selector.addProperty("selector", arg);
                        with.add(selector);
                    }
                    else
                    {
                        with.add(arg);
                    }
                }
            }

            return this;
        }

        @Override
        public Builder score(String name, String objective)
        {
            Validate.notNull(name, "Name cannot be null");
            Validate.notNull(objective, "TapObjective cannot be null");

            JsonObject score = new JsonObject();
            score.addProperty("name", name);
            score.addProperty("objective", objective);
            json.add("score", score);

            return this;
        }

        @Override
        public Builder selector(String selector)
        {
            Validate.notNull(selector, "Selector cannot be null");

            json.addProperty("selector", selector);

            return this;
        }

        @Override
        public Builder keybind(String keybind)
        {
            Validate.notNull(keybind, "Key cannot be null");

            json.addProperty("keybind", keybind);

            return this;
        }

        @Override
        public Builder color(Color color)
        {
            json.addProperty("color", color.value);

            return this;
        }

        @Override
        public Builder style(Style style)
        {
            Validate.notNull(style, "Style cannot be null");

            json.addProperty(style.key, Boolean.TRUE);

            return this;
        }

        @Override
        public Builder styles(Style... styles)
        {
            JsonObject json = this.json;

            for (Style style : styles)
            {
                if (style != null)
                    json.addProperty(style.key, Boolean.TRUE);
            }

            return this;
        }

        @Override
        public Builder insertion(String insertion)
        {
            Validate.notNull(insertion, "Insertion cannot be null");

            json.addProperty("insertion", insertion);

            return this;
        }

        @Override
        public Click click()
        {
            return this;
        }

        private void clickEvent(String action, String value)
        {
            JsonObject clickEvent = new JsonObject();
            clickEvent.addProperty("action", action);
            clickEvent.addProperty("value", value);

            json.add("clickEvent", clickEvent);
        }

        @Override
        public Builder openURL(String url)
        {
            Validate.notNull(url, "URL cannot be null");

            clickEvent("open_url", url);

            return this;
        }

        @Override
        public Builder runCommand(String command)
        {
            Validate.notNull(command, "Command cannot be null");

            clickEvent("run_command", command);

            return this;
        }

        @Override
        public Builder suggestCommand(String command)
        {
            Validate.notNull(command, "Command cannot be null");

            clickEvent("suggest_command", command);

            return this;
        }

        @Override
        public Builder changePage(int page)
        {
            clickEvent("change_page", String.valueOf(page));

            return this;
        }

        @Override
        public Hover hover()
        {
            return this;
        }

        private void hoverEvent(String action, String value)
        {
            JsonObject hoverEvent = new JsonObject();
            hoverEvent.addProperty("action", action);
            hoverEvent.addProperty("value", value);

            json.add("hoverEvent", hoverEvent);
        }

        @Override
        public Builder showText(String text)
        {
            Validate.notNull(text, "Text cannot be null");

            hoverEvent("show_text", text);

            return this;
        }

        @Override
        public Builder showItem(TapItemStack itemStack)
        {
            Validate.notNull(itemStack, "ItemStack cannot be null");

            hoverEvent("show_item", itemStack.save().toJsonString());

            return this;
        }

        @Override
        public Builder showEntity(String name, String type, String id)
        {
            Validate.notNull(name, "Name cannot be null");

            JsonObject showEntity = new JsonObject();
            showEntity.addProperty("name", name);

            if (type != null)
                showEntity.addProperty("type", type);
            if (id != null)
                showEntity.addProperty("id", id);

            hoverEvent("show_entity", showEntity.toString());

            return this;
        }

        private void extra(BuilderImpl other)
        {
            json.add("extra", other.array);
        }

        @Override
        public Builder extra()
        {
            BuilderImpl builder = new BuilderImpl();
            extra(builder);

            return builder;
        }

        @Override
        public Builder extra(Builder extra)
        {
            Validate.notNull(extra, "Extra cannot be null");

            extra((BuilderImpl) extra);

            return this;
        }

        @Override
        public TextComponent build()
        {
            return Tap.TEXT.fromJsonLenient(toString());
        }

        @Override
        public String toString()
        {
            return array.toString();
        }
    }
}
