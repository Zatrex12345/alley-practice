package dev.revere.alley.provider.scoreboard.impl.match;

import dev.revere.alley.game.match.player.impl.MatchGamePlayerImpl;
import dev.revere.alley.game.match.player.participant.GameParticipant;
import dev.revere.alley.profile.Profile;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 30/04/2025
 */
public interface IMatchScoreboard {
    /**
     * Gets the scoreboard lines for the given profile in a regular solo match.
     *
     * @param profile  The profile to get the scoreboard lines for.
     * @param player   The player whose scoreboard is being displayed.
     * @param you      The player whose scoreboard is being displayed.
     * @param opponent The opponent player.
     * @return The scoreboard lines.
     */
    List<String> getLines(Profile profile, Player player, GameParticipant<MatchGamePlayerImpl> you, GameParticipant<MatchGamePlayerImpl> opponent);

    /**
     * Gets the corresponding color of the player including the player's name.
     *
     * @param profile The profile of the player.
     * @return The formatted player name with color.
     */
    default String getColoredName(Profile profile) {
        return profile.getNameColor() + profile.getName();
    }

    /**
     * Gets the ping of the player by using reflection.
     *
     * @param player The player to get the ping for.
     * @return The ping of the player.
     */
    default int getPing(Player player) {
        if (player == null) {
            return 0;
        }

        return ((CraftPlayer) player).getHandle().ping;
    }
}