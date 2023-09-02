package ru.aniby.felmonpunishments;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.aniby.felmonpunishments.punishment.ban.Ban;
import ru.aniby.felmonpunishments.punishment.ban.BanManager;
import ru.aniby.felmonpunishments.punishment.mute.Mute;
import ru.aniby.felmonpunishments.punishment.mute.MuteManager;

import java.util.Objects;

public class EventListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();
        Mute mute = MuteManager.getPlayerMutes().stream().filter(
                m -> Objects.equals(m.getIntruder(), username)
        ).findAny().orElse(null);
        if (mute != null) {
            event.setCancelled(true);
            player.sendMessage(mute.onChatMessage());
        }
    }

    @EventHandler
    public void onPlayerPreConnect(AsyncPlayerPreLoginEvent event) {
        String username = event.getName();
        
        Ban ban = BanManager.getBan(username);
        if (ban != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ban.getPunishmentMessage());
            return;
        }
    }
}
