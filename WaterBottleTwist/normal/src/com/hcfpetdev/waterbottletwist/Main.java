package com.hcfpetdev.waterbottletwist;

import com.hcfpetdev.waterbottletwist.commands.WaterBottleTwist;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;

public class Main extends JavaPlugin {

    private static String name, version;
    private static boolean enabled, overpoweredPotions, craftable;

    public static ShapelessRecipe splashRecipe;

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

        overpoweredPotions = config.getBoolean("overpowered-potions");
        craftable = config.getBoolean("craftable-splash");

        if (craftable) {
            NamespacedKey nsKey = new NamespacedKey(this, "waterbottletwistkey");
            splashRecipe = new ShapelessRecipe(nsKey, new ItemStack(Material.SPLASH_POTION));
            splashRecipe.addIngredient(Material.GUNPOWDER);
            splashRecipe.addIngredient(Material.POTION);

            Bukkit.getServer().addRecipe(splashRecipe);
        }


        int maxLevel = config.getInt("maxlevel");

        WaterBottleTwist waterBottleTwist = new WaterBottleTwist(this);
        int seconds = config.getInt("duration");

        TwistListener twistListener = new TwistListener(overpoweredPotions, maxLevel, seconds);

        pluginManager.registerEvents(twistListener, this);
    }

    public static void setTwistEnabled(boolean enabled) {
        Main.enabled = enabled;
    }

    public static boolean twistIsEnabled() {
        return enabled;
    }
}
