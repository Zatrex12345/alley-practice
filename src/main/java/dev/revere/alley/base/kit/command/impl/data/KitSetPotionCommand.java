package dev.revere.alley.base.kit.command.impl.data;

import dev.revere.alley.api.command.BaseCommand;
import dev.revere.alley.api.command.CommandArgs;
import dev.revere.alley.api.command.annotation.CommandData;
import dev.revere.alley.base.kit.Kit;
import dev.revere.alley.base.kit.KitService;
import dev.revere.alley.config.locale.impl.KitLocale;
import dev.revere.alley.util.chat.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 14/03/2025
 */
public class KitSetPotionCommand extends BaseCommand {

    @CommandData(name = "kit.setpotion", aliases = {"kit.setpotioneffects"}, isAdminOnly = true)
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 1) {
            player.sendMessage(CC.translate("&6Usage: &e/kit setpotion &b<kitName>"));
            return;
        }

        KitService kitService = this.plugin.getKitService();
        Kit kit = kitService.getKit(args[0]);
        if (kit == null) {
            player.sendMessage(CC.translate(KitLocale.KIT_NOT_FOUND.getMessage()));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInHand();
        if (itemInHand == null || itemInHand.getType() != Material.POTION) {
            player.sendMessage(CC.translate("&cYou must hold a potion bottle to set effects for this kit!"));
            return;
        }

        if (!(itemInHand.getItemMeta() instanceof PotionMeta)) {
            player.sendMessage(CC.translate("&cInvalid potion!"));
            return;
        }

        PotionMeta potionMeta = (PotionMeta) itemInHand.getItemMeta();
        List<PotionEffect> effects = potionMeta.getCustomEffects();
        if (effects.isEmpty()) {
            player.sendMessage(CC.translate("&cThe potion you are holding has no custom effects!"));
            return;
        }

        effects.forEach(effect -> kit.getPotionEffects().add(effect));
        kitService.saveKit(kit);
        player.sendMessage(CC.translate(KitLocale.KIT_POTION_EFFECTS_SET.getMessage()).replace("{kit-name}", kit.getName()));
    }
}