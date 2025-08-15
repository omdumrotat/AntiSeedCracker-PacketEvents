package me.gadse.antiseedcracker.listeners;

import me.gadse.antiseedcracker.AntiSeedCracker;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class StrongholdModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey strongholdModified;

    public StrongholdModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.strongholdModified = new NamespacedKey(plugin, "stronghold-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.strongholds.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.STRONGHOLD);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(strongholdModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify stone bricks to cracked stone bricks (subtle change that affects structure recognition)
            // Strongholds are underground, typically in lower Y ranges
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = -20; y <= 60; y++) { // Limited Y range for strongholds
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.STONE_BRICKS) {
                            block.setType(Material.CRACKED_STONE_BRICKS);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(strongholdModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}