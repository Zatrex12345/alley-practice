package dev.revere.alley.game.match.menu;

import dev.revere.alley.Alley;
import dev.revere.alley.api.menu.Button;
import dev.revere.alley.api.menu.pagination.PaginatedMenu;
import dev.revere.alley.game.match.AbstractMatch;
import dev.revere.alley.tool.item.ItemBuilder;
import dev.revere.alley.util.chat.CC;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 */
public class CurrentMatchesMenu extends PaginatedMenu {
    /**
     * Gets the title of the menu.
     *
     * @param player the player viewing the menu
     * @return the title of the menu
     */
    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&b&lCurrent Matches (" + Alley.getInstance().getMatchService().getMatches().size() + ")";
    }

    /**
     * Gets the buttons to display in the menu.
     *
     * @param player the player viewing the menu
     * @return the buttons to display
     */
    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        final Map<Integer, Button> buttons = new ConcurrentHashMap<>();
        int slot = 0;

        for (AbstractMatch match : Alley.getInstance().getMatchService().getMatches()) {
            buttons.put(slot++, new CurrentMatchButton(match));
        }

        return buttons;
    }

    /**
     * Gets the buttons to display in the global section of the menu.
     *
     * @param player the player viewing the menu
     * @return the global buttons
     */
    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        addGlassHeader(buttons, (byte) 15);

        buttons.put(4, new RefreshButton());
        return buttons;
    }

    @RequiredArgsConstructor
    public static class CurrentMatchButton extends Button {
        private final AbstractMatch match;

        /**
         * Gets the item stack for the button.
         *
         * @param player the player viewing the button
         * @return the item stack
         */
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(match.getKit().getIcon()).name("&b&l" + match.getParticipants().get(0).getPlayer().getUsername() + " &7vs &b&l" + match.getParticipants().get(1).getPlayer().getUsername()).durability(match.getKit().getDurability()).hideMeta()
                    .lore(Arrays.asList(
                            " &f● &bArena: &f" + match.getArena().getName(),
                            " &f● &bKit: &f" + match.getKit().getName(),
                            " &f● &bQueue: &f" + (match.getQueue() == null ? "None" : match.getQueue().getQueueType()),
                            " ",
                            "&aClick to spectate!"
                    ))
                    .hideMeta().build();
        }

        /**
         * Handles the click event for the button.
         *
         * @param player       the player who clicked the button
         * @param slot         the slot the button was clicked in
         * @param clickType    the type of click
         * @param hotbarButton the hotbar button clicked
         */
        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (clickType != ClickType.LEFT) return;

            if (Alley.getInstance().getProfileService().getProfile(player.getUniqueId()).getMatch() != null) {
                player.sendMessage(CC.translate("&cYou can't spectate a match in your current state."));
                return;
            }

            match.addSpectator(player);
            playNeutral(player);
        }
    }

    @AllArgsConstructor
    public static class RefreshButton extends Button {
        /**
         * Gets the item stack for the button.
         *
         * @param player the player viewing the button
         * @return the item stack
         */
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.CARPET)
                    .name("&b&lRefresh")
                    .lore(" &f● &bPress to refresh the matches")
                    .durability(2)
                    .build();
        }

        /**
         * Handles the click event for the button.
         *
         * @param player       the player who clicked the button
         * @param slot         the slot the button was clicked in
         * @param clickType    the type of click
         * @param hotbarButton the hotbar button clicked
         */
        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (clickType != ClickType.LEFT) return;
            new CurrentMatchesMenu().openMenu(player);
            playNeutral(player);
        }
    }
}
