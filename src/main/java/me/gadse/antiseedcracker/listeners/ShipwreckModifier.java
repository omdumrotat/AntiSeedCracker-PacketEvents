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

public class ShipwreckModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey shipwreckModified;

    public ShipwreckModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.shipwreckModified = new NamespacedKey(plugin, "shipwreck-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.shipwrecks.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.SHIPWRECK);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(shipwreckModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify oak planks to oak logs (subtle change that affects structure recognition)
            // Shipwrecks can be underwater or on surface, scan appropriate Y range
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 30; y <= 70; y++) { // Y range for shipwrecks (ocean surface to seabed)
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.OAK_PLANKS) {
                            block.setType(Material.OAK_LOG);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(shipwreckModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}