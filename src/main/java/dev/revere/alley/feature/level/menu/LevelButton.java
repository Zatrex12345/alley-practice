package dev.revere.alley.feature.level.menu;

import dev.revere.alley.api.menu.Button;
import dev.revere.alley.feature.level.data.LevelData;
import dev.revere.alley.profile.Profile;
import dev.revere.alley.tool.item.ItemBuilder;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Emmy
 * @project Alley
 * @since 22/04/2025
 */
@AllArgsConstructor
public class LevelButton extends Button {
    private final Profile profile;
    private final LevelData level;

    @Override
    public ItemStack getButtonItem(Player player) {
        if (this.profile.getProfileData().getElo() >= this.level.getMinElo()) {
            return new ItemBuilder(this.level.getMaterial())
                    .name(this.level.getDisplayName())
                    .lore(
                            "",
                            "&a&lUNLOCKED"
                    )
                    .durability(this.level.getDurability())
                    .hideMeta()
                    .build();
        }

        int requiredElo = this.level.getMinElo() - this.profile.getProfileData().getElo();

        return new ItemBuilder(Material.STAINED_GLASS_PANE)
                .name(this.level.getDisplayName())
                .lore(
                        "",
                        "&c&lLOCKED",
                        "",
                        "&fUnlock with &b" + requiredElo + " &fmore Elo!",
                        "&fCurrent Elo: &b" + this.profile.getProfileData().getElo()
                )
                .durability(14)
                .hideMeta()
                .build();
    }
}