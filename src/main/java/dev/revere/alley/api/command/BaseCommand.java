package dev.revere.alley.api.command;

import dev.revere.alley.Alley;
import dev.revere.alley.profile.Profile;
import dev.revere.alley.util.PlayerUtil;
import dev.revere.alley.util.chat.CC;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public abstract class BaseCommand {
    public final Alley plugin;

    /**
     * Constructor for the BaseCommand class.
     */
    public BaseCommand() {
        this.plugin = Alley.getInstance();
        this.plugin.getCommandFramework().registerCommands(this);
    }

    /**
     * Method to be called when a command is executed.
     *
     * @param command The command.
     */
    public abstract void onCommand(CommandArgs command);

    /**
     * Either fetches the profile of an online player or retrieves the offline profile.
     *
     * @param target The name of the player.
     * @param sender The command sender.
     * @return The profile of the player, or null if not found.
     */
    public Profile getOfflineProfile(String target, CommandSender sender) {
        OfflinePlayer offlinePlayer = PlayerUtil.getOfflinePlayerByName(target);
        if (offlinePlayer == null) {
            sender.sendMessage(CC.translate("&cThat player does not exist."));
            return null;
        }

        UUID uuid = offlinePlayer.getUniqueId();
        if (uuid == null) {
            sender.sendMessage(CC.translate("&cThat player is invalid."));
            return null;
        }

        Profile profile = this.plugin.getProfileService().getProfile(uuid);
        if (profile == null) {
            sender.sendMessage(CC.translate("&cThat player does not have a profile."));
            return null;
        }

        return profile;
    }
}