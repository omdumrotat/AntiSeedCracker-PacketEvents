package me.gadse.antiseedcracker.packets;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import me.gadse.antiseedcracker.AntiSeedCracker;

public class ServerLogin extends PacketListenerAbstract {

    private final AntiSeedCracker plugin;

    public ServerLogin(AntiSeedCracker plugin) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.JOIN_GAME) return;

        WrapperPlayServerJoinGame wrapper = new WrapperPlayServerJoinGame(event);
        wrapper.read();
        long hashedSeed = wrapper.getHashedSeed();

        long randHashedSeed = plugin.randomizeHashedSeed(hashedSeed);
        wrapper.setHashedSeed(randHashedSeed);

        wrapper.write();
        event.setByteBuf(wrapper.buffer);
    }
}
