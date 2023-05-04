import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

public class UI {
	//Communications is the thread. 
	public static void main(String[] args) {
		UI UI = new UI();
		UI.go();
	}
	public void printLists() {
		
	}
	public void go() {
		//Either ask user for particular port. 
		//Or get random number 4000 to 65000
	
		
		//Make attach send series of updates to peer when they connnect
		Random rn = new Random();
		int port = rn.nextInt(4000, 65000);
		Communications serverAndClient = new Communications(port);
		Scanner scnr = new Scanner(System.in);
		while (true) {
		System.out.println("Are you the master peer? (Y or N)");
		String input = scnr.nextLine();
		if (input.equals("Y")) {
			serverAndClient.start();
			System.out.println("Master peers information");
			System.out.println(serverAndClient.getPort());
			break;
		}
		//Other peers send a connect message to the master peer. Then connect to the peer master peer tells them to.
		else if(input.equals("N")) {
			System.out.println("Please enter portnumber of master");
			int clientPort = scnr.nextInt();
			serverAndClient.connectToMaster(clientPort, "localhost");
			serverAndClient.start();
			scnr.nextLine();
			break;
		}
		}
		while(true) {
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		System.out.println("What do you want to do. Add a list(A), update a list name(N), delete a list(D), update an item(UI)"
				+ "Add an item(AI)");
		String input = scnr.nextLine();
		String message;
		UUID listUUID;
		String listUUIDstring;
		String listName;
		HashMap<String, List> listOfLists;
		Collection<List> lists;
		switch(input) {
		case "A":
			System.out.println("Please input a name for your list");
			listName = scnr.nextLine();
			listUUID = UUID.randomUUID();
			listUUIDstring = uuid.toString();
			message = "UPDATE" + " " + uuidAsString + " " + listUUIDstring + " " + listName;
			serverAndClient.addOutGoingAction(message);
			System.out.println(serverAndClient.getLists());
			break;
			
		case "N":
			System.out.println("Here is a list of lists");
			listOfLists = serverAndClient.getLists();
			lists = listOfLists.values();
			for (List value: lists) {
				System.out.println(value.getID());
				System.out.println(value.getTitle());
			}
			System.out.println("Which list would you like to change the name of? (copy and paste the id)");
			listUUIDstring = scnr.nextLine();
			System.out.println("What would you like its new name to be?");
			listName = scnr.nextLine();
			message = "UPDATE" + " " + uuidAsString + " " + listUUIDstring + " " + listName;
			serverAndClient.addOutGoingAction(message);
			break;
			
		case "D":
			//Put the prints into a function before turning in since I do it like 5 times.
			System.out.println("Here is a list of lists");
			listOfLists = serverAndClient.getLists();
			lists = listOfLists.values();
			for (List value: lists) {
				System.out.println(value.getID());
				System.out.println(value.getTitle());
			}
			System.out.println("Which list would you like to delete? (copy and paste the id)");
			listUUIDstring = scnr.nextLine();
			message = "DELETE" + " " + uuidAsString + " " + listUUIDstring;
			serverAndClient.addOutGoingAction(message);
			break;
		case "UI":
			System.out.println("Here is a list of lists");
			listOfLists = serverAndClient.getLists();
			lists = listOfLists.values();
			for (List value: lists) {
				System.out.println(value.getID());
				System.out.println(value.getTitle());
			}
			System.out.println("Which list would you like to update an item for?");
			listUUIDstring = scnr.nextLine();
			List list = listOfLists.get(listUUIDstring);
			System.out.println("Here are the items. Which item would you like to change?");
			ArrayList<String> listOfItems = list.getArrayList(); 
			System.out.println("");
			for (int i = 0; i < listOfItems.size(); i++) {
				System.out.println(i + " " + listOfItems.get(i) + ",");
			}
			System.out.println("Which index would you like to change?");
			int itemIndex = Integer.parseInt(scnr.nextLine());
			System.out.println("What would you like its new name to be?");
			String itemName = scnr.nextLine();
			message = "UPDATEITEM" + " " + uuidAsString + " " + listUUIDstring + " " + itemIndex + " " + itemName;
			serverAndClient.addOutGoingAction(message);
			break;
		case "AI":
			System.out.println("Here is a list of lists");
			listOfLists = serverAndClient.getLists();
			lists = listOfLists.values();
			for (List value: lists) {
				System.out.println(value.getID());
				System.out.print(value.getTitle());
			}
			System.out.println("Which list would you like to add an item to(Entered in id)");
			listUUIDstring = scnr.nextLine();
			list = listOfLists.get(listUUIDstring);
			itemIndex = list.getArrayList().size();
			System.out.println("Enter a name for your item");
			itemName = scnr.nextLine();
			//UPDATEITEM MsgId ListId index List Item
			message = "UPDATEITEM" + " " + uuidAsString + " " + listUUIDstring + " " + itemIndex + " " + itemName;
			serverAndClient.addOutGoingAction(message);
			break;
			
		}}
	}
}

