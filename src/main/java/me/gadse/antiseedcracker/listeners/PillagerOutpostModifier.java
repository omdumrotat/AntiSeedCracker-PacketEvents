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

public class PillagerOutpostModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey pillagerOutpostModified;

    public PillagerOutpostModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.pillagerOutpostModified = new NamespacedKey(plugin, "pillager-outpost-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.pillager_outposts.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.PILLAGER_OUTPOST);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(pillagerOutpostModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify dark oak logs to stripped dark oak logs (subtle change that affects structure recognition)
            // Pillager outposts are surface structures, limit Y range for performance
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 60; y <= 120; y++) { // Limited Y range for outposts
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.DARK_OAK_LOG) {
                            block.setType(Material.STRIPPED_DARK_OAK_LOG);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(pillagerOutpostModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}