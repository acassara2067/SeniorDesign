import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Server extends JFrame{
	private static final long serialVersionUID = 1226255923201518073L;
	private JTextArea textDisplayArea; // output the flow of messages from one user to the other
	private User[] users; // array of Users
	//private int currentUser;
	private final static int USER_A = 0; // constant for the first user
	//private final static int USER_B = 1; // constant for the second user
	private final int PORT = 23557;
	private ServerSocket server; // server socket to connect with clients
	private ExecutorService runMessenger; // will run Users
	private Lock messengerLock; // to lock game for synchronization
	private Condition otherUserConnected; // to wait for other user's turn
	boolean keyPresent = false;
	PublicKey user0publicKey;
	
	public Server(){
		super("Messaging Server");
		// create ExecutorService with a thread for each client
		runMessenger = Executors.newFixedThreadPool(2);
		messengerLock = new ReentrantLock();	// create lock for messenger
		otherUserConnected = messengerLock.newCondition();  // condition variable for both clients being connected

		users = new User[2];
		//currentUser = USER_A;
	
		// create GUI
		textDisplayArea = new JTextArea();
		textDisplayArea.setEditable(false);
		//textDisplayArea.setText("Server awaiting connections\n");
		add(new JScrollPane( textDisplayArea ), BorderLayout.CENTER);
		setSize(500,500);
		setVisible(true);
		
		try{
			server = new ServerSocket(PORT, 2);
			displayMessage("Server awaiting connections\n");
			//System.out.print("Server waiting connections\n");
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void execute(){
		// wait for both clients to connect
		
		for(int i = 0; i < users.length; i++){
			displayMessage("waiting for user " + i + "\n");
			//System.out.print("waiting for user " + i + "\n");

			try{// wait for connection, create Client, start Runnable
				users[i] = new User(server.accept(), i);
				runMessenger.execute(users[i]);		// execute player runnable
			}
			catch(IOException ioException){
				//ioException.printStackTrace();
				//System.exit(1);
			}
		}
		
		messengerLock.lock(); // lock game to signal user A's thread
		
		try{
			users[USER_A].setSuspended(false); // resume user A
			otherUserConnected.signal(); // wake up user A's thread
		}
		finally{
			messengerLock.unlock(); // unlock game after signalling user A
		}
	}
	
	private void displayMessage(final String messageToDisplay){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					textDisplayArea.append(messageToDisplay);
				}
			}
		);
	}
	
	// private inner class User manages each User as a runnable
	private class User implements Runnable{
		private Socket connection; //connection to client
		private ObjectInputStream input; // input from client
		private ObjectOutputStream output; // output to client
		private int userNumber; // tracks which user this is
		private boolean suspended = true; // whether thread is suspended
		
		// set up User thread
		public User(Socket socket, int number){
			userNumber = number; // store this user's number
			connection = socket; // store socket for client
			
			try {
				output = new ObjectOutputStream(connection.getOutputStream());
				input = new ObjectInputStream(connection.getInputStream());	
				displayMessage("User " + userNumber + " connected\n" );
			} catch (Exception e) {
				//e.printStackTrace();
				//System.exit(1);
				System.err.println("\nError at connect streams: " + e + "\n");
			}
		}		

		// control thread's execution
		@Override
		public void run() {
			try{
			
				// if user A, wait for another user to arrive
				if(userNumber == USER_A){
					output.writeObject("SEVER: Connected: waiting for another User\n");
					output.flush(); // flush output
					//output.writeBoolean(false); //send boolean to tell client to not use keyListener yet
					//output.flush();
					
					messengerLock.lock(); // lock messenger to wait for second user
					
					try{
						while(suspended){
							otherUserConnected.await(); // wait for user B
						}
					}
					catch(InterruptedException e){
						e.printStackTrace();
					}
					finally{
						//before locking game, wait for pub key to be sent in
						displayMessage("attempting to read user 0 key...\n");
						keyPresent = false;
						while(!keyPresent){
							try {
								Object ob = input.readObject();
								if(ob instanceof PublicKey){
									displayMessage("Got user 0's Key: " + ob.toString()+"\n");
									keyPresent = true;
									users[1].output.writeObject(ob);
									users[1].output.flush();
								}
							} catch (ClassNotFoundException e) {
								displayMessage("scrimp");
								e.printStackTrace();
							}
						}
						
						messengerLock.unlock(); // unlock messenger after second user
					}
					
					// send message that the other user connected
					output.writeObject("SERVER: Other user connected. Begin conversation");
					output.flush();
				}
				else{
					output.writeObject("SERVER: both users connected: begin convo\n");
					output.flush();
					
					keyPresent=false;
					while(!keyPresent){
						try{
							Object ob = input.readObject();
							if(ob instanceof PublicKey){
								//displayMessage("got user 1's Key: " + ob.toString() + "\n");
								keyPresent = true;
								users[0].output.writeObject(ob);
								users[0].output.flush();
							}
						}
						catch(ClassNotFoundException e){
							
						}
					}
				}				
				
				// while messenger application is still running
				while(true){
					try{
						//if(input.available() != 0){
						Object ob = input.readObject();
						if(ob.getClass().equals(PublicKey.class)){
							System.out.print("fuck ya");
							//PublicKey key = (PublicKey)ob;
							//displayMessage("User " + userNumber + " sent their public key: " + key);
						}
						else if(ob.getClass().equals(String.class)){
							String mesg = ((String) ob).toString();
							//System.out.print("User " + userNumber + ":" + mesg +"\n");
							displayMessage("User " + userNumber + ":" + mesg +"\n");
							if(userNumber == 0){
								users[1].output.writeObject("CLIENT 1: " + mesg);
								users[1].output.flush();
							}
							else if(userNumber == 1){
								users[0].output.writeObject("CLIENT 2:" + mesg);
								users[0].output.flush();
							}
						}
					//}
					}
					catch(ClassNotFoundException e){
					//	e.printStackTrace();
					}
					catch(IOException e){
					//	e.printStackTrace();
					}
//					finally{
//						try{
//							connection.close();
//						}
//						catch(IOException e){
//						//	e.printStackTrace();
//						}
//					}
				}
			}
			catch (IOException e) {
					// TODO Auto-generated catch block
			//		e.printStackTrace();
			}
		}
		
		// set whether or not thread is suspended
		public void setSuspended(boolean suspended) {
			this.suspended = suspended;
		}	
	}
}
