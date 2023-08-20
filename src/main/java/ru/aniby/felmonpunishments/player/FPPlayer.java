package ru.aniby.felmonpunishments.player;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.aniby.felmonpunishments.FelmonPunishments;
import ru.aniby.felmonpunishments.discord.DiscordUtils;
import ru.aniby.felmonpunishments.discord.FPLinkedGuild;

import java.util.Objects;

public class FPPlayer {
    @Getter
    private final @NotNull String username;

    public @Nullable User getLuckpermsUser() {
        return FelmonPunishments.getLuckPerms().getUserManager().getLoadedUsers()
                .stream().filter(u -> Objects.equals(u.getUsername(), username))
                .findFirst().orElse(null);
    }

    public int getWeight() {
        User user = getLuckpermsUser();
        if (user != null) {
            String primaryGroup = user.getPrimaryGroup();
            Group group = FelmonPunishments.getLuckPerms().getGroupManager().getGroup(primaryGroup);
            if (group != null) {
                return group.getWeight().orElse(0);
            }
        }
        return 0;
    }

    public static FPPlayer get(@Nullable String username) {
        return username == null ? null : new FPPlayer(username);
    }

    private FPPlayer(@NotNull String username) {
        this.username = username;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(username);
    }

    public @Nullable OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(username);
    }

    public @Nullable net.dv8tion.jda.api.entities.User getDiscordUser() {
        String id = getDiscordId();
        if (id != null)
            return DiscordUtils.getJDA().retrieveUserById(id).complete();
        return null;
    }

    public @Nullable String getDiscordId() {
        OfflinePlayer player = getOfflinePlayer();
        if (player != null) {
            AccountLinkManager accountLinkManager = DiscordSRV.getPlugin().getAccountLinkManager();
            if (accountLinkManager != null)
                return accountLinkManager.getDiscordId(player.getUniqueId());
        }
        return null;
    }

    public void punishWithRole(@NotNull String roleName) {
        Bukkit.getScheduler().runTaskAsynchronously(FelmonPunishments.getInstance(),
                () -> {
                    net.dv8tion.jda.api.entities.User discordUser = getDiscordUser();
                    if (discordUser != null) {
                        FPLinkedGuild linkedGuild = DiscordUtils.getLinkedGuild();
                        if (linkedGuild != null) {
                            Member member = linkedGuild.getGuild().getMember(discordUser);
                            if (member != null) {
                                try {
                                    Role punishRole = linkedGuild.getRole(roleName);
                                    if (punishRole != null)
                                        linkedGuild.getGuild().addRoleToMember(member, punishRole).queue();
                                    if (roleName.equalsIgnoreCase("ban"))
                                        for (Role role : linkedGuild.getRevokeOnBan())
                                            linkedGuild.getGuild().removeRoleFromMember(member, role).queue();
                                } catch (HierarchyException exception) {
                                    FelmonPunishments.getInstance().getLogger().info(
                                            "Заблокирована смена ролей при бане/муте игрока " + username
                                    );
                                }
                            }
                        }
                    }
                }
        );
    }

    public void revokePunishWithRole(@NotNull String roleName) {
        Bukkit.getScheduler().runTaskAsynchronously(FelmonPunishments.getInstance(),
                () -> {
                    net.dv8tion.jda.api.entities.User discordUser = getDiscordUser();
                    if (discordUser != null) {
                        FPLinkedGuild linkedGuild = DiscordUtils.getLinkedGuild();
                        if (linkedGuild != null) {
                            Member member = linkedGuild.getGuild().getMember(discordUser);
                            if (member != null) {
                                try {
                                    Role punishRole = linkedGuild.getRole(roleName);
                                    if (punishRole != null)
                                        linkedGuild.getGuild().removeRoleFromMember(member, punishRole).queue();
                                    if (roleName.equalsIgnoreCase("ban")) {
                                        Role playerRole = linkedGuild.getRole("player");
                                        if (playerRole != null)
                                            linkedGuild.getGuild().addRoleToMember(member, playerRole).queue();
                                    }
                                } catch (HierarchyException exception) {
                                    FelmonPunishments.getInstance().getLogger().info(
                                            "Заблокирована смена ролей при разбане/размуте игрока " + username
                                    );
                                }
                            }
                        }
                    }
                }
        );
    }
}
