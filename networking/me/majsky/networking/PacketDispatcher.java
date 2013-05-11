package me.majsky.networking;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import me.majsky.networking.packet.Packet;

public class PacketDispatcher{

    private ObjectOutputStream oos;

    public PacketDispatcher(Socket s) throws IOException{
        oos = new ObjectOutputStream(s.getOutputStream());
    }

    public void sendPacket(Packet packet) throws IOException{
        oos.writeObject(packet);
        oos.flush();
    }

    public void close() throws IOException{
        oos.close();
    }
}
