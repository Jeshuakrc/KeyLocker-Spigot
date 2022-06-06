package com.jkantrell.keyLocker;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jkantrell.keyLocker.io.KeyLockerConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Openable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public final class KeyLocker extends JavaPlugin {

    //FIELDS
    public static NamespacedKey KEY_ID_NAMESPACE_KEY;
    public static final KeyLockerConfiguration CONFIG = new KeyLockerConfiguration("./plugins/KeyLocker/config.yml");
    public static final ItemStack KEY_ITEM_GOLD = new ItemStack(Material.GOLD_NUGGET);
    public static final ItemStack KEY_ITEM_IRON = new ItemStack(Material.IRON_NUGGET);
    private static JavaPlugin mainInstance_;

    //PLUGIN EVENTS
    @Override
    public void onEnable() {
        //Loading configuration
        try {
            KeyLocker.CONFIG.load();
        } catch (FileNotFoundException e) {
            File configFile = new File(KeyLocker.CONFIG.getFilePath());
            configFile.getParentFile().mkdirs();
            this.saveResource("config.yml",true);
            this.onEnable();
            return;
        }

        //Setting main instance
        KeyLocker.mainInstance_ = this;

        //Setting variables
        KEY_ID_NAMESPACE_KEY = new NamespacedKey(this,"keyID");
        this.setItemsStacks();

        //Registering listeners
        CustomBlockData.registerListener(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(),this);

        // Defines the Key Recipe
        SmithingRecipe ironRecipe = new SmithingRecipe(
                new NamespacedKey(this,"IRON_DOOR_KEY"),
                KEY_ITEM_IRON,
                new RecipeChoice.MaterialChoice(Material.IRON_NUGGET),
                new RecipeChoice.MaterialChoice(Material.COPPER_INGOT)
                );

        SmithingRecipe goldRecipe = new SmithingRecipe(
                new NamespacedKey(this,"GOLDEN_DOOR_KEY"),
                KEY_ITEM_GOLD,
                new RecipeChoice.MaterialChoice(Material.GOLD_NUGGET),
                new RecipeChoice.MaterialChoice(Material.COPPER_INGOT)
        );

        Bukkit.addRecipe(goldRecipe);
        Bukkit.addRecipe(ironRecipe);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    //STATIC METHODS
    public static boolean isBlockLockable(Block block) {
        return KeyLocker.isBlockLockable(block.getType());
    }
    public static boolean isBlockLockable(Material material) {
        if (!material.isBlock()) { return false; }
        return (
                (material.equals(Material.BARREL)) ||
                (material.toString().toLowerCase().contains("chest")) ||
                (material.toString().toLowerCase().contains("door")) ||
                (material.toString().toLowerCase().contains("fence_gate"))
        );
    }
    public static boolean isBlockAssigned(Block block) {
        if (!KeyLocker.isBlockLockable(block)) { return false; }
        String id = new CustomBlockData(block,KeyLocker.getMainInstance()).get(KEY_ID_NAMESPACE_KEY,PersistentDataType.STRING);
        if (id == null) { return false; }
        return !id.equals("");
    }
    public static JavaPlugin getMainInstance() {
        return KeyLocker.mainInstance_;
    }

    private void setItemsStacks() {
        ItemStack item = KEY_ITEM_GOLD;
        boolean repeat = false;
        do {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Unassigned key");
            meta.setLore(List.of("This key will be assigned to the next", "lockable block it's used on."));
            meta.addEnchant(Enchantment.CHANNELING,1,true);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(KEY_ID_NAMESPACE_KEY, PersistentDataType.STRING,"");
            item.setItemMeta(meta);

            repeat = !repeat;
            item = KEY_ITEM_IRON;
        } while (repeat);
    }
}
