package me.majsky.bukkit.craftChat;

import java.io.EOFException;
import java.net.Socket;

import me.majsky.networking.PacketManager;
import me.majsky.networking.packet.Packet;
import me.majsky.networking.packet.PacketBookMessenger;
import me.majsky.networking.packet.PacketStatusChange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ConnectionHandler extends Thread{

    protected final Socket s;
    private String name;
    protected final int id;
    
    public ConnectionHandler(Socket connection, int id){
        super("BookChatClient thread");
        this.id = id;
        this.s = connection;
    }

    @Override
    public void run() {
        try {
            Packet packet;
            String msg = "";
            do{
                packet = PacketManager.recievePacket(s);
                if(packet instanceof PacketBookMessenger){
                    PacketBookMessenger p = (PacketBookMessenger)packet;
                    msg = p.msg;
                    Bukkit.getServer().broadcastMessage(String.format("<%s%s%s> %s", ChatColor.YELLOW, p.sender, ChatColor.WHITE, p.msg));
                    CraftChat.instance.server.dispatch(p.msg, p.sender, id);
                }else if(packet instanceof PacketStatusChange){
                    PacketStatusChange p = (PacketStatusChange)packet;
                    if(p.stausID == PacketStatusChange.NICK_CHANGED){
                        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + name + " changed his name to " + p.nick);
                        CraftChat.instance.server.dispatch(name + " changed his nick to " + p.nick, "SERVER");
                        name = p.nick;
                    }else if(p.stausID == PacketStatusChange.SERVER_JOIN){
                        name = p.nick;
                        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + name + " (" + s.getInetAddress().getHostName() + ") joined chat " + (!p.customText.equals("")?"("+p.customText+")":""));
                        CraftChat.instance.server.dispatch(name + " (" + s.getInetAddress().getHostName() + ") joined chat " + (!p.customText.equals("")?"("+p.customText+")":""), "Server");
                    }
                }
            }while(!msg.equals("END"));
        } catch (Exception e) {
            if(e instanceof EOFException){
                String msg = "Remote chat client " + name + " disconnected from server";
                Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + msg);
                CraftChat.instance.server.dispatch(msg, "SERVER");
            }else
                e.printStackTrace();
        }finally{
            CraftChat.instance.server.activeConnections.remove(this);
        }
    }
}
