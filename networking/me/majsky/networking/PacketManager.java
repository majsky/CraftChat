package me.majsky.networking;

import java.io.ObjectInputStream;
import java.net.Socket;

import me.majsky.networking.packet.Packet;

public class PacketManager {

    public static Packet recievePacket(Socket s) throws Exception{
        if(s.isClosed())
            return null;
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        Object recieved = ois.readObject();
        if(recieved instanceof Packet)
            return (Packet) recieved;
        return null;
    }
}
