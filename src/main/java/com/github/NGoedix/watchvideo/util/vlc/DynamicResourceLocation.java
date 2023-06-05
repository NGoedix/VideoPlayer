/*
 * This file is part of the FancyVideo-API.
 *
 * The FancyVideo-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The FancyVideo-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * The FancyVideo-API uses VLCJ, Copyright 2009-2021 Caprica Software Limited,
 * licensed under the GNU General Public License.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You should have received a copy of the GNU General Public License
 * along with FancyVideo-API.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2022 Nick1st.
 */

package com.github.NGoedix.watchvideo.util.vlc; //NOSONAR

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * ResourceLocation implementation for easier pre- and postfix handling.
 *
 * @since 0.2.0.0
 */
public class DynamicResourceLocation extends ResourceLocation {

    private final String playerNamespace;
    private final String playerPath;

    /**
     * This will construct a ResourceLocation that takes care about the name pre- and postfix minecraft adds to dynamic textures.
     *
     * @param namespace Should be your modid
     * @param path      Should be a unique identifier for this player
     * @since 0.2.0.0
     */
    public DynamicResourceLocation(@ParametersAreNonnullByDefault String namespace, @ParametersAreNonnullByDefault String path) {
        super("minecraft:dynamic/" + namespace + "." + path + "_1");
        playerNamespace = namespace;
        playerPath = path;
    }

    /**
     * See {@link #toWorkingString()} to get the internal name
     *
     * @return The true ResourceLocation, containing pre- and postfix.
     * @since 0.2.0.0
     */
    @Override
    public @Nonnull String toString() {
        return super.toString();
    }

    /**
     * See {@link #toString()} to get the name containing pre- and postfix
     *
     * @return The FancyVideo-API ResourceLocation, <b>NOT</b> containing pre- and postfix.
     * @since 0.2.0.0
     */
    public @Nonnull String toWorkingString() {
        return playerNamespace + ":" + playerPath;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
