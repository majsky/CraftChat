package me.majsky.networking.packet;

public class PacketCmdCall extends Packet {
    private static final long serialVersionUID = -1613205362363768786L;

    public String cmd;
    public String[] args;
    public boolean callAsServer = false;
    
    public boolean needsResponse = true;

}
