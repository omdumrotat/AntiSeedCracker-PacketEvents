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

public class VillageModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey villageModified;

    public VillageModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.villageModified = new NamespacedKey(plugin, "village-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.villages.worlds").contains(world.getName())) {
            return;
        }

        // Check for all village types
        Structure[] villageTypes = {
            Structure.VILLAGE_PLAINS, Structure.VILLAGE_DESERT, Structure.VILLAGE_SAVANNA,
            Structure.VILLAGE_SNOWY, Structure.VILLAGE_TAIGA
        };

        for (Structure villageType : villageTypes) {
            Collection<GeneratedStructure> structures = event.getChunk().getStructures(villageType);
            if (structures.isEmpty()) {
                continue;
            }

            for (GeneratedStructure structure : structures) {
                if (structure.getPersistentDataContainer().getOrDefault(villageModified, PersistentDataType.BOOLEAN, false)) {
                    continue;
                }

                int modifiedBlockCount = 0;
                // Modify hay bales to hay blocks (subtle change that affects structure recognition)
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                            Block block = event.getChunk().getBlock(x, y, z);
                            if (block.getType() == Material.HAY_BLOCK) {
                                block.setType(Material.DRIED_KELP_BLOCK);
                                modifiedBlockCount++;
                            }
                        }
                    }
                }

                if (modifiedBlockCount > 0) {
                    structure.getPersistentDataContainer().set(villageModified, PersistentDataType.BOOLEAN, true);
                }
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}