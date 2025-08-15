package me.gadse.antiseedcracker;

import com.github.retrooper.packetevents.PacketEvents;
import com.tcoded.folialib.FoliaLib;
import me.gadse.antiseedcracker.commands.AntiSeedCrackerCommand;
import me.gadse.antiseedcracker.listeners.BastionRemnantModifier;
import me.gadse.antiseedcracker.listeners.DesertPyramidModifier;
import me.gadse.antiseedcracker.listeners.DragonRespawnSpikeModifier;
import me.gadse.antiseedcracker.listeners.EndCityModifier;
import me.gadse.antiseedcracker.listeners.OceanMonumentModifier;
import me.gadse.antiseedcracker.listeners.StrongholdModifier;
import me.gadse.antiseedcracker.listeners.VillageModifier;
import me.gadse.antiseedcracker.packets.ServerLogin;
import me.gadse.antiseedcracker.packets.ServerRespawn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class AntiSeedCracker extends JavaPlugin implements CommandExecutor {

    private NamespacedKey modifiedSpike;
    private FoliaLib foliaLib;

    private DragonRespawnSpikeModifier dragonRespawnspikeModifier;
    private EndCityModifier endCityModifier;
    private VillageModifier villageModifier;
    private BastionRemnantModifier bastionRemnantModifier;
    private OceanMonumentModifier oceanMonumentModifier;
    private DesertPyramidModifier desertPyramidModifier;
    private StrongholdModifier strongholdModifier;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Config folder can not be written. Check read/write permissions.");
        }
        saveDefaultConfig();
        
        // Initialize FoliaLib
        foliaLib = new FoliaLib(this);
        
        modifiedSpike = new NamespacedKey(this, "modified-spike");
        dragonRespawnspikeModifier = new DragonRespawnSpikeModifier(this, foliaLib);
        endCityModifier = new EndCityModifier(this);
        villageModifier = new VillageModifier(this);
        bastionRemnantModifier = new BastionRemnantModifier(this);
        oceanMonumentModifier = new OceanMonumentModifier(this);
        desertPyramidModifier = new DesertPyramidModifier(this);
        strongholdModifier = new StrongholdModifier(this);

        PluginCommand command = getCommand("antiseedcracker");
        if (command == null) {
            getLogger().severe("The antiseedcracker command is missing from plugin.yml.");
        } else {
            command.setExecutor(new AntiSeedCrackerCommand(this));
        }

        reload(true);
    }

    public void reload(boolean isOnEnable) {
        if (!isOnEnable) {
            PacketEvents.getAPI().getEventManager().unregisterAllListeners();
            dragonRespawnspikeModifier.unregister();
            endCityModifier.unregister();
            villageModifier.unregister();
            bastionRemnantModifier.unregister();
            oceanMonumentModifier.unregister();
            desertPyramidModifier.unregister();
            strongholdModifier.unregister();
        }

        if (getConfig().getBoolean("randomize_hashed_seed.login", true)) {
            PacketEvents.getAPI().getEventManager().registerListener(new ServerLogin(this));
        }

        if (getConfig().getBoolean("randomize_hashed_seed.respawn", true)) {
            PacketEvents.getAPI().getEventManager().registerListener(new ServerRespawn(this));
        }

        if (getConfig().getBoolean("modifiers.end_spikes.enabled", false)) {
            getServer().getWorlds().forEach(world -> {
                if (!getConfig().getStringList("modifiers.end_spikes.worlds").contains(world.getName())) {
                    return;
                }

                if (world.getEnvironment() != World.Environment.THE_END) {
                    getLogger().warning("The world '%s' is not an end dimension, it will be ignored.");
                    return;
                }

                modifyEndSpikes(world);
            });
            getServer().getPluginManager().registerEvents(dragonRespawnspikeModifier, this);
        }

        if (getConfig().getBoolean("modifiers.end_cities.enabled", false)) {
            getServer().getPluginManager().registerEvents(endCityModifier, this);
        }

        if (getConfig().getBoolean("modifiers.villages.enabled", false)) {
            getServer().getPluginManager().registerEvents(villageModifier, this);
        }

        if (getConfig().getBoolean("modifiers.bastion_remnants.enabled", false)) {
            getServer().getPluginManager().registerEvents(bastionRemnantModifier, this);
        }

        if (getConfig().getBoolean("modifiers.ocean_monuments.enabled", false)) {
            getServer().getPluginManager().registerEvents(oceanMonumentModifier, this);
        }

        if (getConfig().getBoolean("modifiers.desert_pyramids.enabled", false)) {
            getServer().getPluginManager().registerEvents(desertPyramidModifier, this);
        }

        if (getConfig().getBoolean("modifiers.strongholds.enabled", false)) {
            getServer().getPluginManager().registerEvents(strongholdModifier, this);
        }
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().getEventManager().unregisterAllListeners();
        dragonRespawnspikeModifier.unregister();
        endCityModifier.unregister();
        villageModifier.unregister();
        bastionRemnantModifier.unregister();
        oceanMonumentModifier.unregister();
        desertPyramidModifier.unregister();
        strongholdModifier.unregister();
        
        // Cancel all FoliaLib tasks
        if (foliaLib != null) {
            foliaLib.getScheduler().cancelAllTasks();
        }
    }

    public long randomizeHashedSeed(long hashedSeed) {
        int length = Long.toString(hashedSeed).length();
        if (length > 18) {
            length = 18;
        }
        long min = (long) Math.pow(10, length - 1);
        long max = (long) (Math.pow(10, length) - 1);
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    // https://minecraft.wiki/w/End_spike
    private final List<Integer> spikeHeights = List.of(76, 79, 82, 85, 88, 91, 94, 97, 100, 103);

    public void modifyEndSpikes(World world) {
        if (world.getEnvironment() != World.Environment.THE_END
                || world.getPersistentDataContainer().getOrDefault(modifiedSpike, PersistentDataType.BOOLEAN, false)) {
            return;
        }

        Map<Integer, Block> bedrockBlocksByHeight = getBedrockBlocksByHeight(world);
        if (getConfig().getString("modify_end_spikes.mode", "swap").equalsIgnoreCase("swap")) {
            swapEndSpikes(world, bedrockBlocksByHeight);
        } else {
            moveEndSpike(world, bedrockBlocksByHeight);
        }
    }

    private void swapEndSpikes(World world, Map<Integer, Block> bedrockBlocksByHeight) {
        int randomSpikeIndex = ThreadLocalRandom.current().nextInt(spikeHeights.size());
        int nextSpikeIndex = randomSpikeIndex + 1 > spikeHeights.size() - 1 ? 0 : randomSpikeIndex + 1;
        Block spike_one = bedrockBlocksByHeight.get(spikeHeights.get(randomSpikeIndex));
        Block spike_two = bedrockBlocksByHeight.get(spikeHeights.get(nextSpikeIndex));

        spike_one.setType(Material.OBSIDIAN);
        new Location(world, spike_one.getX(), spike_two.getY(), spike_one.getZ()).getBlock().setType(Material.BEDROCK);

        spike_two.setType(Material.OBSIDIAN);
        new Location(world, spike_two.getX(), spike_one.getY(), spike_two.getZ()).getBlock().setType(Material.BEDROCK);

        world.getPersistentDataContainer().set(modifiedSpike, PersistentDataType.BOOLEAN, true);
    }

    private void moveEndSpike(World world, Map<Integer, Block> bedrockBlocksByHeight) {
        int randomSpikeIndex = ThreadLocalRandom.current().nextInt(spikeHeights.size());
        Block endSpike = bedrockBlocksByHeight.get(spikeHeights.get(randomSpikeIndex));

        endSpike.setType(Material.OBSIDIAN);
        endSpike.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

        world.getPersistentDataContainer().set(modifiedSpike, PersistentDataType.BOOLEAN, true);
    }

    public Map<Integer, Block> getBedrockBlocksByHeight(World world) {
        Map<Integer, Block> bedrockBlocksByHeight = new HashMap<>(10);

        for (int i = 0; i < 10; i++) {
            // Source: net.minecraft.world.level.levelgen.feature.SpikeFeature.SpikeCacheLoader
            double x = 42.0 * Math.cos(2.0 * (-Math.PI + 0.3141592653589793 * i));
            double z = 42.0 * Math.sin(2.0 * (-Math.PI + 0.3141592653589793 * i));

            Block block = world.getHighestBlockAt(new Location(world, x, 0, z));
            while (block.getType() != Material.BEDROCK && block.getY() > 0) {
                block = block.getRelative(BlockFace.DOWN);
            }
            bedrockBlocksByHeight.put(block.getY(), block);
        }

        return bedrockBlocksByHeight;
    }

    public NamespacedKey getModifiedSpike() {
        return modifiedSpike;
    }
    
    public FoliaLib getFoliaLib() {
        return foliaLib;
    }
}
