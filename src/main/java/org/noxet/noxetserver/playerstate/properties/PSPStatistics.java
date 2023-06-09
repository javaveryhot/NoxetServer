package org.noxet.noxetserver.playerstate.properties;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.noxet.noxetserver.playerstate.PlayerStateProperty;

import java.util.HashMap;
import java.util.Map;

public class PSPStatistics implements PlayerStateProperty<Map<String, Object>> {
    @Override
    public String getConfigName() {
        return "statistics";
    }

    @Override
    public Map<String, Object> getDefaultSerializedProperty() {
        Map<String, Object> allPlayerStatistics = new HashMap<>();

        allPlayerStatistics.put("untyped", new HashMap<String, Integer>());
        allPlayerStatistics.put("material", new HashMap<String, Map<String, Integer>>());
        allPlayerStatistics.put("entity", new HashMap<String, Map<String, Integer>>());

        return allPlayerStatistics;
    }

    @Override
    public Map<String, Object> getSerializedPropertyFromPlayer(Player player) {
        Map<String, Integer> untypedPlayerStatistics = new HashMap<>();
        Map<String, Map<String, Integer>> materialPlayerStatistics = new HashMap<>();
        Map<String, Map<String, Integer>> entityPlayerStatistics = new HashMap<>();

        for(Statistic statistic : Statistic.values()) {
            switch(statistic.getType()) {
                case UNTYPED: // Basic (untyped) statistics.
                    untypedPlayerStatistics.put(statistic.name(), player.getStatistic(statistic));
                    break;
                case BLOCK:
                case ITEM: // Material specific statistics.
                    materialPlayerStatistics.put(statistic.name(), new HashMap<>());

                    int materialStatistic;
                    for(Material material : Material.values())
                        if((materialStatistic = player.getStatistic(statistic, material)) != 0)
                            materialPlayerStatistics.get(statistic.name()).put(material.name(), materialStatistic);
                    break;
                case ENTITY: // Entity specific statistics.
                    entityPlayerStatistics.put(statistic.name(), new HashMap<>());

                    int entityStatistic;
                    for(EntityType entityType : EntityType.values()) {
                        try {
                            if((entityStatistic = player.getStatistic(statistic, entityType)) != 0)
                                entityPlayerStatistics.get(statistic.name()).put(entityType.name(), entityStatistic);
                        } catch(IllegalArgumentException e) {
                            // This is expected. I don't know how else to check whether an entity is statistical.
                        }
                    }
                    break;
            }
        }

        Map<String, Object> allPlayerStatistics = new HashMap<>();

        allPlayerStatistics.put("untyped", untypedPlayerStatistics);
        allPlayerStatistics.put("material", materialPlayerStatistics);
        allPlayerStatistics.put("entity", entityPlayerStatistics);

        return allPlayerStatistics;
    }

    private static Map<?, ?> getMapSafely(Map<String, ?> map, String key) {
        Object value = map.get(key);

        if(value instanceof MemorySection)
            value = ((MemorySection) value).getValues(true);

        return (Map<?, ?>) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreProperty(Player player, Map<String, Object> statistics) {
        Map<String, Object> untypedPlayerStatistics, materialPlayerStatistics, entityPlayerStatistics;

        untypedPlayerStatistics = (Map<String, Object>) getMapSafely(statistics, "untyped");
        materialPlayerStatistics = (Map<String, Object>) getMapSafely(statistics, "material");
        entityPlayerStatistics = (Map<String, Object>) getMapSafely(statistics, "entity");

        for(Statistic statistic : Statistic.values()) {
            switch(statistic.getType()) {
                case UNTYPED:
                    player.setStatistic(statistic, (int) untypedPlayerStatistics.getOrDefault(statistic.name(), 0));
                    break;
                case BLOCK:
                case ITEM:
                    if(materialPlayerStatistics.containsKey(statistic.name()))
                        for(Material material : Material.values())
                            player.setStatistic(statistic, material, ((Map<String, Integer>) getMapSafely(materialPlayerStatistics, statistic.name())).getOrDefault(material.name(), 0));
                    break;
                case ENTITY:
                    if(entityPlayerStatistics.containsKey(statistic.name()))
                        for(EntityType entityType : EntityType.values()) {
                            try {
                                player.setStatistic(statistic, entityType, ((Map<String, Integer>) getMapSafely(entityPlayerStatistics, statistic.name())).getOrDefault(entityType.name(), 0));
                            } catch(IllegalArgumentException e) {
                                // This is expected. I don't know how else to check whether an entity is statistical.
                            }
                        }
                    break;
            }
        }
    }

    @Override
    public Map<String, Object> getValueFromConfig(ConfigurationSection config) {
        MemorySection statisticValues = (MemorySection) config.get(getConfigName());
        assert statisticValues != null;
        return statisticValues.getValues(false);
    }
}
