package com.jkantrell.keyLocker;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

class EventListener implements Listener {

    @EventHandler
    void onKayUse(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) { return; }

        Block block = e.getClickedBlock();
        if (block == null) { return; }
        if (!KeyLocker.isBlockLockable(block)) { return; }

        Key key = Key.getKey(e.getItem());

        if (KeyLocker.isBlockLocked(block)) {
            if (key == null || !key.isAssignedTo(block)) { e.setCancelled(true); }
            return;
        }

        if (key == null || key.isAssigned()) { return; }

        key.assignTo(block);
        e.setCancelled(true);
    }

    @EventHandler
    void onAnvilPrepare(PrepareAnvilEvent e) {
        AnvilInventory inventory = e.getInventory();
        Key key = Key.getKey(inventory.getItem(0));
        if (key == null || !key.isAssigned()) { return; }
        ItemStack mole = inventory.getItem(1);
        if (mole == null || Key.isKey(mole) || !Key.isKeyable(mole)) { return; }

        ItemStack result = new ItemStack(mole.getType());
        result.setAmount(mole.getAmount());
        result.setItemMeta(key.getItem().getItemMeta());

        inventory.setRepairCost(0);
        e.setResult(result);
    }

    @EventHandler
    void onKeyCopy(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory inventory)) { return; }
        ItemStack grabbedItem = e.getCurrentItem();
        if (!Key.isKey(grabbedItem)) { return; }
        if (e.getRawSlot() != 2) { return; }
        ItemStack baseItem = inventory.getItem(0);
        if (!Key.isKey(baseItem)) { return; }
        new BukkitRunnable(){
            @Override
            public void run() {
                inventory.setItem(0,baseItem);
            }
        }.runTaskLater(KeyLocker.getMainInstance(),1);
    }

}
