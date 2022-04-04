package com.hcfpetdev.waterbottletwist.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandHandler extends BukkitCommand implements CommandExecutor {

    private List<String> delayedPlayers = null;
    private int delay;
    private Permission permission;
    private List<String> alias;
    private int minArgs,maxArgs;
    private boolean playerOnly;
    private JavaPlugin instance;



    public CommandHandler(JavaPlugin instance, String command) {
        this(instance, command, false);
    }

    public CommandHandler(JavaPlugin instance, String command, boolean playerOnly) {
        this(instance ,command, 0,-1, playerOnly);
    }

    public CommandHandler(JavaPlugin instance, String command, int minArgs, int maxArgs) {
        this(instance, command,minArgs, maxArgs, false);
    }

    public CommandHandler(JavaPlugin instance, String command, int reqArgs) {
        this(instance, command,reqArgs,false);
    }

    public CommandHandler(JavaPlugin instance, String command, int reqArgs, boolean playerOnly) {
        this(instance, command, reqArgs, reqArgs, playerOnly);
    }

    public CommandHandler(JavaPlugin instance, String command, int minArgs, int maxArgs, boolean playerOnly) {
        this(instance, command,minArgs,maxArgs,playerOnly,null);
    }

    public CommandHandler(JavaPlugin instance, String command, int minArgs, int maxArgs, boolean playerOnly, List<String> aliases) {
        super(command);

        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
        this.playerOnly = playerOnly;
        this.alias = aliases;
        this.permission = null;
        this.instance = instance;

        setDescription(getDescription());
        if (aliases != null) setAliases(aliases);


        CommandMap commandmap = getCommandMap();
        if (commandmap != null) {
            commandmap.register(command, this);
        }
    }

    public CommandMap getCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);

                return (CommandMap) field.get(Bukkit.getPluginManager());
            }
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public CommandHandler enableDelay(int delay) {
        this.delay = delay;
        this.delayedPlayers = new ArrayList<>();
        return this;
    }

    public void removePlayer(Player player) {
        this.delayedPlayers.remove(player.getName());
    }

    public void sendUsage(CommandSender sender) {
        sender.sendMessage("§cUsage: " + getUsage());
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (args.length < minArgs || (args.length > maxArgs && maxArgs != -1)) {
            sendUsage(sender);
            return true;
        }

        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command");
            return true;
        }

        if (delayedPlayers != null && (sender instanceof Player)) {
            Player player = ((Player) sender);
            if (delayedPlayers.contains(player.getName())) {
                player.sendMessage("§cPlease wait before using this command again");
                return true;
            }

            if (!player.isOp()) {
                delayedPlayers.add(player.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
                    delayedPlayers.remove(player.getName());
                }, 20L * delay);
            }
        }

        if (!onCommand(sender, args)) {
            sendUsage(sender);
        }
        return true;

    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        this.onCommand(sender,args);
        return false;
    }

    public abstract boolean onCommand(CommandSender sender, String[] args);

    public abstract String getUsage();

    public abstract String getDescription();



}
