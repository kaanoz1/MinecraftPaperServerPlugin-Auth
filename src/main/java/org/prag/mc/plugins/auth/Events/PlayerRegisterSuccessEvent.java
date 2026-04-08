package org.prag.mc.plugins.auth.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.prag.mc.plugins.serverDatabaseController.Models.RecordedPlayer;

public class PlayerRegisterSuccessEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player bukkitPlayer;
    private final RecordedPlayer dbPlayer;

    public PlayerRegisterSuccessEvent(Player bukkitPlayer, RecordedPlayer dbPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        this.dbPlayer = dbPlayer;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public RecordedPlayer getDbPlayer() {
        return dbPlayer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}