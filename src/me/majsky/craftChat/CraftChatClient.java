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

import me.majsky.networking.PacketDispatcher;
import me.majsky.networking.PacketManager;
import me.majsky.networking.packet.PacketBookMessenger;
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
    
    private boolean shouldReset = true;
    
    public CraftChatClient(final boolean nogui) {
        super("Craft chat client");
        
        userText = new JTextField("Enter server IP");
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
                        handleMsg(CraftChatClient.this.s.next());
                    }
                }
            }).start();
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
            try {
                connection = new Socket(InetAddress.getByName(server), port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    inChat();
                }
            }).start();
            shouldReset = false;
            userText.setText("Set your nickname");
            return;
        }
        if(nick == null){
            nick = msg.split(" ")[0];
            textArea.setVisible(true);
            PacketStatusChange packet = new PacketStatusChange();
            packet.nick = nick;
            packet.stausID = PacketStatusChange.SERVER_JOIN;
            try {
                PacketDispatcher.sendPacket(connection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        if(!msg.startsWith("/")){
            addText("[You] " + msg);
            sendData(msg);
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
                    PacketDispatcher.sendPacket(connection, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "end":
                userText.setEditable(false);
                System.exit(0);
                break;
        }
    }
    
    public void sendData(String text){
        PacketBookMessenger packet = new PacketBookMessenger();
        packet.sender = nick;
        packet.msg = text;
        try {
            PacketDispatcher.sendPacket(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addText(String text){
        System.out.println(text);
        textArea.append(text + "\n");
    }
    
    public void inChat(){
        String msg = "";
        do{
            try {
                PacketBookMessenger packet = (PacketBookMessenger)PacketManager.recievePacket(connection);
                msg = packet.msg;
                addText(String.format("[%s] %s", packet.sender, msg));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }while(!msg.equals("END"));
    }
    
    public static void main(String[] args){
        boolean nogui = false;
        if(args.length == 1)
            nogui = args[0].equalsIgnoreCase("nogui");
        new CraftChatClient(nogui);      
    }
}
