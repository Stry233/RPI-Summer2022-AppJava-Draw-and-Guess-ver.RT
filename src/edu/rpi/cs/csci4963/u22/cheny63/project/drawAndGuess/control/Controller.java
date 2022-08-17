package edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.control;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.UI.DrawAndGuessGUI;
import edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.model.ClientModel;
import edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.model.ServerModel;

public class Controller{
    // Game Object
    private Config config;
    private Protocol protocol;
    private ClientModel model;
    private DrawAndGuessGUI window;
    
    // Game Network
    private Thread network;
    private Server server;
    private Client client;
    private boolean isServer;
    private int myId;
    private String myName;


    // Logger
    private Logger log;

    public Controller() throws Exception{
        // Create logger
        log = Logger.getLogger("DrawAndGuess");
        StreamHandler handler;
		try {
			handler = new FileHandler();
		}catch (Exception e) {
			log.warning(String.format("Unable to create logger file handler: %s", e.getMessage()));
            throw e;
		} 
		handler.setFormatter(new SimpleFormatter());
		log.addHandler(handler);

        // Create game object
        config = new Config(log);
        protocol = new Protocol(this);
        window = new DrawAndGuessGUI(this);
    }

    public void onStartServer(String name, int port, String filePath){
        config.setName(name);
        config.setFilePath(filePath);
        startServer(port);
        myName = name;
        // afterConnect();
    }

    public void onClientStart(String name, String address, int port){
        config.setName(name);
        startClient(address, port);
        myName = name;
        // afterConnect();
    }

    /**
     * Intial command to send after the connection is made
     */
    public void afterConnect(){
        // Send the player name to server
        if(isServer){
            protocol.process(protocol.userJoinServerEvent("localhost", myName));
        }else{
            client.send(protocol.userJoinServerEvent(client.getAddress(), myName));
        }
    }

    public void onIdReturn(int id){
        myId = id;
    }

    public void sendMessageToAll(String message){
        if(isServer){
            server.sendMessageToAll(message);
            protocol.process(message);
        }
    }

    public void startGame(){
        ((ServerModel)model).startGame();
    }

    public String getNameConfig(){
        return config.getName();
    }

    public String getFileConfig(){
        return config.getFilePath();
    }

    public Protocol getProtocol(){
        return protocol;
    }

    public boolean isServer(){
        return isServer;
    }

    public void onPlayerJoinServer(String name, String address){
        if(isServer){
            int id = server.getId(address);
            if(!address.equals("localhost")){
                server.sendMessage(protocol.serverReturnIdEvent(id), address);
            }else{
                myId = 0;
                id = 0;
            }
            sendMessageToAll(protocol.userJoinClientEvent(id, name));
        }
    }

    public void onPlayerLeaveServer(int id){
        if(isServer){
            sendMessageToAll(protocol.userLeftEvent(id));
        }
    }

    public void onPlayerJoinClient(String name, int id){
        model.addUser(name, id);
        model.addChat("System", "Welcome %s to the game!".formatted(name));
    }

    public void onPlayerLeaveClient(int id){
        model.addChat("System", "Player %s left the game!".formatted(model.getPlayerName(id)));
        model.removeUser(id);
    }

    public void onPlayerSentMessage(String message){
        if(isServer){
            onPlayerReceiveMessageServer(myId, message);
        }else{
            client.send(protocol.userSentMessageEvent(myId, message));
        }
    }

    public void onPlayerReceiveMessageServer(int id, String message){
        // TODO: Guess Word
        sendMessageToAll(protocol.serverSentMessageEvent(id, message));
     }

    public void onPlayerReceiveMessageClient(int id, String message){
       model.addChat(model.getPlayerName(id), message);
    }

    public void onClose(){
        log.info("Closing the application...");
        config.save();
        if(network != null){
            network.interrupt();
            try{
                network.join();
            }catch(InterruptedException e){
                log.warning("Failed to interrupt network thread");
            }
        }
        System.exit(0);
    }

    private void startServer(int port){
        model = new ServerModel(log);
        server = new Server(port, log, this);
        network = new Thread(server);
        isServer = true;
        network.start();
    }

    private void startClient(String address, int port){
        model = new ClientModel(log);
        client = new Client(address, port, log, this);
        network = new Thread(client);
        isServer = false;
        network.start();
    }
}
