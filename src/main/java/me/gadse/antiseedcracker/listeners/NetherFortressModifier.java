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

public class NetherFortressModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey netherFortressModified;

    public NetherFortressModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.netherFortressModified = new NamespacedKey(plugin, "nether-fortress-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER
                || !plugin.getConfig().getStringList("modifiers.nether_fortresses.worlds").contains(world.getName())) {
            return;
        }

        Collection<GeneratedStructure> structures = event.getChunk().getStructures(Structure.FORTRESS);
        if (structures.isEmpty()) {
            return;
        }

        for (GeneratedStructure structure : structures) {
            if (structure.getPersistentDataContainer().getOrDefault(netherFortressModified, PersistentDataType.BOOLEAN, false)) {
                continue;
            }

            int modifiedBlockCount = 0;
            // Modify nether bricks to cracked nether bricks (subtle change that affects structure recognition)
            // Nether fortresses span various Y levels in the nether
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 20; y <= 120; y++) { // Y range for nether fortresses
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType() == Material.NETHER_BRICKS) {
                            block.setType(Material.CRACKED_NETHER_BRICKS);
                            modifiedBlockCount++;
                        }
                    }
                }
            }

            if (modifiedBlockCount > 0) {
                structure.getPersistentDataContainer().set(netherFortressModified, PersistentDataType.BOOLEAN, true);
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}