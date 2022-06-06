package com.jkantrell.keyLocker;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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


}
