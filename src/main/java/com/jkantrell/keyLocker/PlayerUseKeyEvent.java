package com.jkantrell.keyLocker;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import javax.annotation.Nonnull;

public class PlayerUseKeyEvent extends PlayerEvent implements Cancellable {

    //EVENT-REQUIRED ================================================
    private static final HandlerList HANDLERS = new HandlerList();
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    //===============================================================

    //ASSETS
    public enum Action { ASSIGN, UNLOCK }

    //FIELDS
    private final Key key_;
    private final Block block_;
    private final PlayerUseKeyEvent.Action action_;
    private boolean cancelled_ = false;

    //CONSTRUCTOR
    public PlayerUseKeyEvent(Player who, Key key, Block block, PlayerUseKeyEvent.Action action) {
        super(who);
        this.key_ = key;
        this.block_ = block;
        this.action_ = action;
    }

    //GETTERS
    public Key getKey() {
        return key_;
    }
    public Block getBlock() {
        return block_;
    }
    public PlayerUseKeyEvent.Action getAction() {
        return this.action_;
    }
    @Override
    public boolean isCancelled() {
        return this.cancelled_;
    }

    //SETTERS
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled_ = cancel;
    }
}
