package org.prag.mc.plugins.auth.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.prag.mc.plugins.auth.Cache.PlayerStateCache;
import org.prag.mc.plugins.auth.Events.PlayerLoginSuccessEvent;
import org.prag.mc.plugins.auth.Repositories.AuthRepository;
import org.prag.mc.plugins.serverDatabaseController.Models.RecordedPlayer;
import org.prag.mc.plugins.serverDatabaseController.ServerDatabaseController;

public class LoginCommand implements CommandExecutor {
    private final AuthRepository authRepository;
    private final PlayerStateCache playerStateCache;

    public LoginCommand(PlayerStateCache playerStateCache) {
        RegisteredServiceProvider<AuthRepository> authServiceProvider = Bukkit.getServicesManager().getRegistration(AuthRepository.class);

        if (authServiceProvider == null)
            throw new RuntimeException("AuthRepository is NOT registered!. This should not happen.");

        this.authRepository = authServiceProvider.getProvider();
        this.playerStateCache = playerStateCache;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.YELLOW));
            return true;
        }

        if (args.length != 1 || args[0].isEmpty() || args[0].isBlank()) {
            player.sendMessage(Component.text("Usage: ", NamedTextColor.RED).append(Component.text("/login <password>", NamedTextColor.YELLOW)));
            return true;
        }

        if (authRepository.isSignedIn(player)) {
            player.sendMessage(Component.text("You are already logged in!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }

        String inputPass = args[0];

        try (Session session = ServerDatabaseController.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            var dbPlayer = session.createQuery("from RecordedPlayer where uuid = :uuid", RecordedPlayer.class)
                    .setParameter("uuid", player.getUniqueId())
                    .uniqueResult();

            if (dbPlayer == null || !dbPlayer.isRegistered()) {
                player.sendMessage(Component.text("You are not registered yet! Use ", NamedTextColor.RED).append(Component.text(" /register <password> <password> ", NamedTextColor.YELLOW)).append(Component.text("first.", NamedTextColor.RED)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                return true;
            }


            if (dbPlayer.isPasswordCorrect(inputPass)) {
                session.merge(dbPlayer);

                tx.commit();

                authRepository.markAsSignedIn(player);

                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType())
                );

                var savedState = playerStateCache.pullState(player);
                if (savedState != null && !savedState.isEmpty())
                    player.addPotionEffects(savedState);


                PlayerLoginSuccessEvent loginEvent = new PlayerLoginSuccessEvent(player, dbPlayer);
                Bukkit.getPluginManager().callEvent(loginEvent);

                player.sendMessage(Component.text("Giriş başarılı! Hoş geldin.", NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                player.sendMessage(Component.text("Şifre yanlış! Tekrar dene.", NamedTextColor.RED));
            }

        } catch (Exception e) {
            Component errorMsg = Component.text("Giriş yapma sırasında bir veritabanı hatası oluştu. Daha sonra tekrar deneyin.", NamedTextColor.DARK_PURPLE);
            player.sendMessage(errorMsg);

            Bukkit.getOnlinePlayers().forEach(p -> p.kick(errorMsg));

            Bukkit.getLogger().severe(e.getMessage());
            throw new RuntimeException(e);
        }

        return true;
    }
}