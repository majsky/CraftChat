package me.majsky.bukkit.craftChat;

import java.util.logging.Logger;

import me.majsky.lib.simpleConfig.ConfigCategory;
import me.majsky.lib.simpleConfig.SimpleConfigFile;

import org.bukkit.plugin.java.JavaPlugin;

public class CraftChat extends JavaPlugin{

    protected ChatServer server;
    protected EventListener listener;

    protected Logger logger;
    public static CraftChat instance;

    @Override
    public void onEnable(){
        logger = getLogger();
        listener = new EventListener();
        getServer().getPluginManager().registerEvents(listener, this);
        server.start();
    }

    @Override
    public void onDisable(){
        server.close();
    }

    @Override
    public void onLoad(){
        CraftChat.instance = this;
        SimpleConfigFile config = new SimpleConfigFile(this);
        config.load();

        ConfigCategory catGeneral = config.getOrCreateCategory("General");
        ConfigCategory catConnection = config.getOrCreateCategory("Connection");

        catGeneral.addValue("Server enabled", "true", false);
        catConnection.addValue("Server port", "29899", false);
        catConnection.addValue("Maximum remote clients", "20", false);

        if(catGeneral.getValue("Server enabled").equalsIgnoreCase("true"))
            server = new ChatServer(Integer.parseInt(catConnection.getValue("Server port")),
                    Integer.parseInt(catConnection.getValue("Maximum remote clients")));

        config.save();
    }

}
