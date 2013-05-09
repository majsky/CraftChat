package me.majsky.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import me.majsky.networking.packet.Packet;

public class PacketReciever {
    
    private ObjectInputStream ois;
    
    public PacketReciever(Socket s) throws IOException{
        ois = new ObjectInputStream(s.getInputStream());
    }
    
    public Packet recievePacket() throws IOException, ClassNotFoundException{
        Object recieved = ois.readObject();
        if(recieved instanceof Packet)
            return (Packet)recieved;   
        return null;
    }
    
    public void close() throws IOException{
        ois.close();
    }
}
