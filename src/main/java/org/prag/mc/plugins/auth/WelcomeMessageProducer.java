package org.prag.mc.plugins.auth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class WelcomeMessageProducer {

    private static final String[] NEW_PLAYER_MESSAGES = {
            "Welcome to the club!",
            "A new legend has arrived!",
            "Glad to see you here, adventurer!"
    };

    private static final String[] REMIND_MESSAGES = {
            "Please register to secure your account.",
            "Security first! Register now.",
            "Your journey awaits, just one more step!"
    };

    public static String getRandomWelcome() {
        return NEW_PLAYER_MESSAGES[ThreadLocalRandom.current().nextInt(NEW_PLAYER_MESSAGES.length)];
    }

    public static String getRandomReminder() {
        return REMIND_MESSAGES[ThreadLocalRandom.current().nextInt(REMIND_MESSAGES.length)];
    }

    public static Title getNewPlayerTitle() {
        return Title.title(
                Component.text("WELCOME", NamedTextColor.AQUA),
                Component.text("Please /register <password> <password>", NamedTextColor.WHITE),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        );
    }

    public static Title getRegisterReminderTitle() {
        return Title.title(
                Component.text("REGISTER", NamedTextColor.RED),
                Component.text("Use /register <password> <password>", NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(250))
        );
    }

    public static Title getLoginReminderTitle() {
        return Title.title(
                Component.text("WELCOME BACK", NamedTextColor.GREEN),
                Component.text("Please /login <password> to start playing.", NamedTextColor.WHITE),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        );
    }
}