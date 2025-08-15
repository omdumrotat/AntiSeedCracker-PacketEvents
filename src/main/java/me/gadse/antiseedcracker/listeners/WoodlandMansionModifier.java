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

public class WoodlandMansionModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey mansionModified;

    public WoodlandMansionModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.mansionModified = new NamespacedKey(plugin, "woodland-mansion-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.woodland_mansions.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.MANSION);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(mansionModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify dark oak planks to dark oak logs (subtle change that affects structure recognition)
            // Woodland mansions are large surface structures, limit Y range for performance
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 60; y <= 120; y++) { // Limited Y range for mansions
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.DARK_OAK_PLANKS) {
                            block.setType(Material.DARK_OAK_LOG);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(mansionModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}