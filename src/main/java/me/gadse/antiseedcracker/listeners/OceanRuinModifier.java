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

public class OceanRuinModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey oceanRuinModified;

    public OceanRuinModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.oceanRuinModified = new NamespacedKey(plugin, "ocean-ruin-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.ocean_ruins.worlds").contains(world.getName())) {
            return;
        }

        // Check for both cold and warm ocean ruins
        Structure[] oceanRuinTypes = {
            Structure.OCEAN_RUIN_COLD, Structure.OCEAN_RUIN_WARM
        };

        for (Structure ruinType : oceanRuinTypes) {
            Collection<GeneratedStructure> structures = event.getChunk().getStructures(ruinType);
            if (structures.isEmpty()) {
                continue;
            }

            for (GeneratedStructure structure : structures) {
                if (structure.getPersistentDataContainer().getOrDefault(oceanRuinModified, PersistentDataType.BOOLEAN, false)) {
                    continue;
                }

                int modifiedBlockCount = 0;
                // Modify stone bricks to mossy stone bricks (subtle change that affects structure recognition)
                // Ocean ruins are underwater structures, scan appropriate Y range
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 30; y <= 65; y++) { // Y range for ocean ruins
                            Block block = event.getChunk().getBlock(x, y, z);
                            if (block.getType() == Material.STONE_BRICKS) {
                                block.setType(Material.MOSSY_STONE_BRICKS);
                                modifiedBlockCount++;
                            }
                        }
                    }
                }

                if (modifiedBlockCount > 0) {
                    structure.getPersistentDataContainer().set(oceanRuinModified, PersistentDataType.BOOLEAN, true);
                }
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}