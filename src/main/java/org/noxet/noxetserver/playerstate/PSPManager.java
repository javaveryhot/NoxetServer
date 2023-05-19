package org.noxet.noxetserver.playerstate;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.playerstate.properties.*;

import java.lang.reflect.InvocationTargetException;

public class PSPManager {
    private static final Class<?>[] propertyClassList = {
            PSPAdvancementCriteria.class, PSPAllowFlight.class, PSPExperienceLevel.class, PSPExperienceProgress.class, PSPFlying.class, PSPFoodLevel.class, PSPGameMode.class, PSPHealth.class, PSPInvisible.class, PSPInvulnerable.class, PSPLocation.class, PSPPlayerInventory.class, PSPPotionEffects.class, PSPVelocity.class
    };

    @SuppressWarnings("unchecked")
    public static void addToConfiguration(ConfigurationSection configSection, Player player) {
        for(Class<?> propertyClass : propertyClassList) {
            Class<? extends PlayerStateProperty> playerStatePropertyClass = (Class<? extends PlayerStateProperty>) propertyClass;
            try {
                playerStatePropertyClass.getDeclaredConstructor().newInstance().addToConfiguration(configSection, player);
            } catch (InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void restoreFromConfiguration(ConfigurationSection configSection, Player player) {
        for(Class<?> propertyClass : propertyClassList) {
            Class<? extends PlayerStateProperty> playerStatePropertyClass = (Class<? extends PlayerStateProperty>) propertyClass;
            try {
                playerStatePropertyClass.getDeclaredConstructor().newInstance().restoreProperty(configSection, player);
            } catch (InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}