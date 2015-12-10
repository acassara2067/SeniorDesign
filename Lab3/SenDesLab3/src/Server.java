import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
	private int currentUser;
	private final static int USER_A = 0; // constant for the first user
	private final static int USER_B = 1; // constant for the second user
	private final int PORT = 23555;
	private ServerSocket server; // server socket to connect with clients
	private ExecutorService runMessenger; // will run Users
	private Lock messengerLock; // to lock game for synchronization
	private Condition otherUserConnected; // to wait for other user's turn
	
	public Server(){
		super("Messaging Server");
		// create ExecutorService with a thread for each client
		runMessenger = Executors.newFixedThreadPool(2);
		messengerLock = new ReentrantLock();	// create lock for messenger
		otherUserConnected = messengerLock.newCondition();  // condition variable for both clients being connected

		users = new User[2];
		currentUser = USER_A;
	
		// create GUI
		textDisplayArea = new JTextArea();
		textDisplayArea.setEditable(false);
		textDisplayArea.setText("Server awaiting connections\n");
		add(new JScrollPane( textDisplayArea ), BorderLayout.CENTER);
		setSize(500,500);
		setVisible(true);
		
		try{
			server = new ServerSocket(PORT, 2);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void execute(){
		//server = new ServerSocket(PORT, 2);
		// wait for both clients to connect
		for(int i = 0; i < users.length; i++){
			try{// wait for connection, create Client, start Runnable
				users[i] = new User(server.accept(), i);
				
				runMessenger.execute(users[i]);		// execute player runnable
			}
			catch(IOException ioException){
				ioException.printStackTrace();
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
	
	// privet inner class User manages each User as a runnable
	private class User implements Runnable{
		private Socket connection; //connection to client
		private ObjectInputStream input;
		private ObjectOutputStream output;
		private int userNumber; // tracks which user this is
		private boolean suspended = true; // whether thread is suspended
		
		// set up User thread
		public User(Socket socket, int number){
			userNumber = number; // store this user's number
			connection = socket; // store socket for client
			
			try {
				input = new ObjectInputStream(connection.getInputStream());
				output = new ObjectOutputStream(connection.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		

		// control thread's execution
		@Override
		public void run() {
			try{
				displayMessage("User " + userNumber + " connected\n");
			
				// if user A, wait for another user to arrive
				if(userNumber == USER_A){
					output.writeObject("User A connected\nWaiting for another User\n");
					output.flush(); // flush output
					
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
						messengerLock.unlock(); // unlock messenger after second user
					}
					
					// send message that the other user connected
					output.writeObject("Other user connected. Begin conversation");
					output.flush();
				}
				else{
					output.writeObject("User B connected, please wait\n");
					output.flush();
				}
				
				// while messenger application is still running
				while(true){
					try{
						Object ob = input.readObject();
						if(ob.getClass().equals(String.class)){
							String mesg = ((String) ob).toString();
							displayMessage("\nmessage: " + ob);
							output.writeObject(ob);
							output.flush();
						}
					}
					catch(ClassNotFoundException e){
						e.printStackTrace();
					}
					catch(IOException e){
						e.printStackTrace();
					}
					finally{
						try{
							connection.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}
			catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}
		
		// set whether or not thread is suspended
		public void setSuspended(boolean suspended) {
			this.suspended = suspended;
		}	
	}
}
