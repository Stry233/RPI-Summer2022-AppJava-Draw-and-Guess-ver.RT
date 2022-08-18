package edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Logger;

import edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.tools.StringUtil;

/**
* Game config loader
* @author Kevin Xia
* @version 1.0
*/
public class Config {
    // Config value
    private Properties configFile;
    private String username;
    private String filePath;
    private String address;
    private int port;
    private Logger log;

    /**
     * Constructor of game config
     */
    public Config(Logger log){
        this.log = log;
        configFile = new Properties();
        load();
    }

    /**
     * Default config of the game
     */
    public void defaultConfig(){
        username = "Player";
        filePath = System.getProperty("user.dir");
        try{
            address = InetAddress.getLocalHost().toString().split("/")[1];
        }catch(UnknownHostException e){
            log.severe("Cannot get localhost information...");
            address = "";
        }
        port = 8180;
    }

    /**
     * Validate the data and change if needed
     * @return true if data changes and need to update in other component,
     *          false otherwise
     */
    public boolean validateData(){
        boolean update = false;
        if(username == null){
            username = "Player";
            update = true;
        }
        File path = new File(filePath);
        if(!path.exists()){
            setFilePath(System.getProperty("user.dir"));
            update = false;
        }
        if(port < 0 || port > 65565){
            port = 8180;
            update = true;
        }
        if(!StringUtil.validAddress(address)){
            try{
                address = InetAddress.getLocalHost().toString().split("/")[1];
            }catch(UnknownHostException e){
                log.severe("Cannot get localhost information...");
                address = "";
            }
            update = true;
        }
        return update;
    }

    /**
     * Load the config file
     * It will create a new config file from default config if failed to load config file
     */
    public void load(){
        log.info("Loading config file...");
        try {
            // Loading
            FileInputStream file = new FileInputStream("Config.cfg");
            configFile.load(file);
            file.close();
            // Config
            filePath = configFile.getProperty("file.path");
            username = configFile.getProperty("network.username");
            address = configFile.getProperty("network.address");
            port = Integer.parseInt(configFile.getProperty("network.port"));
            // Validate
            log.info("Validating config...");
            if(validateData()){
                log.info("Found invalid config, resetting them to default value");
                save();
            }
            log.info("Finish loading config file");
        }catch(Exception e){
            log.warning("Unable to load config file, using default config");
            defaultConfig();
            save();
        }
    }

    /**
     * Save the config file
     */
    public void save(){
        log.info("Saving config file...");
        try{
            // Game
            configFile.setProperty("file.path", filePath);
            configFile.setProperty("network.username", username);
            configFile.setProperty("network.address", address);
            configFile.setProperty("network.port", Integer.toString(port));
            // Saving
            FileOutputStream file = new FileOutputStream("Config.cfg");
            configFile.store(file, "Config for Draw and Guess");            
            file.close();
            log.info("Finish saving config file");
        }catch(Exception e){
            log.severe("Unable to save config file");
        }
    }

    /**
     * Set the usename
     * @param name the username
     */
    public void setName(String name){
        username = name;
    }

    /**
     * Get the username
     * @return the username
     */
    public String getName(){
        return username;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getAddress(){
        return address;
    }

    public void setPort(int port){
        this.port = port;
    }

    public int getPort(){
        return port;
    }
}
