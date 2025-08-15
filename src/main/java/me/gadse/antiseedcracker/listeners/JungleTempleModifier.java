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

public class JungleTempleModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey jungleTempleModified;

    public JungleTempleModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.jungleTempleModified = new NamespacedKey(plugin, "jungle-temple-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.jungle_temples.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.JUNGLE_PYRAMID);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(jungleTempleModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify cobblestone to mossy cobblestone (subtle change that affects structure recognition)
            // Jungle temples are surface structures, limit Y range for performance
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 60; y <= 100; y++) { // Limited Y range for jungle temples
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.COBBLESTONE) {
                            block.setType(Material.MOSSY_COBBLESTONE);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(jungleTempleModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}