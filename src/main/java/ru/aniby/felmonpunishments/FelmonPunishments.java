package ru.aniby.felmonpunishments;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.aniby.felmonapi.configuration.FelmonConfigurator;
import ru.aniby.felmonapi.database.MySQL;
import ru.aniby.felmonpunishments.commands.FPCommand;
import ru.aniby.felmonpunishments.commands.offense.TicketCommand;
import ru.aniby.felmonpunishments.commands.offense.UnticketCommand;
import ru.aniby.felmonpunishments.commands.punishment.*;
import ru.aniby.felmonpunishments.configuration.FPMainConfig;
import ru.aniby.felmonpunishments.configuration.FPMessagesConfig;
import ru.aniby.felmonpunishments.configuration.FPPunishmentsConfig;
import ru.aniby.felmonpunishments.discord.DiscordUtils;
import ru.aniby.felmonpunishments.punishment.PunishmentTimerTask;
import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;
import ru.aniby.felmonpunishments.punishment.warn.WarnManager;

import java.sql.Connection;
import java.util.*;

public final class FelmonPunishments extends JavaPlugin {
    @Getter
    private static final String overridePermission = "felmonpunishments.punishment.override";
    @Getter
    private static FelmonPunishments instance;
    @Getter
    private static Connection databaseConnection;
    @Getter
    private static LuckPerms luckPerms;
    @Getter
    private static List<FPCommand> commands;
    @Getter
    private static Timer punishmentTimer;
    @Getter
    private static final HashSet<String> punishedPlayers = new HashSet<>();

    public static @Nullable FPCommand getFPCommand(@NotNull String name) {
        return commands.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public void onEnable() {
        instance = this;

        // LuckPerms
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException exception) {
            exception.printStackTrace();
            getLogger().warning("LuckPerms can't be loaded! Disabling...");
            return;
        }

        // Config
        FelmonConfigurator mainConfig = new FelmonConfigurator("config.yml", instance, FPMainConfig.class);
        mainConfig.saveDefault(false);
        mainConfig.load();

        FelmonConfigurator messagesConfig = new FelmonConfigurator("messages.yml", instance, FPMessagesConfig.class);
        messagesConfig.saveDefault(false);
        messagesConfig.load();

        FelmonConfigurator punishmentsConfig = new FelmonConfigurator("punishments.yml", instance, FPPunishmentsConfig.class);
        punishmentsConfig.saveDefault(false);
        punishmentsConfig.load();

        // Database
        databaseConnection = MySQL.connect(
                FPMainConfig.MySQL.host,
                FPMainConfig.MySQL.database,
                FPMainConfig.MySQL.user,
                FPMainConfig.MySQL.password
        );
        if (databaseConnection == null) {
            instance.getLogger().warning("MySQL database (" + FPMainConfig.MySQL.database + ") can't be loaded! Stopping...");
            return;
        }

        if (FPPunishmentsConfig.Mute.enabled) {
            instance.getLogger().info("Mutes loading...");
            MuteManager.createTableIfNotExists();
            MuteManager.load();
            instance.getLogger().info("Mutes loaded!");
        }
        if (FPPunishmentsConfig.Permban.enabled || FPPunishmentsConfig.Tempban.enabled){
            instance.getLogger().info("Bans loading...");
            BanManager.createTableIfNotExists();
            BanManager.load();
            instance.getLogger().info("Bans loaded!");
        }
        if (FPPunishmentsConfig.Fine.enabled || FPPunishmentsConfig.Tempwarn.enabled || FPPunishmentsConfig.Warn.enabled || FPPunishmentsConfig.Ticket.enabled){
            instance.getLogger().info("Warns loading...");
            WarnManager.createTableIfNotExists();
            WarnManager.load();
            instance.getLogger().info("Warns loaded!");
        }

        // Discord
        DiscordUtils.start();

        // Commands
        commands = new ArrayList<>(Arrays.asList(
                new PunishmentsCommand(),

                new TicketCommand(),
                new UnticketCommand(),

                new UnbanCommand(),
                new UnwarnCommand(),
                new UnmuteCommand(),

                new MuteCommand(),
                new WarnCommand(),
                new TempwarnCommand(),
                new FineCommand(),
                new PermbanCommand(),
                new TempbanCommand()
        ));
        for (FPCommand command : commands) {
            PluginCommand pluginCommand = instance.getCommand(command.getName());
            if (pluginCommand != null)
                pluginCommand.setExecutor(command);
        }
        instance.getLogger().info("Commands loaded!");

        // Timer
        TimerTask timerTask = new PunishmentTimerTask();
        punishmentTimer = new Timer(true);
        punishmentTimer.scheduleAtFixedRate(timerTask, 0, 10*1000);
        instance.getLogger().info("Timer started!");

        // Events & Timers
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getLogger().info("Events loaded!");

    }

    @Override
    public void onDisable() {
        // Timer
        if (punishmentTimer != null)
            punishmentTimer.cancel();

        // Database
        MySQL.disconnect(databaseConnection);
    }
}
