package me.gadse.antiseedcracker.packets;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import me.gadse.antiseedcracker.AntiSeedCracker;

public class ServerRespawn extends PacketListenerAbstract {

    private final AntiSeedCracker plugin;

    public ServerRespawn(AntiSeedCracker plugin) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.RESPAWN) {
            WrapperPlayServerRespawn wrapper = new WrapperPlayServerRespawn(event);
            wrapper.read();

            long hashedSeed = wrapper.getHashedSeed();
            long randHashedSeed = plugin.randomizeHashedSeed(hashedSeed);
            wrapper.setHashedSeed(randHashedSeed);

            wrapper.write();
            event.setByteBuf(wrapper.buffer);
        }
    }
}
