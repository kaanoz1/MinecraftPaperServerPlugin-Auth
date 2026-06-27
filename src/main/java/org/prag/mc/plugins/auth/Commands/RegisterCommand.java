package org.prag.mc.plugins.auth.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
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
import org.prag.mc.plugins.auth.Events.PlayerRegisterSuccessEvent;
import org.prag.mc.plugins.auth.Repositories.AuthRepository;
import org.prag.mc.plugins.serverDatabaseController.Auth.PlayerRegisterOptions;
import org.prag.mc.plugins.serverDatabaseController.Models.RecordedPlayer;
import org.prag.mc.plugins.serverDatabaseController.ServerDatabaseController;

import java.time.Duration;

public class RegisterCommand implements CommandExecutor {
    private final AuthRepository authRepository;
    private final PlayerStateCache playerStateCache;

    public RegisterCommand(PlayerStateCache playerStateCache) {
        RegisteredServiceProvider<AuthRepository> authServiceProvider = Bukkit.getServicesManager().getRegistration(AuthRepository.class);

        if (authServiceProvider == null)
            throw new RuntimeException("AuthRepository is NOT registered!. This should not happen.");

        this.authRepository = authServiceProvider.getProvider();
        this.playerStateCache = playerStateCache;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Kullanım: /register <password> <password>", NamedTextColor.AQUA));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }

        if(authRepository.isSignedIn(player)){
            player.sendMessage(Component.text("Zaten giriş yaptın.", NamedTextColor.GOLD));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }

        String pass1 = args[0];
        String pass2 = args[1];

        if (!pass1.equals(pass2)) {
            player.sendMessage(Component.text("Şifreler eşleşmiyor.", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }

        try (Session session = ServerDatabaseController.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            var dbPlayer = session.createQuery("from RecordedPlayer where uuid = :uuid", RecordedPlayer.class)
                    .setParameter("uuid", player.getUniqueId())
                    .uniqueResult();

            if (dbPlayer == null) {
                Component errorMsg = Component.text("Kritik: Profik bulunamadı. Tekrar giriş yapın.", NamedTextColor.DARK_PURPLE);
                player.sendMessage(errorMsg);

                player.showTitle(Title.title(
                        Component.text("KRİTİK HATA", NamedTextColor.RED),
                        errorMsg,
                        Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(250))
                ));

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                return true;
            }

            if (dbPlayer.isRegistered()) {
                Component registeredMsg = Component.text("Zaten kayıtlsın! Giriş yap. /login <şifre>.", NamedTextColor.YELLOW);
                player.sendMessage(registeredMsg);

                player.showTitle(Title.title(
                        Component.text("ZATEN KAYITLISIN!", NamedTextColor.GOLD),
                        Component.text("/login <şifre> ile giriş yap.", NamedTextColor.WHITE),
                        Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(3), Duration.ofMillis(250))
                ));

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                return true;
            }

            dbPlayer.register(new PlayerRegisterOptions(pass1));
            session.merge(dbPlayer);
            tx.commit();

            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

            var potionEffects = playerStateCache.pullState(player);
            if (potionEffects != null)
                player.addPotionEffects(potionEffects);

            authRepository.markAsSignedIn(player);


            PlayerRegisterSuccessEvent registerEvent = new PlayerRegisterSuccessEvent(player, dbPlayer);
            Bukkit.getPluginManager().callEvent(registerEvent);

            player.sendMessage(Component.text("Başarıyla kaydedildin! Şimdi giriş yapabilirsin.", NamedTextColor.GREEN));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } catch (Exception e) {
            Component errorMsg = Component.text("Kayıt olma sırasında bir veritabanı hatası gerçekleşti. Bu hata devam ederse admin'e bildirin.", NamedTextColor.DARK_PURPLE);
            player.sendMessage(errorMsg);
            Bukkit.getOnlinePlayers().forEach(p -> p.kick(errorMsg));
            Bukkit.getLogger().severe(e.getMessage());
            throw new RuntimeException(e);
        }

        return true;
    }
}