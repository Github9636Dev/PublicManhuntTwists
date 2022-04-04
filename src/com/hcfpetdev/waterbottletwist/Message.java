package com.hcfpetdev.waterbottletwist;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Message {

    static String prefix;

    static {
        prefix = ChatColor.translateAlternateColorCodes('&',
                "&b<Twist>&r ");
    }

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendPlayerError(Player player, String error) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&4Error: &c") +
                error);
    }

    public static void sendPlayerError(CommandSender sender, String error) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&4Error: &c") +
                error);
    }

    public static void sendPlayerMessage(Player player, String message) {
        player.sendMessage(prefix +  ChatColor.translateAlternateColorCodes('&',message));
    }

    public static void sendPlayerMessage(CommandSender sender, String message) {
        sender.sendMessage(prefix +  ChatColor.translateAlternateColorCodes('&',message));
    }

    public static String format(String format, Object ... params) {
        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();
        List<Double> doubles = new ArrayList<>();
        List<Character> chars = new ArrayList<>();

        int stringCounter = 0;
        int intCounter = 0;
        int doubleCounter = 0;
        int charCounter = 0;

        for (Object o : params) {
            if (o instanceof Character) chars.add((char)o);
            else if (o instanceof Integer) integers.add((int)o);
            else if (o instanceof Double) doubles.add((double)o);
            else strings.add((String)o);
        }

        while (format.contains("%s")) format = format.replaceFirst("%s", strings.get(stringCounter++));
        while (format.contains("%i")) format = format.replaceFirst("%i", integers.get(intCounter++).toString());
        while (format.contains("%d")) format = format.replaceFirst("%d", doubles.get(doubleCounter++).toString());
        while (format.contains("%c")) format = format.replaceFirst("%c", chars.get(charCounter++).toString());

        return format;
    }

}
