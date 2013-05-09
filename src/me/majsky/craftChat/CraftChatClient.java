package me.majsky.craftChat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import me.majsky.networking.PacketDispatcher;
import me.majsky.networking.PacketReciever;
import me.majsky.networking.packet.Packet;
import me.majsky.networking.packet.PacketBookMessenger;
import me.majsky.networking.packet.PacketCmdCall;
import me.majsky.networking.packet.PacketCmdResponse;
import me.majsky.networking.packet.PacketStatusChange;

public class CraftChatClient extends JFrame{
    private static final long serialVersionUID = 1L;
    
    private JTextField userText;
    private JTextArea textArea;
    private Socket connection;
    private String server;
    private int port;
    private String nick;
    private Scanner s;
    
    private PacketDispatcher packetDispatcher;
    private PacketReciever packetReciever;
    
    private boolean shouldReset = true;
    
    public CraftChatClient(final boolean nogui, String server, String port, String name){
        super("CraftChat client");
        
        userText = new JTextField();
        userText.setEditable(true);
        userText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                handleMsg(arg0.getActionCommand());
                if(shouldReset)
                    userText.setText("");
                shouldReset = true;
            }
        });
        add(userText, BorderLayout.NORTH);
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setVisible(false);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane textPane = new JScrollPane(textArea);
        textPane.setAutoscrolls(true);
        add(textPane, BorderLayout.CENTER);
        setSize(600, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(!nogui);
        if(nogui){
            this.s = new Scanner(System.in);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(nogui){
                        handleMsg(CraftChatClient.this.s.nextLine());
                    }
                }
            }).start();
        }
        if(server == null || port == null || name == null)
            userText.setText("Enter server IP");
        else{
            this.server = server;
            this.port = Integer.parseInt(port);
            nick = name;
            openConnection();
            PacketStatusChange packet = new PacketStatusChange();
            packet.nick = nick;
            packet.stausID = PacketStatusChange.SERVER_JOIN;
            sendPacket(packet);
        }
    }
    
    public void handleMsg(String msg){
        if(server == null){
            server = msg;
            shouldReset = false;
            userText.setText("Enter server port");
            return;
        }
        if(port == 0){
            port = Integer.parseInt(msg);
            openConnection();
            shouldReset = false;
            userText.setText("Set your nickname");
            return;
        }
        if(nick == null){
            nick = msg.split(" ")[0];
            PacketStatusChange packet = new PacketStatusChange();
            packet.nick = nick;
            packet.stausID = PacketStatusChange.SERVER_JOIN;
            try {
                packetDispatcher.sendPacket(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        if(!msg.startsWith("/")){
            addText("[You] " + msg);
            sendMsg(msg);
            return;
        }
        
        String[] cmd = msg.split(" ");
        cmd[0] = cmd[0].substring(1);
        
        switch(cmd[0].toLowerCase()){
            case "nick":
                nick = cmd[1];
                addText("Your nick has been changd to " + cmd[1]);
                PacketStatusChange packet = new PacketStatusChange();
                packet.nick = nick;
                packet.stausID = PacketStatusChange.NICK_CHANGED;
                try {
                    packetDispatcher.sendPacket(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "end":
                userText.setEditable(false);
                System.exit(0);
                break;
            case "list":
                addText("Calling for player list...");
                PacketCmdCall p = new PacketCmdCall();
                p.cmd = "list";
                p.args = new String[]{};
                sendPacket(p);
                break;
            case "me":
                if(cmd.length < 2){
                    addText("Usage: /me [text]");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for(int i=1;i<cmd.length;i++)
                    sb.append(cmd[i] + " ");
                PacketCmdCall p1 = new PacketCmdCall();
                p1.cmd = "me";
                p1.args = new String[]{sb.toString()};
                p1.needsResponse = false;
                sendPacket(p1);
                break;
        }
    }
    
    public void sendMsg(String text){
        PacketBookMessenger packet = new PacketBookMessenger();
        packet.sender = nick;
        packet.msg = text;
        sendPacket(packet);
    }
    
    public void sendPacket(Packet packet){
        try {
            packetDispatcher.sendPacket(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addText(String text){
        System.out.println(text);
        textArea.append(text + "\n");
    }
    
    public void inChat(){
        boolean connected = true;
        do{
            Packet packet;
            try {
                packet = packetReciever.recievePacket();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                continue;
            }
            //addText(packet!=null?packet.toString():packet + "");
            if(packet instanceof PacketBookMessenger){
                PacketBookMessenger p = (PacketBookMessenger)packet;
                addText(String.format("[%s] %s", p.sender, p.msg));
                connected = p.sender.equals("SERVER")?p.msg.equals("END")?false:true:true;
            }else if(packet instanceof PacketCmdResponse){
                PacketCmdResponse p = (PacketCmdResponse) packet;
                switch (p.call_cmd) {
                    case "list":
                        if(p.resultStatus != PacketCmdResponse.SUCCESSFUL){
                            addText("Can't get online player list!");
                            break;
                        }
                        addText("Online players & chatters:");
                        for(String person:p.result)
                            addText(person);
                        break;
                    default:
                        addText("Recieved unknown command response! (" + p.call_cmd + ")");
                        break;
                }
            }
        }while(connected);
        try {
            addText("Closing connection...");
            packetDispatcher.close();
            packetReciever.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void openConnection(){
        try {
            connection = new Socket(InetAddress.getByName(server), port);
            packetDispatcher = new PacketDispatcher(connection);
            packetReciever = new PacketReciever(connection);
            textArea.setVisible(true);
        } catch (IOException e) {
            System.out.println("Connection to server failed");
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                inChat();
            }
        }).start();
    }
    
    public static void main(String[] args){
        boolean nogui = false;
        String server, port, nick;
        server = port = nick = null;
        for(int i=0;i<args.length;i++){
            if(args[i].equalsIgnoreCase("nogui")){
                nogui = true;
            }else if(args[i].equalsIgnoreCase("-server")){
                if(i+1 < args.length)
                    server = args[i+1];
            }else if(args[i].equalsIgnoreCase("-port")){
                if(i+1 < args.length)
                    port = args[i+1];
            }else if(args[i].equalsIgnoreCase("-nick")){
                if(i+1 < args.length)
                    nick = args[i+1];
            }
        }
        new CraftChatClient(nogui, server, port, nick);      
    }
}