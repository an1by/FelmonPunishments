package ru.aniby.msvelocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ru.aniby.msvelocity.commands.discord.LinkCommand;
import ru.aniby.msvelocity.database.MySQL;
import ru.aniby.msvelocity.punishment.ban.Ban;
import ru.aniby.msvelocity.punishment.ban.BanManager;
import ru.aniby.msvelocity.punishment.mute.Mute;
import ru.aniby.msvelocity.punishment.mute.MuteManager;

import java.util.Objects;

public class EventListener {
    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String username = player.getUsername();
        Mute mute = MuteManager.getPlayerMutes().stream().filter(
                m -> Objects.equals(m.getIntruder(), username)
        ).findAny().orElse(null);
        if (mute != null) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            player.sendMessage(mute.onChatMessage());
        }
    }

    @Subscribe
    public void onPlayer(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer previousServer = event.getPreviousServer().orElse(null);
        if (previousServer == null
                && !MSVelocity.getDiscordBot().isInLobbyLinking()) {
            Component component = LinkCommand.linking(player.getUsername());
            if (component != null) {
//                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                player.disconnect(component);
                return;
            }
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        String username = player.getUsername();
        RegisteredServer server = event.getOriginalServer();
        if (server != null) {
            String serverName = server.getServerInfo().getName();
            Ban ban = BanManager.getBan(username, serverName);
            if (ban != null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                ban.kickIntruder();
                return;
            }
        }


        RegisteredServer previousServer = event.getPreviousServer();
        if (previousServer != null
                && previousServer.getServerInfo().getName().equalsIgnoreCase("lobby")
                && MSVelocity.getDiscordBot().isInLobbyLinking()) {
            Component component = LinkCommand.linking(player.getUsername());
            if (component != null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                player.sendMessage(component);
                return;
            }
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPreConnect(LoginEvent event) {
        Player player = event.getPlayer();
        String username = player.getUsername();
        String serverName = "all";
        Ban ban = BanManager.getBan(username, serverName);
        if (ban != null) {
            event.setResult(ResultedEvent.ComponentResult.denied(
                    ban.getPunishmentMessage()
            ));
            return;
        }
    }

    @Subscribe
    public void onProxyShutdown(@NotNull ProxyShutdownEvent event) {
        MySQL.disconnect(MSVelocity.getDatabaseConnection());
    }
}
