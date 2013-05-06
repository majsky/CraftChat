package me.majsky.networking;

import java.io.ObjectOutputStream;
import java.net.Socket;

import me.majsky.networking.packet.Packet;

public class PacketDispatcher {
    
    public static void sendPacket(Socket s, Packet packet) throws Exception{
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(packet);
        oos.flush();
    }
}
