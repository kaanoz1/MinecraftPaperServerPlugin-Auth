package org.prag.mc.plugins.auth.Repositories;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthRepository {
    private final Set<UUID> signedInPlayers = new HashSet<>();

    public boolean isSignedIn(Player player) {
        return signedInPlayers.contains(player.getUniqueId());
    }


    public void markAsSignedIn(Player player) {
        signedInPlayers.add(player.getUniqueId());
    }

    public void markAsSignedOut(Player player) {
        signedInPlayers.remove(player.getUniqueId());
    }
}
