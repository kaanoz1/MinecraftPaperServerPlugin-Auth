package org.prag.mc.plugins.auth;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.prag.mc.plugins.auth.AnsiConstants.Utils;
import org.prag.mc.plugins.auth.Cache.PlayerStateCache;
import org.prag.mc.plugins.auth.Commands.LoginCommand;
import org.prag.mc.plugins.auth.Commands.RegisterCommand;
import org.prag.mc.plugins.auth.Listener.AuthListener;
import org.prag.mc.plugins.auth.Repositories.AuthRepository;

public final class Auth extends JavaPlugin {

    @Override
    public void onEnable() {
        AuthRepository authRepository = new AuthRepository();
        PlayerStateCache playersStateCache = new PlayerStateCache();
        getServer().getServicesManager().register(AuthRepository.class, authRepository, this, ServicePriority.High);

        getServer().getPluginManager().registerEvents(new AuthListener(playersStateCache), this);


        var loginCommand = getCommand("login");

        if (loginCommand != null)
            loginCommand.setExecutor(new LoginCommand(playersStateCache));

        var registerCommand = getCommand("register");

        if (registerCommand != null)
            registerCommand.setExecutor(new RegisterCommand(playersStateCache));


        getLogger().info(Utils.ANSI_GREEN + "Auth plugin is enabled!" + Utils.ANSI_RESET);

    }

    @Override
    public void onDisable() {
        getLogger().info(Utils.ANSI_RED + "Auth plugin is disabled!" + Utils.ANSI_RESET);
    }
}
