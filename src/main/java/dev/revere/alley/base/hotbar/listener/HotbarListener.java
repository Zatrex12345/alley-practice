package dev.revere.alley.base.hotbar.listener;

import dev.revere.alley.Alley;
import dev.revere.alley.base.hotbar.HotbarItem;
import dev.revere.alley.base.queue.menu.sub.RankedMenu;
import dev.revere.alley.feature.leaderboard.menu.LeaderboardMenu;
import dev.revere.alley.game.match.menu.CurrentMatchesMenu;
import dev.revere.alley.game.party.menu.duel.DuelOtherPartyMenu;
import dev.revere.alley.game.party.menu.event.PartyEventMenu;
import dev.revere.alley.profile.Profile;
import dev.revere.alley.util.chat.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
public class HotbarListener implements Listener {
    /*private final Map<UUID, Long> lastInteraction = new HashMap<>();
    protected final long COOLDOWN_TIME = 300;*/

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if ((action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        /*UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (this.lastInteraction.containsKey(playerId) && (currentTime - this.lastInteraction.get(playerId)) < this.COOLDOWN_TIME) {
            return;
        }

        this.lastInteraction.put(playerId, currentTime);*/

        ItemStack item = player.getItemInHand();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        Profile profile = Alley.getInstance().getProfileService().getProfile(player.getUniqueId());
        HotbarItem hotbarItem = Alley.getInstance().getHotbarService().getItemByStack(item);

        if (hotbarItem != null) {
            String command = hotbarItem.getHotbarItems().getCommand();
            if (command != null && !command.isEmpty()) {
                player.performCommand(command);
            } else {
                switch (profile.getState()) {
                    case LOBBY:
                        switch (hotbarItem.getHotbarItems()) {
                            case UNRANKED_QUEUES:
                                Alley.getInstance().getQueueService().getQueueMenu().openMenu(player);
                                break;
                            case RANKED_QUEUES:
                                new RankedMenu().openMenu(player);
                                break;
                            case CURRENT_MATCHES:
                                new CurrentMatchesMenu().openMenu(player);
                                break;
                            case KIT_EDITOR:
                                break;
                            case LEADERBOARD:
                                new LeaderboardMenu().openMenu(player);
                                break;
                            case START_PARTY_EVENT:
                                if (this.checkForPartyLeader(player, profile)) return;
                                new PartyEventMenu().openMenu(player);
                                break;
                            case FIGHT_OTHER_PARTY:
                                if (this.checkForPartyLeader(player, profile)) return;
                                new DuelOtherPartyMenu().openMenu(player);
                                break;
                        }
                        break;
                }
            }
        }
    }

    /**
     * Check if the player is the leader of the party.
     *
     * @param player  the player to check
     * @param profile the profile of the player
     * @return true if the player is not the leader of the party
     */
    private boolean checkForPartyLeader(Player player, Profile profile) {
        if (player != profile.getParty().getLeader()) {
            player.sendMessage(CC.translate("&cYou're not the leader of this party."));
            return true;
        }
        return false;
    }
}