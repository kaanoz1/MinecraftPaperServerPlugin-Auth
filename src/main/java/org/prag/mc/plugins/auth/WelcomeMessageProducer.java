package org.prag.mc.plugins.auth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class WelcomeMessageProducer {

    private static final String[] NEW_PLAYER_MESSAGES = {
            "Kulübe hoş geldin!",
            "Yeni bir efsane geldi!",
            "Seni burada görmek güzel, maceracı!"
    };

    private static final String[] REMIND_MESSAGES = {
            "Hesabınızı güvenceye almak için lütfen kayıt olun.",
            "Önce güvenlik! Şimdi kayıt olun.",
            "Yolculuğunuz sizi bekliyor, sadece bir adım kaldı!"
    };

    public static String getRandomWelcome() {
        return NEW_PLAYER_MESSAGES[ThreadLocalRandom.current().nextInt(NEW_PLAYER_MESSAGES.length)];
    }

    public static String getRandomReminder() {
        return REMIND_MESSAGES[ThreadLocalRandom.current().nextInt(REMIND_MESSAGES.length)];
    }

    public static Title getNewPlayerTitle() {
        return Title.title(
                Component.text("HOŞ GELDİN!", NamedTextColor.AQUA),
                Component.text("Kayıt olmak için: /register <şifre> <şifre>", NamedTextColor.WHITE),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        );
    }

    public static Title getRegisterReminderTitle() {
        return Title.title(
                Component.text("KAYIT OL!", NamedTextColor.RED),
                Component.text("/register <şifre> <şifre>", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(250))
        );
    }

    public static Title getLoginReminderTitle() {
        return Title.title(
                Component.text("GİRİŞ YAP!", NamedTextColor.GREEN),
                Component.text("/login <şifre> ile oynayama başlayabilirsin.", NamedTextColor.WHITE),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        );
    }
}