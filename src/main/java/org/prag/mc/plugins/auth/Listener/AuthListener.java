package org.prag.mc.plugins.auth.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prag.mc.plugins.auth.Auth;
import org.prag.mc.plugins.auth.Cache.PlayerStateCache;
import org.prag.mc.plugins.auth.Repositories.AuthRepository;
import org.prag.mc.plugins.auth.WelcomeMessageProducer;
import org.prag.mc.plugins.serverDatabaseController.Models.RecordedPlayer;
import org.prag.mc.plugins.serverDatabaseController.ServerDatabaseController;

import java.util.UUID;
import java.util.logging.Level;

public class AuthListener implements Listener {
    private final AuthRepository authRepository;
    private final PlayerStateCache playerStateCache;

    public AuthListener(PlayerStateCache playerStateCache) {
        RegisteredServiceProvider<AuthRepository> authServiceProvider = Bukkit.getServicesManager().getRegistration(AuthRepository.class);
        if (authServiceProvider == null)
            throw new RuntimeException("AuthRepository is NOT registered!");

        this.authRepository = authServiceProvider.getProvider();
        this.playerStateCache = playerStateCache;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {

        event.joinMessage(null);

        Player bukkitPlayer = event.getPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();

        var realEffects = bukkitPlayer.getActivePotionEffects().stream()
                .filter(e -> e.getDuration() < 1000000)
                .toList();
        playerStateCache.saveState(bukkitPlayer, realEffects);

        bukkitPlayer.getActivePotionEffects().forEach(e -> bukkitPlayer.removePotionEffect(e.getType()));

        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, true, true));
        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false, false));
        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 255, false, false, false));
        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false, false));
        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false, false));
        authRepository.markAsSignedOut(bukkitPlayer);

        try (Session session = ServerDatabaseController.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            var query = session.createQuery("from RecordedPlayer where uuid = :uuid", RecordedPlayer.class);
            query.setParameter("uuid", uuid);
            var player = query.uniqueResult();

            if (player == null) {
                player = new RecordedPlayer(uuid, bukkitPlayer.getName());
                session.persist(player);
                bukkitPlayer.sendMessage(Component.text(WelcomeMessageProducer.getRandomWelcome() + " ", NamedTextColor.LIGHT_PURPLE).append(Component.text("Şu komutla kayıt ol: ", NamedTextColor.GREEN).append(Component.text("/register <şifre> <şifre>", NamedTextColor.YELLOW))));
                bukkitPlayer.showTitle(WelcomeMessageProducer.getNewPlayerTitle());
            } else if (!player.isRegistered()) {
                bukkitPlayer.sendMessage(Component.text(WelcomeMessageProducer.getRandomReminder(), NamedTextColor.YELLOW));
                bukkitPlayer.showTitle(WelcomeMessageProducer.getRegisterReminderTitle());
            } else {
                bukkitPlayer.sendMessage(Component.text("Hoş geldin! Giriş yapmak için ", NamedTextColor.GREEN).append(Component.text("/login <şifre>", NamedTextColor.YELLOW)));
                bukkitPlayer.showTitle(WelcomeMessageProducer.getLoginReminderTitle());
            }
            transaction.commit();
        } catch (Exception e) {
            bukkitPlayer.kick(Component.text("Veritabanı hatası. Daha sonra tekrar deneyin.", NamedTextColor.LIGHT_PURPLE));
            Auth.getPlugin(Auth.class).getLogger().log(Level.SEVERE, "Database error in PlayerJoinEvent", e);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        boolean isPlayerFrozen = !authRepository.isSignedIn(player);

        if (isPlayerFrozen) {
            event.quitMessage(null);

            player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));

            var saved = playerStateCache.pullState(player);
            if (saved != null)
                player.addPotionEffects(saved);

        } else {
            Component quitMsg = Component.text("", NamedTextColor.GRAY)
                    .append(Component.text("[", NamedTextColor.GRAY))
                    .append(Component.text("!", NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("] ", NamedTextColor.GRAY))
                    .append(Component.text(player.getName(), NamedTextColor.DARK_PURPLE))
                    .append(Component.text(" sunucudan ayrıldı.", NamedTextColor.LIGHT_PURPLE));

            event.quitMessage(quitMsg);
            authRepository.markAsSignedOut(player);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(event.getPlayer());
        if (isPlayerFrozen) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(event.getPlayer());
        if (isPlayerFrozen) {
            String cmd = event.getMessage().toLowerCase();
            if (!cmd.startsWith("/login") && !cmd.startsWith("/register")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Komut girmeden önce giriş yapmalısın!", NamedTextColor.GOLD));
            }
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(e.getPlayer());
        if (isPlayerFrozen) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(e.getPlayer());
        if (isPlayerFrozen) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(e.getPlayer());
        if (isPlayerFrozen) e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(e.getPlayer());
        if (isPlayerFrozen) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(e.getPlayer());
        if (isPlayerFrozen) e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        boolean isPlayerFrozen = !authRepository.isSignedIn(e.getPlayer());
        if (isPlayerFrozen) e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            boolean isPlayerFrozen = !authRepository.isSignedIn(p);
            if (isPlayerFrozen) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) {
            boolean isPlayerFrozen = !authRepository.isSignedIn(p);
            if (isPlayerFrozen) e.setCancelled(true);
        }
    }
}