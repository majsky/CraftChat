package me.majsky.networking.packet;

import java.io.IOException;
import java.io.Serializable;

import me.majsky.networking.PacketDispatcher;

public class Packet implements Serializable{
    private static final long serialVersionUID = 5173427639789441523L;

    public void send(PacketDispatcher dispatcher) throws IOException{
        dispatcher.sendPacket(this);
    }
}
