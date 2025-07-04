package dev.revere.alley.command.impl.main.impl;

import dev.revere.alley.api.command.BaseCommand;
import dev.revere.alley.api.command.CommandArgs;
import dev.revere.alley.api.command.annotation.CommandData;
import dev.revere.alley.profile.Profile;
import dev.revere.alley.util.chat.CC;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Emmy
 * @project Alley
 * @date 30/05/2024 - 12:15
 */
public class AlleyDebugCommand extends BaseCommand {

    @Override
    @CommandData(name = "alley.debug", isAdminOnly = true, usage = "/alley debug <memory/instance/profile/profileData>", description = "Displays debug information for development purposes.")
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        Profile profile = this.plugin.getProfileService().getProfile(player.getUniqueId());

        if (args.length < 1) {
            player.sendMessage(CC.translate("&6Usage: &e/alley debug &b<memory/instance/profile/profiledata>"));
            return;
        }

        switch (args[0]) {
            case "memory":
                this.sendMemoryInfo(player);
                break;
            case "instance":
                this.sendInstanceInfo(player);
                break;
            case "profile":
                this.sendProfileInfo(profile, player);
                break;
            case "profiledata":
                this.sendProfileData(profile, player);
                break;
            default:
                player.sendMessage(CC.translate("&6Usage: &e/alley debug &b<memory/instance/profile/profileData>"));
                break;
        }
    }

    /**
     * Sends memory information to the player.
     *
     * @param player The player to send the information to.
     */
    private void sendMemoryInfo(Player player) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;

        Arrays.asList(
                "",
                "     &b&lAlley &7┃ &fMemory Information",
                "      &f┃ Max Memory: &b" + this.formatNumber((int) (maxMemory / 1024 / 1024)) + "MB",
                "      &f┃ Allocated Memory: &b" + this.formatNumber((int) (allocatedMemory / 1024 / 1024)) + "MB",
                "      &f┃ Free Memory: &b" + this.formatNumber((int) (freeMemory / 1024 / 1024)) + "MB",
                "      &f┃ Used Memory: &b" + this.formatNumber((int) (usedMemory / 1024 / 1024)) + "MB",
                ""
        ).forEach(line -> player.sendMessage(CC.translate(line)));
    }

    /**
     * Sends instance information to the player.
     *
     * @param player The player to send the information to.
     */
    private void sendInstanceInfo(Player player) {
        Arrays.asList(
                "",
                "     &b&lAlley &7┃ &fInstance Information",
                "      &f┃ Profiles: &b" + this.formatNumber(this.plugin.getProfileService().getProfiles().size()),
                "      &f┃ Matches: &b" + this.formatNumber(this.plugin.getMatchService().getMatches().size()),
                "      &f┃ Queues: &b" + this.formatNumber(this.plugin.getQueueService().getQueues().size()),
                "      &f┃ Queue profiles: &b" + this.formatNumber(Arrays.stream(this.plugin.getQueueService().getQueues().stream().mapToInt(queue -> queue.getProfiles().size()).toArray()).sum()),
                "      &f┃ Cooldowns: &b" + this.formatNumber(this.plugin.getCooldownRepository().getCooldowns().size()),
                "      &f┃ Active Cooldowns: &b" + this.formatNumber((int) this.plugin.getCooldownRepository().getCooldowns().stream().filter(cooldown -> cooldown.getC().isActive()).count()),
                "      &f┃ Combats: &b" + this.formatNumber(this.plugin.getCombatService().getCombatMap().size()),
                "      &f┃ Kits: &b" + this.formatNumber(this.plugin.getKitService().getKits().size()),
                "      &f┃ Kit Settings: &b" + this.formatNumber(this.plugin.getKitSettingService().getSettings().size()),
                "      &f┃ Parties: &b" + this.formatNumber(this.plugin.getPartyService().getParties().size()),
                "      &f┃ Arenas: &b" + this.formatNumber(this.plugin.getArenaService().getArenas().size()),
                "      &f┃ Snapshots: &b" + this.formatNumber(this.plugin.getSnapshotRepository().getSnapshots().size()),
                "      &f┃ Duel Requests: &b" + this.formatNumber(this.plugin.getDuelRequestService().getDuelRequests().size()),
                "      &f┃ Emojis: &b" + this.formatNumber(this.plugin.getEmojiRepository().getEmojis().size()),
                ""
        ).forEach(line -> player.sendMessage(CC.translate(line)));
    }

    /**
     * Sends profile information to the player.
     *
     * @param profile The profile to send the information for.
     * @param player  The player to send the information to.
     */
    private void sendProfileInfo(Profile profile, Player player) {
        String banned = profile.getProfileData().isRankedBanned() ? "&c&lBANNED" : "&a&lNOT BANNED";
        Arrays.asList(
                "",
                "     &b&lProfile &7┃ &f" + profile.getName(),
                "      &f┃ UUID: &b" + profile.getUuid(),
                "      &f┃ Elo: &b" + this.formatNumber(profile.getProfileData().getElo()),
                "      &f┃ Coins: &b" + this.formatNumber(profile.getProfileData().getCoins()),
                "      &f┃ State: &b" + profile.getState() + " &7(" + profile.getState().getDescription() + ")",
                "      &f┃ Queue Profile: &b" + (profile.getQueueProfile() != null ? profile.getQueueProfile().getQueue().getKit().getName() : "&c&lNULL"),
                "      &f┃ Ranked: &b" + banned,
                ""
        ).forEach(line -> player.sendMessage(CC.translate(line).replace("The player", profile.getName())));
    }

    /**
     * Sends profile data to the player.
     *
     * @param profile The profile to send the data for.
     * @param player  The player to send the data to.
     */
    private void sendProfileData(Profile profile, Player player) {
        Arrays.asList(
                "",
                "     &b&lProfile Data &7┃ &f" + profile.getName(),
                "      &f┃ Unranked Wins: &b" + this.formatNumber(profile.getProfileData().getUnrankedWins()),
                "      &f┃ Unranked Losses: &b" + this.formatNumber(profile.getProfileData().getUnrankedLosses()),
                "      &f┃ Ranked Wins: &b" + this.formatNumber(profile.getProfileData().getRankedWins()),
                "      &f┃ Ranked Losses: &b" + this.formatNumber(profile.getProfileData().getRankedLosses()),
                "      &f┃ Total FFA Kills: &b" + this.formatNumber(profile.getProfileData().getTotalFFAKills()),
                "      &f┃ Total FFA Deaths: &b" + this.formatNumber(profile.getProfileData().getTotalFFADeaths()),
                ""
        ).forEach(line -> player.sendMessage(CC.translate(line)));
    }

    /**
     * Formats a number with commas.
     *
     * @param number The number to format
     * @return The formatted number
     */
    private String formatNumber(int number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }
}