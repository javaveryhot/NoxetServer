package org.noxet.noxetserver.playerstate;

import org.bukkit.GameMode;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.noxet.noxetserver.NoxetServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerState {
    private enum PlayerStateType {
        SMP,
        ANARCHY
    }

    /**
     * Gets the PlayerStates directory, which contains all the saved states of players.
     * @return The "PlayerStates" directory inside the plugin directory
     */
    private static File getDirectory() {
        File playerStateDir = new File(NoxetServer.getPlugin().getPluginDirectory(), "PlayerStates");

        if(!playerStateDir.mkdir() && (!playerStateDir.exists() || !playerStateDir.isDirectory()))
            throw new RuntimeException("Cannot create PlayerStates directory.");

        return playerStateDir;
    }

    /**
     * Gets the state file of a player. The state file can contain multiple sections of multiple states for different worlds/events.
     * @param player The player whose state should be given
     * @return The player's state file
     */
    private static File getStateFile(Player player) {
        return new File(getDirectory(), player.getUniqueId() + ".yml");
    }

    /**
     * Reads the player's state file into a YAML configuration.
     * @param player The player that the configuration belongs to
     * @return The saved YAML configuration if found, otherwise a blank configuration
     */
    private static YamlConfiguration getConfig(Player player) {
        File stateFile = getStateFile(player);

        if(!stateFile.exists())
            return new YamlConfiguration();

        return YamlConfiguration.loadConfiguration(stateFile);
    }

    /**
     * Get a certain state section from a player state config. Each state section has its own properties.
     * @param config The config to get the state section from
     * @param stateType What section to return
     * @return The saved state if found, otherwise create it and return a blank section
     */
    private static ConfigurationSection getConfigSection(YamlConfiguration config, PlayerStateType stateType) {
        ConfigurationSection stateSection = config.getConfigurationSection(stateType.name());

        if(stateSection == null)
            stateSection = config.createSection(stateType.name());

        return stateSection;
    }

    /**
     * Restore a player's state.
     * @param player The player to restore
     * @param stateType The state to restore from
     */
    public static void restoreState(Player player, PlayerStateType stateType) {
        if(!player.isOnline()) return;

        prepareNormal(player, true);

        PSPManager.restoreFromConfiguration(
                getConfigSection(
                        getConfig(player), stateType
                ), player
        );
    }

    /**
     * Saves the player's current state in-game (inventory, health, etc.) to the given YAML configuration. This does NOT mean that the file is saved!
     * It must be saved to the file after saving to the config using this method.
     * @param config The config to save the player's current state to
     * @param player The player whose state should be saved
     * @param stateType What state type/section of the config it should be saved to
     */
    private static void saveStateToConfig(YamlConfiguration config, Player player, PlayerStateType stateType) {
        PSPManager.addToConfiguration(getConfigSection(config, stateType), player);
    }

    /**
     * Saves the player's current state to their own state file (saved to plugins/NoxetServer/PlayerStates/uuid.yml where uuid is the player's UUID).
     * @param player The player whose state should be saved to disk
     * @param stateType What state section to save it to
     * @throws IOException The configuration saving failed
     */
    public static void saveState(Player player, PlayerStateType stateType) throws IOException {
        YamlConfiguration config = getConfig(player);

        saveStateToConfig(config, player, stateType);

        config.save(getStateFile(player));
    }

    /**
     * Reset a player's state.
     * @param player The player to reset
     */
    private static void prepareDefault(Player player) {
        if(player.isDead())
            player.spigot().respawn();

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);

        player.setLevel(0);
        player.setExp(0);

        player.setFireTicks(0);
        player.setHealthScaled(false);

        player.getInventory().clear();

        for(PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());

        Iterator<Advancement> advancementIterator = NoxetServer.getPlugin().getServer().advancementIterator();

        while(advancementIterator.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(advancementIterator.next());
            for(String criteria : progress.getAwardedCriteria())
                progress.revokeCriteria(criteria); // Remove all advancement criteria, resetting the advancement state.
        }
    }

    /**
     * Set a player in the "normal" mode (vulnerable, visible, not flying).
     * @param player The player to prepare to normal mode
     * @param inherit Whether to reset the player completely
     */
    public static void prepareNormal(Player player, boolean inherit) {
        if(inherit)
            prepareDefault(player);

        player.setInvulnerable(false);
        player.setInvisible(false);

        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFallDistance(0);
    }

    /**
     * Set a player in the "idle" mode (invulnerable, invisible, flying).
     * @param player The player to prepare to idle mode
     * @param inherit Whether to reset the player completely
     */
    public static void prepareIdle(Player player, boolean inherit) {
        if(inherit)
            prepareDefault(player);

        player.setInvulnerable(true);
        player.setInvisible(true);

        player.setAllowFlight(true);
        player.setFlying(true);
    }

    /**
     * Gets the advancement criteria for a player. Used for saving player states.
     * @param player The player to get the criteria from
     * @return The advancement criteria as String[] (serializable to config)
     */
    public static String[] getAdvancementCriteriaList(Player player) {
        List<String> criteriaListTemp = new ArrayList<>();
        Iterator<Advancement> advancementIterator = NoxetServer.getPlugin().getServer().advancementIterator();

        while(advancementIterator.hasNext())
            criteriaListTemp.addAll(
                    player.getAdvancementProgress( // Advancement progression for player.
                            advancementIterator.next() // This advancement.
                    ).getAwardedCriteria() // Get requirements for the advancement.
            );

        return criteriaListTemp.toArray(new String[] {});
    }
}
