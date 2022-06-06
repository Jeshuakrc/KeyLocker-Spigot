package com.jkantrell.keyLocker;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.function.Consumer;

public class Key {
    //FIELDS
    private final ItemStack item_;

    //CONSTRUCTOR
    private Key(ItemStack item) {
        this.item_ = item;
    }

    //STATIC
    public static Key getKey(ItemStack item) {
        if (!Key.isKey(item)) { return null; }
        return new Key(item);
    }
    public static boolean isKey(ItemStack item) {
        if (item == null) { return false; }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {return false; }
        return meta.getPersistentDataContainer().has(KeyLocker.KEY_ID_NAMESPACE_KEY, PersistentDataType.STRING);
    }
    public static boolean isKeyable(ItemStack item) {
        Material type = item.getType();
        return type.equals(Material.IRON_NUGGET) || type.equals(Material.GOLD_NUGGET);
    }

    //GETTERS
    public ItemStack getItem() {
        return item_;
    }
    public UUID getId() {
        if (!this.isAssigned()) { return null; }
        return UUID.fromString(this.item_.getItemMeta().getPersistentDataContainer().get(KeyLocker.KEY_ID_NAMESPACE_KEY,PersistentDataType.STRING));
    }
    public boolean isAssigned() {
        return !item_.getItemMeta().getPersistentDataContainer().get(KeyLocker.KEY_ID_NAMESPACE_KEY,PersistentDataType.STRING).equals("");
    }
    public boolean isAssignedTo(Block block) {
        if (!isAssigned()) { return false; }
        if (!KeyLocker.isBlockLockable(block)) { return false; }

        String keyId = this.item_.getItemMeta().getPersistentDataContainer().get(KeyLocker.KEY_ID_NAMESPACE_KEY,PersistentDataType.STRING);
        String blockId = new CustomBlockData(block,KeyLocker.getMainInstance()).get(KeyLocker.KEY_ID_NAMESPACE_KEY,PersistentDataType.STRING);

        if (blockId == null) { return false; }
        return blockId.equals(keyId);
    }

    //METHODS
    public void assignTo(Block block) {
        if (!KeyLocker.isBlockLockable(block)) {
            throw new IllegalArgumentException("'" + block.getType().toString() + "' is not a lockable block.");
        }

        UUID id = UUID.randomUUID();
        Consumer<PersistentDataContainer> assign = c -> c.set(KeyLocker.KEY_ID_NAMESPACE_KEY, PersistentDataType.STRING, id.toString());

        ItemMeta meta = this.item_.getItemMeta();
        meta.setDisplayName("Key");
        assign.accept(meta.getPersistentDataContainer());
        this.item_.setItemMeta(meta);
        assign.accept(new CustomBlockData(block,KeyLocker.getMainInstance()));

        if (block.getBlockData() instanceof Door door) {
            BlockFace face = (door.getHalf().equals(Bisected.Half.BOTTOM)) ? BlockFace.UP : BlockFace.DOWN;
            assign.accept(new CustomBlockData(block.getRelative(face),KeyLocker.getMainInstance()));
        }
    }
}
