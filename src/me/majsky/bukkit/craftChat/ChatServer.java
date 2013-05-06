package me.majsky.bukkit.craftChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import me.majsky.networking.PacketDispatcher;
import me.majsky.networking.packet.PacketBookMessenger;

public class ChatServer extends Thread{

    public final int port;
    protected ServerSocket serverSocket;
    protected List<ConnectionHandler> activeConnections;
    
    public ChatServer(int port, int backlog){
        super("BookChat Server thread");
        this.port = port;
        activeConnections = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port, backlog);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void close(){
        try {
            for(ConnectionHandler ch:activeConnections)
                ch.s.close();
            activeConnections.clear();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void addToList(ConnectionHandler handler){
        activeConnections.add(handler);
    }
    
    @Override
    public void run() {
        CraftChat.instance.logger.info("Server started");
        while(serverSocket != null && !serverSocket.isClosed()){
            try {
                Socket connection = serverSocket.accept();
                CraftChat.instance.logger.info(String.format("Remote chat client connected from %s wtih id %s", connection.getInetAddress().getHostName(), activeConnections.size()));
                ConnectionHandler connectionHandler = new ConnectionHandler(connection, activeConnections.size());
                addToList(connectionHandler);
                connectionHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    
    protected void dispatch(String msg, String sender){
        dispatch(msg, sender, -1);
    }
    
    protected void dispatch(String msg, String sender, int notSend){
        while(msg.contains("§"))
            msg = msg.substring(msg.indexOf("§") + 1, msg.length()-1);
        for(ConnectionHandler ch:activeConnections){
            if(ch.id == notSend)
                continue;
            try {
                PacketBookMessenger packet = new PacketBookMessenger();
                packet.msg = msg;
                packet.sender = sender;
                PacketDispatcher.sendPacket(ch.s, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    protected void sendTo(int id, String msg, String sender){
        for(ConnectionHandler ch:activeConnections){
            if(ch.id != id)
                continue;
            
            PacketBookMessenger packet = new PacketBookMessenger();
            packet.msg = msg;
            packet.sender = sender;
            try {
                PacketDispatcher.sendPacket(ch.s, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
