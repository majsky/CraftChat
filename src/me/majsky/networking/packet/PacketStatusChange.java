package me.majsky.networking.packet;

public class PacketStatusChange extends Packet {
    private static final long serialVersionUID = 8809184081878132180L;
    
    public String nick;
    public String customText = "";
    public int stausID = 0;
    
    public static final int SERVER_JOIN = 1;
    public static final int NICK_CHANGED = 2;
}
