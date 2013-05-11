package me.majsky.bukkit.craftChat;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import me.majsky.networking.PacketDispatcher;
import me.majsky.networking.PacketReciever;
import me.majsky.networking.packet.Packet;
import me.majsky.networking.packet.PacketCmdCall;
import me.majsky.networking.packet.PacketCmdResponse;
import me.majsky.networking.packet.PacketMsg;
import me.majsky.networking.packet.PacketPrivateMsg;
import me.majsky.networking.packet.PacketStatusChange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ConnectionHandler extends Thread{

    protected final Socket s;
    protected PacketDispatcher dispatcher;
    protected PacketReciever reciever;
    protected String name;
    protected final int id;

    public ConnectionHandler(Socket connection, int id){
        super("BookChatClient thread");
        this.id = id;
        s = connection;
        try{
            dispatcher = new PacketDispatcher(connection);
            reciever = new PacketReciever(connection);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try{
            String msg = "";
            do{
                Packet packet = reciever.recievePacket();
                if(packet instanceof PacketMsg){
                    PacketMsg p = (PacketMsg)packet;
                    msg = p.msg;
                    Bukkit.getServer().broadcastMessage(
                            String.format("<%s%s%s> %s", ChatColor.YELLOW, name, ChatColor.WHITE, p.msg));
                    CraftChat.instance.server.dispatch(p.msg, name, id);
                } else if(packet instanceof PacketStatusChange){
                    PacketStatusChange p = (PacketStatusChange)packet;
                    if(p.stausID == PacketStatusChange.NICK_CHANGED){
                        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + name + " changed his name to " + p.nick);
                        CraftChat.instance.server.dispatch(name + " changed his nick to " + p.nick, "SERVER");
                        name = p.nick;
                    } else if(p.stausID == PacketStatusChange.SERVER_JOIN){
                        name = p.nick;
                        Bukkit.getServer().broadcastMessage(
                                ChatColor.YELLOW + name + " (" + s.getInetAddress().getHostName() + ") joined chat "
                                        + (!p.customText.equals("") ? "(" + p.customText + ")" : ""));
                        CraftChat.instance.server.dispatch(name + " (" + s.getInetAddress().getHostName()
                                + ") joined chat " + (!p.customText.equals("") ? "(" + p.customText + ")" : ""),
                                "SERVER");
                    }
                } else if(packet instanceof PacketPrivateMsg){
                    PacketPrivateMsg p = (PacketPrivateMsg)packet;
                    CraftChat.instance.server.sendPacket(CraftChat.instance.server.identify(p.target), p);
                    CraftChat.instance.logger.info(String.format("%s sent private msg to %s", name, p.target));
                } else if(packet instanceof PacketCmdCall){
                    PacketCmdCall p = (PacketCmdCall)packet;
                    CraftChat.instance.logger.info("User " + name + " is calling command " + p.cmd);
                    PacketCmdResponse response = new PacketCmdResponse(p);
                    switch (p.cmd){
                        case "list":
                            StringBuilder data = new StringBuilder();
                            for(Player player:Bukkit.getServer().getOnlinePlayers())
                                data.append(player.getName() + ":");
                            for(ConnectionHandler ch:CraftChat.instance.server.activeConnections)
                                data.append("-" + ch.name + ":");
                            response.result = data.toString().split(":");
                            response.resultStatus = PacketCmdResponse.SUCCESSFUL;
                            break;
                        case "me":
                            if(p.args.length != 1){
                                response.resultStatus = PacketCmdResponse.BAD_ARGS;
                                break;
                            }
                            Bukkit.getServer().broadcastMessage("*" + name + " " + p.args[0]);
                            CraftChat.instance.server.dispatch("*" + name + " " + p.args[0], "SERVER");
                            response.resultStatus = PacketCmdResponse.SUCCESSFUL;
                            break;
                        default:
                            response.resultStatus = PacketCmdResponse.UNKNOWN_COMMAND;
                            break;
                    }
                    if(p.needsResponse)
                        dispatcher.sendPacket(response);
                }
            } while(!msg.equals("END"));
        } catch(Exception e){
            if(e instanceof EOFException){
                String msg = "Remote chat client " + name + " disconnected from server";
                Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + msg);
                CraftChat.instance.server.dispatch(msg, "SERVER");
            } else
                e.printStackTrace();
        } finally{
            CraftChat.instance.server.activeConnections.remove(this);
        }
    }
}