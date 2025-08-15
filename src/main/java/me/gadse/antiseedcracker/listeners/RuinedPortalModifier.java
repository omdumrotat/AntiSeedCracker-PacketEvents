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

public class RuinedPortalModifier implements Listener {

    private final AntiSeedCracker plugin;
    private final NamespacedKey ruinedPortalModified;

    public RuinedPortalModifier(AntiSeedCracker plugin) {
        this.plugin = plugin;
        this.ruinedPortalModified = new NamespacedKey(plugin, "ruined-portal-modified");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        if (!plugin.getConfig().getStringList("modifiers.ruined_portals.worlds").contains(world.getName())) {
            return;
        }

        // Check for all ruined portal types
        Structure[] ruinedPortalTypes = {
            Structure.RUINED_PORTAL, Structure.RUINED_PORTAL_DESERT, Structure.RUINED_PORTAL_JUNGLE,
            Structure.RUINED_PORTAL_MOUNTAIN, Structure.RUINED_PORTAL_NETHER, Structure.RUINED_PORTAL_OCEAN,
            Structure.RUINED_PORTAL_SWAMP
        };

        for (Structure portalType : ruinedPortalTypes) {
            Collection<GeneratedStructure> structures = event.getChunk().getStructures(portalType);
            if (structures.isEmpty()) {
                continue;
            }

            for (GeneratedStructure structure : structures) {
                if (structure.getPersistentDataContainer().getOrDefault(ruinedPortalModified, PersistentDataType.BOOLEAN, false)) {
                    continue;
                }

                int modifiedBlockCount = 0;
                // Modify obsidian to crying obsidian (fits thematic and affects structure recognition)
                // Ruined portals can be in various Y levels, broad range needed
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y <= 120; y++) { // Broad Y range for ruined portals
                            Block block = event.getChunk().getBlock(x, y, z);
                            if (block.getType() == Material.OBSIDIAN) {
                                block.setType(Material.CRYING_OBSIDIAN);
                                modifiedBlockCount++;
                            }
                        }
                    }
                }

                if (modifiedBlockCount > 0) {
                    structure.getPersistentDataContainer().set(ruinedPortalModified, PersistentDataType.BOOLEAN, true);
                }
            }
        }
    }

    public void unregister() {
        ChunkLoadEvent.getHandlerList().unregister(this);
    }
}