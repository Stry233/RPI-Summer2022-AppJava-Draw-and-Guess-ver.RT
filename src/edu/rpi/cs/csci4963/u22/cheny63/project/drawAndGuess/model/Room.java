package edu.rpi.cs.csci4963.u22.cheny63.project.drawAndGuess.model;

import java.util.*;
public class Room {
	LinkedList<User> users;
	//grid
	public Room(String[] names) {
		users = new LinkedList<User>();
		for(int i = 0; i<names.length;i++) {
			users.addLast(new User(names[i],0));
		}
		// grid
		
	}
	
}