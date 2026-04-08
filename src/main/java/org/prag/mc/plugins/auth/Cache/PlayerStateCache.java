package org.prag.mc.plugins.auth.Cache;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStateCache {

    private final Map<UUID, Collection<PotionEffect>> effectMap;

    public PlayerStateCache() {
        this.effectMap = new HashMap<>();
    }

    public void saveState(Player player, Collection<PotionEffect> effects) {
        this.effectMap.put(player.getUniqueId(), effects);
    }

    public Collection<PotionEffect> pullState(Player player) {
        return this.effectMap.remove(player.getUniqueId());
    }

    public boolean hasState(Player player) {
        return this.effectMap.containsKey(player.getUniqueId());
    }

    public void clearState(Player player) {
        this.effectMap.remove(player.getUniqueId());
    }
}