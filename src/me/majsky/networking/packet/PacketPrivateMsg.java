package me.majsky.networking.packet;

public class PacketPrivateMsg extends Packet{
    private static final long serialVersionUID = 4505757697737989233L;

    public String sender;
    public String msg;
    public String target;
}
