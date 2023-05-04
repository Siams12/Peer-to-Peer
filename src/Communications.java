import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

//First thing other peers do on startup is get ip to connect to master peer.
//This will be a thread for mantaining communication between nodes.
public class Communications extends Thread {
	private ArrayList<String> outgoingActions = new ArrayList<String>();
	private ArrayList<String> incomingActions = new ArrayList<String>();
	private int port;
	private String IPAddress;
	private ServerSocket serverSocket;
	private HashMap<String, Boolean> UUIDNotSend = new HashMap<String, Boolean>();
	private HashMap<String, List> lists = new HashMap<String, List>();
	
	public void addOutGoingAction(String message) {
		outgoingActions.add(message);
	}
	public HashMap<String, List> getLists() {
		return lists;
	}
	public int getPort() {
		return port;
	}
	public String getIPAddress() {
		return IPAddress;
	}
	public Communications(int port) {
		this.port = port;
		this.serverSocket = connectServerSocket();
		this.IPAddress = this.serverSocket.getInetAddress().toString();
		
	}
	public class Peer
	{
		public Socket client;
		public PrintWriter writer;
		public BufferedReader reader;
		public int port;
	}
	//allClients is only used by master and contains each client
	private ArrayList<Peer> allClients = new ArrayList<Peer>();
	//Clients current node is connected to 
	private ArrayList<Peer> clients = new ArrayList<Peer>();
	
	public ArrayList<Peer> getClients(){
		return clients;
	}
	public Socket getClientSocket(ServerSocket serverSocket) {
        	/* 
        	 * Give new clients a chance to connect.
        	 */
        	
        	Socket client = null;
        	
        	try
			{
        		client = serverSocket.accept ();
			}
        	catch (SocketTimeoutException e)
			{
        		/* do nothing, this just means no client tried to connect */
			}
        	catch (Exception e)
			{
        		e.printStackTrace ();
			}
        	return client;

	}
	

	public ServerSocket connectServerSocket() {
		ServerSocket serverSocket = null; 
        
        /*
         * Create the server socket and set a timeout so that
         * it will not wait forever for a client to connect when
         * we call accept ().
         */
        
        try 
        {
            serverSocket = new ServerSocket(this.port);
            System.out.println(serverSocket);
            serverSocket.setSoTimeout (50);
            System.out.println ("Listening on port 4444");
        } 
        catch (IOException e) 
        { 
            System.err.println("Could not listen on port: 4444."); 
            System.exit(-1); 
        } 
        return serverSocket;
	}
	
	private void sendToClient (Peer peer, String message)
    {
	    try
	    {
	      peer.writer.println(message);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace ();
	    }
    }
	//Send outgoing actions. This peer represents one of the clients to send to.
	//Sends one action to all clients
	private boolean sendMessages(String action) {
		for (int i = 0; i < clients.size(); i++) {
			
			Peer peer = clients.get(i);
			try {
			peer.writer.println(action);
			//UUIDNotSend.put(sendAction.split(" ")[1], true);
			}catch(Exception e) {
				System.out.println(e.getMessage());
				return false;
		}
		
	}
		outgoingActions.remove(action);
		return true;
	}

