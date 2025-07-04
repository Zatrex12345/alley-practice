package dev.revere.alley.base.queue;

import dev.revere.alley.Alley;
import dev.revere.alley.base.hotbar.enums.EnumHotbarType;
import dev.revere.alley.base.kit.Kit;
import dev.revere.alley.profile.Profile;
import dev.revere.alley.profile.enums.EnumProfileState;
import dev.revere.alley.util.chat.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.UUID;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Getter
@Setter
public class Queue {
    private final LinkedList<QueueProfile> profiles;

    private final Kit kit;

    private final boolean ranked;
    private final long maxQueueTime;

    /**
     * Constructor for the Queue class.
     *
     * @param kit The kit associated with the queue.
     */
    public Queue(Kit kit, boolean ranked) {
        this.profiles = new LinkedList<>();
        this.kit = kit;
        this.ranked = ranked;
        this.maxQueueTime = 300000L; // 5 minutes
        Alley.getInstance().getQueueService().getQueues().add(this);
    }

    /**
     * Gets the amount of people playing that queue.
     *
     * @return The amount of people playing that queue.
     */
    public int getQueueFightCount() {
        return Alley.getInstance().getMatchService().getMatches().stream().filter(match -> match.getQueue().equals(this)).toArray().length;
    }

    /**
     * Gets the queue type.
     *
     * @return The queue type.
     */
    public String getQueueType() {
        return (this.ranked ? "Ranked" : "Unranked");
    }

    /**
     * Adds a player to the queue.
     *
     * @param player The player to add.
     */
    public void addPlayer(Player player, int elo) {
        UUID uuid = player.getUniqueId();

        if (this.profiles.stream().anyMatch(queueProfile -> queueProfile.getUuid().equals(uuid))) {
            player.sendMessage(CC.translate("&cYou're already in a queue."));
            return;
        }

        QueueProfile queueProfile = new QueueProfile(this, uuid);
        queueProfile.setElo(elo);

        Profile profile = Alley.getInstance().getProfileService().getProfile(uuid);
        profile.setQueueProfile(queueProfile);
        profile.setState(EnumProfileState.WAITING);

        this.profiles.add(queueProfile);

        player.sendMessage(CC.translate("&aYou've joined the &b" + queueProfile.getQueue().getKit().getName() + " &aqueue."));
    }

    /**
     * Removes a player from the queue.
     *
     * @param queueProfile The queue profile to remove.
     */
    public void removePlayer(QueueProfile queueProfile) {
        this.profiles.remove(queueProfile);

        Profile profile = Alley.getInstance().getProfileService().getProfile(queueProfile.getUuid());
        profile.setQueueProfile(null);
        profile.setState(EnumProfileState.LOBBY);

        Player player = Bukkit.getPlayer(queueProfile.getUuid());
        if (player == null) {
            return;
        }

        Alley.getInstance().getHotbarService().applyHotbarItems(player, EnumHotbarType.LOBBY);
        player.sendMessage(CC.translate("&cYou've left the queue."));
    }

    /**
     * Gets the profile of a player.
     *
     * @param uuid The UUID of the player.
     * @return The profile object.
     */
    public Profile getProfile(UUID uuid) {
        return Alley.getInstance().getProfileService().getProfile(uuid);
    }
}