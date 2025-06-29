package dev.revere.alley.base.kit;

import dev.revere.alley.Alley;
import dev.revere.alley.base.kit.enums.EnumKitCategory;
import dev.revere.alley.base.kit.setting.KitSetting;
import dev.revere.alley.base.kit.setting.impl.mode.KitSettingRankedImpl;
import dev.revere.alley.base.queue.Queue;
import dev.revere.alley.tool.logger.Logger;
import dev.revere.alley.tool.serializer.Serializer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 19/05/2024 - 23:30
 */
@Getter
public class KitService {
    protected final Alley plugin;
    private final List<Kit> kits;

    /**
     * Constructor for the KitService class.
     *
     * @param plugin The main class.
     */
    public KitService(Alley plugin) {
        this.plugin = plugin;
        this.kits = new ArrayList<>();
        this.loadKits();
    }

    /**
     * Method to load all kits from the kits.yml file.
     */
    public void loadKits() {
        FileConfiguration config = this.plugin.getConfigService().getKitsConfig();
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) {
            return;
        }

        for (String name : kitsSection.getKeys(false)) {
            String key = "kits." + name;

            Kit kit = new Kit(
                    name,
                    config.getString(key + ".display-name"),
                    config.getString(key + ".description"),
                    config.getString(key + ".disclaimer"),
                    config.getBoolean(key + ".enabled"),
                    config.getBoolean(key + ".editable"),
                    EnumKitCategory.valueOf(config.getString(key + ".category")),
                    Material.matchMaterial(config.getString(key + ".icon.material")),
                    config.getInt(key + ".icon.durability"),
                    config.getInt(key + ".slots.unranked"),
                    config.getInt(key + ".slots.ranked"),
                    config.getInt(key + ".slots.editor"),
                    config.getInt(key + ".ffa.slot"),
                    config.getInt(key + ".slots.extra"),
                    Serializer.deserializeItemStack(config.getString(key + ".items")),
                    Serializer.deserializeItemStack(config.getString(key + ".armor")),
                    Serializer.deserializeItemStack(config.getString(key + ".editor-items"))
            );

            this.setupFFA(kit, config, key);
            this.loadKitSettings(config, key, kit);
            this.loadPotionEffects(config, key, kit);
            this.addMissingKitSettings(kit, config, key); // Only necessary for development purposes if you're lazy like me - to add the new kit setting manually.
            this.kits.add(kit);
            this.addKitToQueue(kit);
        }
    }

    /**
     * Method to setup the FFA settings of a kit.
     *
     * @param kit    The kit.
     * @param config The configuration file.
     * @param key    The path key.
     */
    private void setupFFA(Kit kit, FileConfiguration config, String key) {
        kit.setFfaEnabled(config.getBoolean(key + ".ffa.enabled"));
        kit.setFfaArenaName(config.getString(key + ".ffa.arena-name"));
        kit.setMaxFfaPlayers(config.getInt(key + ".ffa.max-players"));
        kit.setFfaSlot(config.getInt(key + ".ffa.slot"));
    }

    /**
     * Method to load the settings of a kit.
     *
     * @param config The configuration file.
     * @param key    The path key.
     * @param kit    The kit.
     */
    private void loadKitSettings(FileConfiguration config, String key, Kit kit) {
        ConfigurationSection settingsSection = config.getConfigurationSection(key + ".settings");
        if (settingsSection == null) {
            this.applyDefaultSettings(config, key, kit);
            return;
        }

        for (String settingName : settingsSection.getKeys(false)) {
            boolean enabled = settingsSection.getBoolean(settingName);

            KitSetting kitSetting = this.plugin.getKitSettingService().createSettingByName(settingName);
            if (kitSetting != null) {
                kitSetting.setEnabled(enabled);
                kit.addKitSetting(kitSetting);
            }
        }
    }

    /**
     * Method to load the potion effects of a kit.
     *
     * @param config The configuration file.
     * @param key    The path key.
     * @param kit    The kit.
     */
    private void loadPotionEffects(FileConfiguration config, String key, Kit kit) {
        try {
            List<PotionEffect> potionEffects = Serializer.deserializePotionEffects(config.getStringList(key + ".potion-effects"));
            kit.setPotionEffects(potionEffects);
        } catch (Exception exception) {
            Logger.logException("Failed to load potion effects for kit " + kit.getName() + ": " + exception.getMessage(), exception);
        }
    }

    /**
     * Handle creation in config for each kit that has missing settings (for development purposes).
     *
     * @param kit    The kit.
     * @param config The configuration file.
     * @param key    The path key.
     */
    private void addMissingKitSettings(Kit kit, FileConfiguration config, String key) {
        this.plugin.getKitSettingService().getSettings().forEach(setting -> {
            if (kit.getKitSettings().stream().noneMatch(kitSetting -> kitSetting.getName().equals(setting.getName()))) {
                kit.addKitSetting(setting);
                Logger.log("&cAdded missing kit setting to" + kit.getName() + ": " + setting.getName());
                this.saveKitSettings(config, key, kit);
            }
        });
    }

    /**
     * Method to save all kits to the kits.yml file.
     */
    public void saveKits() {
        for (Kit kit : this.kits) {
            FileConfiguration config = this.plugin.getConfigService().getKitsConfig();
            String key = "kits." + kit.getName();
            this.kitToConfig(kit, config, key);
            this.saveKitSettings(config, key, kit);
            this.savePotionEffects(config, key, kit);
            this.plugin.getConfigService().saveConfig(this.plugin.getConfigService().getConfigFile("storage/kits.yml"), config);
        }
    }

    /**
     * Method to save the settings of a kit.
     *
     * @param config The configuration file.
     * @param key    The path key.
     * @param kit    The kit.
     */
    private void saveKitSettings(FileConfiguration config, String key, Kit kit) {
        for (KitSetting kitSetting : kit.getKitSettings()) {
            config.set(key + ".settings." + kitSetting.getName(), kitSetting.isEnabled());
        }
    }

    /**
     * Method to save the potion effects of a kit.
     *
     * @param config The configuration file.
     * @param key    The path key.
     * @param kit    The kit.
     */
    private void savePotionEffects(FileConfiguration config, String key, Kit kit) {
        List<String> potionEffects = Serializer.serializePotionEffects(kit.getPotionEffects());
        config.set(key + ".potion-effects", potionEffects);
    }

    /**
     * Method to add a kit to the queue.
     *
     * @param kit The kit to add.
     */
    private void addKitToQueue(Kit kit) {
        if (!kit.isEnabled()) return;
        new Queue(kit, false);

        if (kit.isSettingEnabled(KitSettingRankedImpl.class)) {
            new Queue(kit, true);
        }
    }

    /**
     * Method to save a kit to the kits.yml file.
     *
     * @param kit The kit to save.
     */
    public void saveKit(Kit kit) {
        FileConfiguration config = this.plugin.getConfigService().getKitsConfig();
        String key = "kits." + kit.getName();
        this.kitToConfig(kit, config, key);

        if (kit.getKitSettings() == null) {
            this.applyDefaultSettings(config, key, kit);
        } else {
            this.saveKitSettings(config, key, kit);
        }

        if (kit.getPotionEffects() != null) {
            this.savePotionEffects(config, key, kit);
        }

        this.plugin.getConfigService().saveConfig(this.plugin.getConfigService().getConfigFile("storage/kits.yml"), config);
    }

    /**
     * Method to apply the default settings to a kit.
     *
     * @param config The configuration file.
     * @param key    The path key.
     * @param kit    The kit.
     */
    public void applyDefaultSettings(FileConfiguration config, String key, Kit kit) {
        this.plugin.getKitSettingService().getSettings().forEach(setting -> {
            kit.addKitSetting(setting);
            config.set(key + ".settings." + setting.getName(), setting.isEnabled());
        });

        this.plugin.getConfigService().saveConfig(this.plugin.getConfigService().getConfigFile("storage/kits.yml"), config);
    }


    /**
     * Method to create a kit.
     *
     * @param kitName   The name of the kit.
     * @param inventory The inventory of the kit.
     * @param armor     The armor of the kit.
     * @param icon      The icon of the kit.
     */
    public void createKit(String kitName, ItemStack[] inventory, ItemStack[] armor, Material icon, int slot) {
        Kit kit = new Kit(
                kitName,
                this.plugin.getConfigService().getSettingsConfig().getString("kit.default-values.display-name").replace("{kit-name}", kitName),
                this.plugin.getConfigService().getSettingsConfig().getString("kit.default-values.description").replace("{kit-name}", kitName),
                this.plugin.getConfigService().getSettingsConfig().getString("kit.default-values.disclaimer").replace("{kit-name}", kitName),
                true,
                true,
                EnumKitCategory.NORMAL,
                icon,
                0,
                slot,
                0,
                0,
                0,
                0,
                inventory,
                armor,
                new ItemStack[0]
        );

        this.plugin.getKitSettingService().applyAllSettingsToKit(kit);
        this.kits.add(kit);
        this.saveKit(kit);
    }

    /**
     * Method to delete a kit.
     *
     * @param kit The kit to delete.
     */
    public void deleteKit(Kit kit) {
        FileConfiguration config = this.plugin.getConfigService().getKitsConfig();
        File file = this.plugin.getConfigService().getConfigFile("storage/kits.yml");

        this.kits.remove(kit);
        config.set("kits." + kit.getName(), null);

        this.plugin.getConfigService().saveConfig(file, config);
    }

    /**
     * Method to get a kit by name.
     *
     * @param name The name of the kit.
     * @return The kit.
     */
    public Kit getKit(String name) {
        return this.kits.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Method to get a kit by its object.
     *
     * @param kit The kit instance.
     * @return The kit.
     */
    public Kit getKit(Kit kit) {
        return this.kits.stream().filter(k -> k.getName().equalsIgnoreCase(kit.getName())).findFirst().orElse(null);
    }

    /**
     * Method to save a kit to the configuration specified in the parameters with a given key path.
     *
     * @param kit    The kit to save.
     * @param config The configuration file.
     * @param key    The path key.
     */
    private void kitToConfig(Kit kit, FileConfiguration config, String key) {
        config.set(key + ".display-name", kit.getDisplayName());
        config.set(key + ".description", kit.getDescription());
        config.set(key + ".disclaimer", kit.getDisclaimer());
        config.set(key + ".enabled", kit.isEnabled());
        config.set(key + ".editable", kit.isEditable());
        config.set(key + ".category", kit.getCategory().name());
        config.set(key + ".icon.material", kit.getIcon().name());
        config.set(key + ".icon.durability", kit.getDurability());
        config.set(key + ".slots.unranked", kit.getUnrankedSlot());
        config.set(key + ".slots.ranked", kit.getRankedSlot());
        config.set(key + ".slots.editor", kit.getEditorSlot());
        config.set(key + ".slots.extra", kit.getExtraSlot());
        config.set(key + ".ffa.arena-name", kit.getFfaArenaName());
        config.set(key + ".ffa.enabled", kit.isFfaEnabled());
        config.set(key + ".ffa.slot", kit.getFfaSlot());
        config.set(key + ".ffa.max-players", kit.getMaxFfaPlayers());
        config.set(key + ".items", Serializer.serializeItemStack(kit.getItems()));
        config.set(key + ".armor", Serializer.serializeItemStack(kit.getArmor()));
        config.set(key + ".editor-items", Serializer.serializeItemStack(kit.getEditorItems()));
    }
}