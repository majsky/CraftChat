package me.majsky.networking.packet;

public class PacketCmdResponse extends Packet{
    private static final long serialVersionUID = -7323609846778480467L;

    public final String call_cmd;
    public final String[] call_args;
    public final boolean call_callAsServer;

    public String[] result;
    public int resultStatus;

    public static final int SUCCESSFUL = 1;
    public static final int FAILED = 2;
    public static final int UNKNOWN_COMMAND = 3;
    public static final int BAD_ARGS = 4;

    public PacketCmdResponse(PacketCmdCall call){
        call_cmd = call.cmd;
        call_args = call.args;
        call_callAsServer = call.callAsServer;
    }

}
