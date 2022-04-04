package com.hcfpetdev.waterbottletwist.commands;

import com.hcfpetdev.waterbottletwist.Main;
import com.hcfpetdev.waterbottletwist.Message;
import com.sun.media.jfxmedia.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class WaterBottleTwist {
    public WaterBottleTwist(JavaPlugin instance) {
        String command = instance.getConfig().getString("command");

        if (command == null) command = "";
        if (command.equals("")) {
            command = "/wbtwist";
            instance.getLogger().warning("'command' not set in config.yml, defaulting to /wbtwist");
        }

        if (command.startsWith("/")) command = command.substring(1);

        CommandHandler wbCommand =  new CommandHandler(instance, command, 1) {

            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                if (args[0].equalsIgnoreCase("start")) {
                    if (Main.twistIsEnabled()) {
                        Message.sendPlayerError(sender, "That twist is already enabled");
                        return true;
                    }
                    Message.broadcast("The twist has started");
                    Main.setTwistEnabled(true);
                }
                else if (args[0].equalsIgnoreCase("stop")) {
                    if (!Main.twistIsEnabled()) {
                        Message.sendPlayerError(sender, "That twist isn't enabled");
                        return true;
                    }
                    Message.broadcast("The twist has stopped");
                    Main.setTwistEnabled(true);
                }
                else return false;
                return true;
            }

            @Override
            public String getUsage() {
                return "/" + this.getName() + " <start|stop>";
            }

            @Override
            public String getDescription() {
                return "Starts or stop a twist";
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                if (args.length == 1) return Arrays.asList("start","stop");
                return super.tabComplete(sender, alias, args);
            }
        };
    }
}
