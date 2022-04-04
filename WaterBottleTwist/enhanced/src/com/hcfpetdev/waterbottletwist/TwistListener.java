package com.hcfpetdev.waterbottletwist;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class TwistListener implements Listener {

    private boolean overPoweredPotions, runnerOnly;
    private int maxLevel, seconds;


    public TwistListener(boolean overpoweredPotions, int maxLevel, int seconds, boolean runnerOnly) {
        this.overPoweredPotions = overpoweredPotions;
        this.maxLevel = maxLevel;
        this.runnerOnly = runnerOnly;
        this.seconds = seconds;
    }

    @EventHandler
    private void onWaterBottleFill(PlayerInteractEvent event) {
        if (!Main.twistIsEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.GLASS_BOTTLE) return;
        if (runnerOnly && Main.getRunners().contains(event.getPlayer().getName())) return;

        Player player = event.getPlayer();

        Vector direction = event.getPlayer().getFacing().getDirection();

        Block result = player.getTargetBlockExact(3, FluidCollisionMode.SOURCE_ONLY);

        if (result == null) return;

        Material material = result.getType();

        if (material == Material.WATER || material == Material.LAVA ||
        material == Material.DRAGON_BREATH) {
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getAmount() == 1) item = new ItemStack(Material.AIR);
            else item.setAmount(item.getAmount() - 1);

            player.getInventory().setItemInMainHand(item);

            if (player.getInventory().firstEmpty() > 0) player.getInventory().addItem(getRandomPotion(material));
            else player.getWorld().dropItem(player.getLocation(), getRandomPotion(material));

            event.setCancelled(true);
        }

    }

    @EventHandler
    private void cauldronLevelChangeEvent(CauldronLevelChangeEvent event) {
        if (!Main.twistIsEnabled()) return;

        if (event.getReason() != CauldronLevelChangeEvent.ChangeReason.BOTTLE_FILL) return;

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (runnerOnly && Main.getRunners().contains(player.getName())) return;

            player.getInventory().remove(Material.POTION);

            player.getInventory().addItem(getRandomPotion(Material.WATER));
        }
    }

    @EventHandler
    private void onSplashPotionCraft(CraftItemEvent event) {
        if (runnerOnly && Main.getRunners().contains(event.getWhoClicked().getName())) return;

        if (!(event.getRecipe() instanceof ShapelessRecipe)) return;

        ShapelessRecipe recipe = (ShapelessRecipe) event.getRecipe();

        if ((!recipe.getKey().getKey().equals("waterbottletwistkey"))) return;

        PotionMeta meta = null;

        for (ItemStack stack : event.getInventory().getContents()) {
            if (stack.getType() == Material.POTION) meta = (PotionMeta) stack.getItemMeta();
        }


        if (meta == null) return;

        meta.setDisplayName(meta.getDisplayName().replaceFirst("Potion of", "§rSplash potion of"));

        ItemStack output = new ItemStack(Material.SPLASH_POTION);

        output.setItemMeta(meta);

        event.setCurrentItem(output);
    }

    @EventHandler
    private void onHarmingPotionSplash(PotionSplashEvent event) {
        if (!event.getPotion().getItem().hasItemMeta()) return;
        if (!((PotionMeta)event.getPotion().getItem().getItemMeta()).getCustomEffects().get(0).getType().getName().equals("HARM")) return;

            int level = ((PotionMeta)event.getPotion().getItem().getItemMeta()).getCustomEffects().get(0).getAmplifier();

            PotionEffect effect =  new PotionEffect(PotionEffectType.HARM,1,level);

            EntityType type;


            for (Entity e : event.getAffectedEntities()) {

                if (e instanceof Player) ((Player) e).damage(level * 6);

                 type = e.getType();

                 LivingEntity entity = (LivingEntity) e;

                if (!type.isAlive()) entity.setHealth(entity.getHealth() + 4 * level);
                else {
                    if (entity.getHealth() > level * 6) entity.setHealth(entity.getHealth() - level * 6);
                    else entity.setHealth(0);
                }
            }
    }

    private ItemStack getRandomPotion(Material material) {

        ItemStack item;
        PotionMeta meta;

        item = new ItemStack(Material.POTION);
        if (material == Material.LAVA) item = new ItemStack(Material.SPLASH_POTION);
        if (material == Material.DRAGON_BREATH) item = new ItemStack(Material.LINGERING_POTION);

        meta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(item.getType());

        double rand = Math.random();

        PotionEffectType[] types = PotionEffectType.values();

        PotionEffectType type = types[(int)(rand * types.length)];

        String potionName = "";
        String[] words = type.getName().toLowerCase().split("_");

        for (String s : words) potionName += (s.charAt(0)+"").toUpperCase() + s.substring(1) + " ";

        if (item.getType() == Material.SPLASH_POTION) potionName = "Splash potion of " + potionName;
        else if (item.getType() == Material.LINGERING_POTION) potionName = "Lingering potion of " + potionName;
        else potionName = "Potion of " + potionName;

        assert meta != null;

        int level;
        int time = 20 * seconds;

        if (overPoweredPotions) level = (int)(Math.random() * (maxLevel - 1));
        else level = (int)Math.round(Math.random());

        if (type.isInstant()) time = 10;

        PotionEffect effect = new PotionEffect(type, time, level);

        meta.addCustomEffect(effect, false);

        meta.setColor(type.getColor());

        meta.setDisplayName("§r" + potionName + (level + 1));

        item.setItemMeta(meta);

        return item;
    }

    private PotionType randomPotionType() {
        PotionType[] types = PotionType.values();

        int random = (int)(Math.random() * types.length);

        System.out.println(types[random].name());

        return types[random];
    }
}
