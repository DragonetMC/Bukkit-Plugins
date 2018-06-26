package org.dragonet.bukkit.legendguns.crafting;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Created on 2017/10/6.
 */
public enum Lang {
    NO_CRAFTING_PERMISSION,
    PERMISSION_NOTIFICATION;

    public static YamlConfiguration lang;

    public String build(Object... args) {
        return String.format(lang.getString(name()), args);
    }
}
