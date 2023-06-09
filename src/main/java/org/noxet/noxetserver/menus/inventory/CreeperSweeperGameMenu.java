package org.noxet.noxetserver.menus.inventory;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.noxet.noxetserver.NoxetServer;
import org.noxet.noxetserver.creepersweeper.CreeperSweeperGame;
import org.noxet.noxetserver.creepersweeper.CreeperSweeperTile;
import org.noxet.noxetserver.menus.ItemGenerator;
import org.noxet.noxetserver.messaging.Message;
import org.noxet.noxetserver.playerdata.PlayerDataManager;
import org.noxet.noxetserver.util.FancyTimeConverter;
import org.noxet.noxetserver.util.InventoryCoordinate;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreeperSweeperGameMenu extends InventoryMenu {
    private static final List<Material> countHintColors = Arrays.asList(
            Material.LIGHT_BLUE_DYE, // 1
            Material.GREEN_DYE,      // 2
            Material.RED_DYE,        // 3
            Material.BLUE_DYE,       // 4
            Material.ORANGE_DYE,     // 5
            Material.CYAN_DYE,       // 6
            Material.BLACK_DYE,      // 7
            Material.GRAY_DYE,       // 8
            Material.PURPLE_DYE      // 9
    );

    private final CreeperSweeperGame game;
    private final int height, creepers;

    public CreeperSweeperGameMenu(int height, int creepers) {
        super(height, "Creeper Sweeper (9x" + height + ", " + creepers + " creepers)", false);

        this.height = height;
        this.creepers = creepers;
        game = new CreeperSweeperGame(9, height, creepers);
    }

    @Override
    protected void updateInventory() {
        Iterator<Map.Entry<InventoryCoordinate, CreeperSweeperTile>> tileEntries = game.tileIterator();

        while(tileEntries.hasNext()) {
            Map.Entry<InventoryCoordinate, CreeperSweeperTile> tileEntry = tileEntries.next();
            CreeperSweeperTile tile = tileEntry.getValue();

            // Now, draw the tile:

            ItemStack tileItemStack;

            if(!tile.isRevealed() && !game.hasEnded()) {
                // Covered tile.
                tileItemStack = !tile.isFlagged() ?
                        ItemGenerator.generateItem(Material.GRASS_BLOCK, "§r") :
                        ItemGenerator.generateItem(Material.CREEPER_BANNER_PATTERN, "§c⚐");
            } else if(tile.isCreeperTile()) {
                // Uncovered creeper tile.
                tileItemStack = ItemGenerator.generateItem(Material.CREEPER_HEAD, "§c§lCREEPER!");
            } else if(tile.countCreeperNeighbors(game, tileEntry.getKey()) > 0) {
                // Uncovered safe tile (with creeper proximity number).
                int count = tile.countCreeperNeighbors(game, tileEntry.getKey());

                tileItemStack = ItemGenerator.generateItem(
                        countHintColors.get(count - 1),
                        count,
                        "§r",
                        null
                );
            } else
                tileItemStack = ItemGenerator.generateItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§r");

            setSlotItem(tileItemStack, tileEntry.getKey());
        }
    }

    enum SurpriseFill {
        WIN(ItemGenerator.generateItem(Material.EMERALD, "§a§lFinished!")),
        LOSE(ItemGenerator.generateItem(Material.TNT, "§c§lAwww..."));

        private final ItemStack itemStack;

        SurpriseFill(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public void fillInventory(Inventory inventory) {
            for(int i = 0; i < inventory.getSize(); i++)
                inventory.setItem(i, itemStack);
        }
    }

    @Override
    protected boolean onSlotClick(Player player, InventoryCoordinate coordinate, ClickType clickType) {
        CreeperSweeperTile clickedTile = game.getTileAt(coordinate);

        if(clickedTile == null)
            return false;

        if(game.hasEnded()) {
            new CreeperSweeperGameMenu(height, creepers).openInventory(player);
            return true;
        }

        switch(clickType) {
            case LEFT: // Open tile.
                game.revealTileAt(coordinate);
                break;
            case RIGHT: // Flag tile.
                clickedTile.setFlagged(!clickedTile.isFlagged());
                break;
        }

        SurpriseFill surpriseFill = null;

        if(game.hasEnded()) {
            PlayerDataManager playerDataManager = new PlayerDataManager(player);

            if(game.didWin()) {
                player.playSound(player, Sound.ENTITY_CREEPER_DEATH, 1, 0.5f);

                new Message("§eYou beat Creeper Sweeper in §c" + FancyTimeConverter.deltaSecondsToFancyTime((int) (game.getGameDuration() / 1000)) + "§e.").send(player);

                playerDataManager.incrementInt(PlayerDataManager.Attribute.CREEPER_SWEEPER_WINS);
                playerDataManager.addLong(PlayerDataManager.Attribute.CREEPER_SWEEPER_TOTAL_WIN_PLAYTIME, game.getGameDuration() / 1000);

                surpriseFill = SurpriseFill.WIN;
            } else {
                player.playSound(player, Sound.ENTITY_CREEPER_PRIMED, 1, 1);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                    }
                }.runTaskLater(NoxetServer.getPlugin(), 20);

                new Message("§c§lTss...! You revealed a creeper.").send(player);

                playerDataManager.incrementInt(PlayerDataManager.Attribute.CREEPER_SWEEPER_LOSSES);

                surpriseFill = SurpriseFill.LOSE;
            }

            int wins = (int) playerDataManager.get(PlayerDataManager.Attribute.CREEPER_SWEEPER_WINS),
                losses = (int) playerDataManager.get(PlayerDataManager.Attribute.CREEPER_SWEEPER_LOSSES);

            new Message(null).add(
                    "W/L: §e" + new DecimalFormat("###.###").format((double) wins / Math.max(losses, 1)) + "§7 (" + wins + " wins, " + losses + " losses)",
                    "The higher W/L, the better. To clear these stats, type /clear-creeper-sweeper-stats."
            ).send(player);

            long totalWinPlaytime = (long) playerDataManager.get(PlayerDataManager.Attribute.CREEPER_SWEEPER_TOTAL_WIN_PLAYTIME);

            if(wins > 0)
                new Message("Average time (wins): §e" + FancyTimeConverter.deltaSecondsToFancyTime((int) (totalWinPlaytime / wins))).send(player);

            playerDataManager.save();
        }

        if(surpriseFill != null) {
            surpriseFill.fillInventory(getInventory());
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateInventory();
                }
            }.runTaskLater(NoxetServer.getPlugin(), 20);
        } else
            updateInventory();
        return false;
    }
}
