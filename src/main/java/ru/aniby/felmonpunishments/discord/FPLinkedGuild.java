package ru.aniby.felmonpunishments.discord;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.aniby.felmonpunishments.FelmonPunishments;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FPLinkedGuild {
    @Getter
    private final @NotNull Guild guild;
    @Getter
    private final HashMap<String, TextChannel> channels = new HashMap<>();
    @Getter
    private final HashMap<Group, Role> groups = new HashMap<>();
    @Getter
    private final List<Role> revokeOnBan = new ArrayList<>();

    public FPLinkedGuild(@NotNull Guild guild) {
        this.guild = guild;

        this.initChannels();
        this.initGroups();
    }

    public boolean send(@NotNull String channelName, @NotNull String text, @Nullable EmbedBuilder embedBuilder) {
        TextChannel channel = channels.get(channelName);
        if (channel != null) {
            MessageCreateAction action = channel.sendMessage(text);
            if (embedBuilder != null)
                action = action.setEmbeds(embedBuilder.build());
            action.queue();
            return true;
        }
        return false;
    }

    public void initGroups() {
        List<String> revokeOnBanList = getGuildNode().getStringList("revokeOnBan");
        Map<String, Object> map = getGuildNode().getConfigurationSection("groups").getValues(false);
        for (String key : map.keySet()) {
            String value = (String) map.get(key);
            if (value != null) {
                Group primaryGroup = FelmonPunishments.getLuckPerms().getGroupManager().getGroup(key);
                Role role = this.guild.getRoleById(value);
                if (primaryGroup != null && role != null) {
                    this.groups.put(primaryGroup, role);
                    if (revokeOnBanList.contains(key))
                        revokeOnBan.add(role);
                }
            }
        }
    }

    public void initChannels() {
        Map<String, Object> map = getGuildNode().getConfigurationSection("channels").getValues(false);
        for (String key : map.keySet()) {
            String value = (String) map.get(key);
            if (value != null) {
                TextChannel channel = this.guild.getTextChannelById(value);
                if (channel != null)
                    this.channels.put(key, channel);
            }
        }
    }

    public ConfigurationSection getGuildNode() {
        return FelmonPunishments.getInstance().getConfig().getConfigurationSection("discord.server");
    }

    public @Nullable Role getRole(String primaryGroup) {
        Group group = this.groups.keySet().stream().filter(g -> g.getName().equals(primaryGroup)).findFirst().orElse(null);
        if (group == null)
            return null;
        return this.groups.get(group);
    }
}
