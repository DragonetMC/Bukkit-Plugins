package org.dragonet.bukkit.ultimateroles.ingameshop;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

/**
 * Created on 2017/11/13.
 */
public final class Lang {

    public static YamlConfiguration lang;

    public static String build(String path, Object... args) {
        return String.format(lang.getString(path), args);
    }

    public static List<String> getStringList(String path) {
        return lang.getStringList(path);
    }

    public static void sendMessage(CommandSender sender, String path, Object... args) {
        sender.sendMessage(lang.getString("chat-prefix") + build(path, args));
    }

    public static void sendMessageList(CommandSender sender, String path) {
        sender.sendMessage("\u00a75====== " + lang.getString("chat-prefix") + " \u00a75======");
        getStringList(path).forEach(sender::sendMessage);
    }

}
