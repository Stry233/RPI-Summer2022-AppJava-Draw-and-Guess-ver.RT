package edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.model;

import java.io.IOException;
import java.util.Timer;
import java.util.logging.Logger;

public class ServerModel extends ClientModel{
	// Data
	private WordDictionary dictionary;

	// Game Data
	private boolean gameStart;
	private String secretWord;
	private Timer timer;
	private int remainPoint;

	// Log
	private Logger log;

	public ServerModel(Logger log) {
		super(log);
		timer = new Timer();
		currentDrawerId = -1;
		gameStart = false;
	}

	public void readGraph(String filename) throws IOException{
		if(gameStatus == GameStatus.INIT){
			dictionary = new WordDictionary(filename);
		}
	}

	@Override
	public void addUser(String name, int id){
		UserServer user = new UserServer(name, id);
		if(gameStatus == GameStatus.PROCESSING){
			user.changeStatus(PlayerStatus.Waiter);
		}
        userList.add(user);
    }
	
	public void startGame(){
		intializeGame();
		gameStart = true;
		startRound();
	}

	public void intializeGame(){
		if(gameStatus == GameStatus.INIT || gameStatus == GameStatus.END){
			dictionary.resetWordList();
			for(User user: userList){
				((UserServer)user).initialize();
			}
			currentDrawerId = 0;
			gameStatus = GameStatus.WAITING;
		}
	}

	public void startRound(){
		if(gameStatus == GameStatus.WAITING || gameStatus == GameStatus.PROCESSING_WAIT){
			secretWord = dictionary.getRandomWord();
			gameStatus = GameStatus.PROCESSING;
			for(User user: userList){
				((UserServer)user).newRound();
			}
			remainPoint = userList.size() - 1;
		}
	}

	public void endRound(){
		if(gameStatus == GameStatus.PROCESSING){
			UserServer user = getUser(currentDrawerId);
			if(remainPoint != 0){
				addScore(user, userList.size() - remainPoint);
			}
			++currentDrawerId;
			if(currentDrawerId > userList.size()){
				currentDrawerId = 0;
			}
			gameStatus = GameStatus.PROCESSING_WAIT;
		}
	}

	public void guessWord(int id, String word){
		if(gameStatus == GameStatus.PROCESSING){
			if(currentDrawerId != id){
				UserServer user = getUser(id);
				if(!user.getGuessSuccess()){
					if(equalSecret(word)){
						userGuessRight(user);
					}
				}
			}
		}
	}

	private void userGuessRight(UserServer user){
		addScore(user, remainPoint);
		--remainPoint;
		user.setGuessSuccess();
		if(remainPoint == 0){
			endRound();
		}
	}

	private void addScore(UserServer user, int score){
		user.addScore(score);
	}

	private UserServer getUser(int id){
		for(User user: userList){
			if(user.id == id){
				return (UserServer)user;
			}
		}
		return null;
	}

	private boolean equalSecret(String word){
		return secretWord.toLowerCase().equals(word.toLowerCase());
	}
}