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

public class OceanMonumentModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey monumentModified;

    public OceanMonumentModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.monumentModified = new NamespacedKey(plugin, "monument-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL
                || !plugin.getConfig().getStringList("modifiers.ocean_monuments.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.MONUMENT);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(monumentModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify prismarine to dark prismarine (subtle change that affects structure recognition)
            // Ocean monuments are underwater, typically at lower Y levels
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 20; y <= 80; y++) { // Limited Y range for ocean monuments
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.PRISMARINE) {
                            block.setType(Material.DARK_PRISMARINE);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(monumentModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}