	public boolean connectToMaster(int portNum, String IP) {
		Socket server = null;
		try
	    {
			//Creates new socket with master node.
			server = new Socket(IP, portNum);
			PrintWriter outToServer = new PrintWriter(server.getOutputStream(), true);
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
			outToServer.println("CONNECT " + this.port);
			System.out.println("Sending connect to master");
			String serverLine = inFromServer.readLine();
			server.close();
			String[] ipAndPort = serverLine.split(" ")[1].split(":");
			String ip = ipAndPort[0];
			int port = Integer.parseInt(ipAndPort[1]);
			System.out.println(ip);
			System.out.println(port);
			//This is returned from server and is the client it told us we should connect to
			Socket clientToConnectTo = new Socket(ip, port);
			//Create new peer and fill in relevant info related to clientToConnectTo
			Peer peer = new Peer();
			peer.client = clientToConnectTo;
			peer.port = port;
			peer.reader = new BufferedReader(new InputStreamReader(clientToConnectTo.getInputStream()));
			peer.writer = new PrintWriter(clientToConnectTo.getOutputStream(), true);
			clients.add(peer);
			peer.writer.println("ATTACH");
			
	    } 
	    catch (UnknownHostException e) 
	    {
	      e.printStackTrace();
	      System.err.println("Can't find localhost.");
	      return false;
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return true;
		
	}
	
	private boolean processClient (Peer peer)
    {
    	/*
    	 * Returns true if the client connection is still okay
    	 */
    	

    	BufferedReader inFromClient = null;
    	String input = null;
    	
	    try
	    {
	      inFromClient = peer.reader;
	      
	    	if (inFromClient.ready ())
	    	{
	    		input = inFromClient.readLine ();
	    		System.out.println(input);
	    		Scanner scanner = new Scanner (input);
	    		String command = scanner.next();
	    		
	    		if (command.equalsIgnoreCase("END"))
	    		{
	    			  System.out.println("Peer disconnected");
	    			  peer.client.close();
	    		      clients.remove(peer);
	    		      return false;
	    		}
	    		
	    		if (command.equalsIgnoreCase("CONNECT"))
	    		{
	    			int port = Integer.parseInt(scanner.next());
	    			peer.port = port;
	    			
	    			Random rn = new Random();
	    			if (allClients.size() == 0) {
	    				peer.writer.println("CONNECT " + "localhost" + ":" + this.port);
	    			}
	    			else {
	    			int num = rn.nextInt(allClients.size());
	    			String clientIP = allClients.get(num).client.getInetAddress().getHostAddress();
	    			peer.writer.println("CONNECT " + clientIP + ":" + allClients.get(num).port);
	    			}
	    			clients.remove(peer);
	    			allClients.add(peer);
	    			return false;
	    		}
	    		//Gives peer information from other already connected peer.
	    		if (command.equalsIgnoreCase("ATTACH")) {
	    			for (String value : lists.keySet()) {
	    				List list = lists.get(value);
	    				UUID uuid = UUID.randomUUID();
	    				String uuidAsString = uuid.toString();
	    				peer.writer.println("UPDATE" + " " + uuidAsString  + " " + list.getID() + " " + list.getTitle());
	    				for (int j = 0; j < list.getArrayList().size(); j++) {
	    					uuid = UUID.randomUUID();
		    				uuidAsString = uuid.toString();
	    					peer.writer.println("UPDATEITEM" + " " + uuidAsString + " " + list.getID() + " " + j + " " + list.getArrayList().get(j));
	    					UUIDNotSend.put(uuidAsString, true);
	    				}
	    			}
	    			peer.writer.println("ATTACHOK");
	    		}
	    		//Check if message is currently exists in hashmap
	    		if (command.equalsIgnoreCase("UPDATE"))
	    		{
	    			System.out.println("I HAVE GOTTEN TO UPDATE");
	    			String msgUUID = scanner.next();
	    			String listID = scanner.next();
	    			String title = scanner.next();
	    			//In our client sending the list this is getting called.
	    			if (UUIDNotSend.containsKey(msgUUID)) {
	    				return true;
	    			}
	    			//Creates a new list and sets it in hashmap.
	    			if (lists.containsKey(listID)) {
	    				List list = lists.get(listID);
	    				list.setTitle(title);
	    				lists.put(listID, list);
	    			}
	    			//Creates a new list and sets it in hashmap.
	    			else {
	    			List list = new List();
	    			list.setTitle(title);
	    			list.setID(listID);
	    			lists.put(listID, list);
	    			System.out.println(lists.toString());
	    			}
	    			outgoingActions.add(input);
	    			UUIDNotSend.put(msgUUID, true);
	    			
	    		}
	    		if (command.equalsIgnoreCase("DELETE"))
	    		{
	    			String msgUUID = scanner.next();
	    			String listID = scanner.next();
	    			if (UUIDNotSend.containsKey(msgUUID)) {
	    				return true;
	    			}
	    			lists.remove(listID);
	    			outgoingActions.add(input);
	    			UUIDNotSend.put(msgUUID, true);
	    		}
	    		if (command.equalsIgnoreCase("UPDATEITEM")) {
	    			String msgUUID = scanner.next();
	    			String listID = scanner.next();
	    			String listIndex = scanner.next();
	    			int listIndexInt = Integer.parseInt(listIndex);
	    			String itemName = scanner.next();
	    			if (UUIDNotSend.containsKey(msgUUID)) {
	    				return true;
	    			}
	    			System.out.println("I am in UpdateItem");
	    			List list = lists.get(listID);
	    			if (list.getArrayList().size() == listIndexInt) {
	    				list.addItem(itemName);
	    			}
	    			else {
	    				list.changeItem(itemName, listIndexInt);
	    			}
	    			outgoingActions.add(input);
	    			UUIDNotSend.put(msgUUID, true);
	    		}
	    	}}catch (Exception e)
	    	{
	      e.printStackTrace ();
	      clients.remove(peer);
	      return false;
	    }
	    
	    return true;
    }
    
	
	public void run() {
		do {
			Socket client = getClientSocket(serverSocket);
			//Adds client to our peer array. They are "neighbors" in our graph.
			if (client != null)
        	{
				System.out.println("Got a peer");
        		Peer peer = new Peer();
        		
        		peer.client = client;
        		try {
					peer.reader = new BufferedReader (new InputStreamReader (peer.client.getInputStream ()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("Error creating BufferedReader");
				}
        		try {
					peer.writer = new PrintWriter (peer.client.getOutputStream (), true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Error creating PrintWriter");
				}
        		clients.add (peer);
        	}
			for (Peer oneClient : clients) {
			if(!oneClient.client.isConnected()){
				System.out.println("client is not connected");
			}
			}
        	
        	int i = 0;
        	//Change to iterate over outgoing actions instead of clients. For one iteration
        	//Send action to all clients. Then remove
        	while (i < outgoingActions.size()) {
        		if (sendMessages(outgoingActions.get(i)))
        			i++;
        	}
        	
        	i = 0;
        	while (i < clients.size ())
        	{
        		if (processClient (clients.get (i)))
        			i++;
        	}
        	
        	
        } while (true);
		}
	
}
