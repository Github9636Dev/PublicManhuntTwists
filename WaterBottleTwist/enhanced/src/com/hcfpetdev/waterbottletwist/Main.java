package com.hcfpetdev.waterbottletwist;

import com.hcfpetdev.waterbottletwist.commands.WaterBottleTwist;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

public class Main extends JavaPlugin {

    private static String name, version;
    private static boolean enabled, overpoweredPotions, craftable;

    public static ShapelessRecipe splashRecipe;

    private static List<String> runners;
    private static com.hcfpetdev.baseManhunt.Main manhunt;

    public static String getPluginName() {
        return name;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        name = getDescription().getName();
        version = getDescription().getVersion();

        PluginManager pluginManager = Bukkit.getPluginManager();
        FileConfiguration config = getConfig();
        if (!new File(config.getCurrentPath()).exists()) saveDefaultConfig();

        getConfig().options().copyDefaults(true);

        String name;

        if (config.getBoolean("random-name")) {

            List<String> names = config.getStringList("names");
            String randName = names.get((int)(Math.random() * names.size()));
            name = randName;

            try {
                PluginDescriptionFile object = getDescription();

                Field field = PluginDescriptionFile.class.getDeclaredField("name");
                field.setAccessible(true);
                field.set(object, randName);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        else name = this.getName();

        overpoweredPotions = config.getBoolean("overpowered-potions");
        craftable = config.getBoolean("craftable-splash");

        manhunt = (com.hcfpetdev.baseManhunt.Main) pluginManager.getPlugin("ManhuntBase");

        if (manhunt == null) {
            getLogger().warning("will not work without ManhuntBase, disabling");
            pluginManager.disablePlugin(this);
            return;
        }

        if (craftable) {
            NamespacedKey nsKey = new NamespacedKey(manhunt, "waterbottletwistkey");
            splashRecipe = new ShapelessRecipe(nsKey, new ItemStack(Material.SPLASH_POTION));
            splashRecipe.addIngredient(Material.GUNPOWDER);
            splashRecipe.addIngredient(Material.POTION);

            Bukkit.getServer().addRecipe(splashRecipe);
        }

        runners = manhunt.getConfig().getStringList("values.runners");


        int maxLevel = config.getInt("maxlevel");

        WaterBottleTwist waterBottleTwist = new WaterBottleTwist(this);
        int seconds = config.getInt("duration");

        boolean runnerOnly = config.getBoolean("runner-only");

        TwistListener twistListener = new TwistListener(overpoweredPotions, maxLevel, seconds, runnerOnly);

        pluginManager.registerEvents(twistListener, this);

        if (!config.getBoolean("autostart")) return;

        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (manhunt.getConfig().getBoolean("values.inprogress") && !enabled) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),  WaterBottleTwist.command +
                        " start");
            }
            if (!manhunt.getConfig().getBoolean("values.inprogress") && enabled) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),  WaterBottleTwist.command +
                        " stop");
            }
        },20,20);

    }

    public static void setTwistEnabled(boolean enabled) {
        Main.enabled = enabled;
    }

    public static boolean twistIsEnabled() {
        return enabled;
    }

    public static List<String> getRunners() {
        runners = manhunt.getConfig().getStringList("values.runners");
        return runners;
    }
}
