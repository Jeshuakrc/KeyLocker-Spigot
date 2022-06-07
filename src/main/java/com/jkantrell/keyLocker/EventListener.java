package com.jkantrell.keyLocker;

import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

class EventListener implements Listener {

    private static class OpenLiddeds {
        private final HashMap<Lidded, LinkedList<Player>> liddedMap_ = new HashMap<>();

        void put(Lidded lidded, Player player) {
            this.liddedMap_.putIfAbsent(lidded,new LinkedList<>());
            this.liddedMap_.get(lidded).add(player);
        }
        void remove(Player player) {
            Iterator<Map.Entry<Lidded,LinkedList<Player>>> i = this.liddedMap_.entrySet().iterator();
            LinkedList<Player> players;
            while (i.hasNext()) {
                players = i.next().getValue();
                players.removeIf(p -> p.equals(player));
                if (players.isEmpty()) { i.remove(); }
            }
        }
        boolean isOpen(Lidded lidded) {
            return this.liddedMap_.containsKey(lidded);
        }
    }

    private OpenLiddeds openLiddeds = new OpenLiddeds();

    @EventHandler
    void onKayUse(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) { return; }

        Block block = e.getClickedBlock();
        if (block == null) { return; }
        if (!KeyLocker.isBlockLockable(block)) { return; }

        Key key = KeyLocker.CONFIG.handsFreeKeys ?
                Key.getKey(Arrays.stream(e.getPlayer().getInventory().getContents()).filter(
                    i -> {
                        Key key_ = Key.getKey(i);
                        return key_ != null && key_.isAssignedTo(block);
                    }
                ).findFirst().orElse(null)) :
                Key.getKey(e.getItem());

        if (KeyLocker.isBlockAssigned(block)) {
            if (KeyLocker.CONFIG.unlockIfOpen && block.getBlockData() instanceof Openable openable) {
                if (openable.isOpen()) { return; }
            }
            if (KeyLocker.CONFIG.accessibleIfOpen && block.getState() instanceof Lidded lidded) {
                if (this.openLiddeds.isOpen(lidded)) {
                    this.openLiddeds.put(lidded,e.getPlayer());
                    return;
                }
            }
            if (key == null || !key.isAssignedTo(block)) { e.setCancelled(true); }
            else if (KeyLocker.CONFIG.accessibleIfOpen && block.getState() instanceof Lidded lidded) {
                this.openLiddeds.put(lidded,e.getPlayer());
            }
            return;
        }

        if (key == null || key.isAssigned()) { return; }

        key.assignTo(block);
        e.setCancelled(true);
    }

    @EventHandler
    void onCloseLidded(InventoryCloseEvent e) {
        if (!KeyLocker.CONFIG.accessibleIfOpen) { return; }
        if (!(e.getPlayer() instanceof Player player)) { return; }
        this.openLiddeds.remove(player);
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
