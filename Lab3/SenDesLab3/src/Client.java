import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame implements Runnable{
	private static final long serialVersionUID = 2542084860592908935L;
	private Cryption cryption;
	private String clientHost;	//host name for server
	private Socket connection; // connection to server
	private ObjectInputStream input; // input from server
	private ObjectOutputStream output;
	private MyKeyListener handler;
	private final int port = 23557; 
	private JTextArea displayArea; // textfield to display conversation
	private JTextField messageField;
	
	public Client(String host){
		super("Messenger Client");
		clientHost = host; // set name of server
		
		messageField = new JTextField("Enter message here.");
		messageField.setEditable(true);
		
		handler = new MyKeyListener();
		messageField.addKeyListener(handler);
		add(messageField, BorderLayout.SOUTH);
		
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		add(new JScrollPane(displayArea), BorderLayout.CENTER);
		
		setSize(450,200);
		setVisible(true);
	}
	
	// start the client thread
	public void startClient(){
		try{
			connectToServer();
			getStreams();
			handleConnection();
		}catch(Exception e){
			
		}finally{
			//closeConnection();
		}
	}
	public void connectToServer() throws IOException{
		InetAddress ipAddress = InetAddress.getByName(clientHost);
		connection = new Socket(ipAddress,port);
	}
	public void getStreams()throws IOException{
		input = new ObjectInputStream(connection.getInputStream());
		output = new ObjectOutputStream(connection.getOutputStream());
		
	}
	public void handleConnection(){
		
		cryption = new Cryption(); //generate keySet for user
		displayMessage("Cryprion created\nRunning client thread\n");
		ExecutorService worker = Executors.newFixedThreadPool(1);
		worker.execute(this); // execute client
	}

	// control thread that allows continuous update of displayArea
	@Override
	public void run() {
		//displayMessage("my public key: " + cryption.getPublicKey().toString() + "\n");
		//displayMessage("my private key: " + cryption.getPrivateKey().toString() + "\n");
		//displayMessage("Trying to send key:\n");
		try {
			output.writeObject(cryption.getPublicKey());
			output.flush();
			displayMessage("\tsent public key: " + cryption.getPublicKey() + "\n");
		} catch (IOException e1) {
			displayMessage("public key NOT sent\n");
		}
		//displayMessage("Done trying\n");

		
		// continually keep the connection open and read messages from the server
		while(true){
			try {
				Object ob = input.readObject();
				// if there is input data from the server
				if(ob.getClass().equals(String.class)){
					processMessageDecryption(((String) ob).toString());
				}
				else if(ob.getClass().equals(Byte[].class)){
					
				}
				else if(ob instanceof PublicKey){
					try {
						cryption.setEncryptionKey((PublicKey)ob);
						displayMessage("got others public key: " + cryption.getEncryptionKey());

					} catch (GeneralSecurityException e) {

					}
				}
//				else if(ob.getClass().equals(boolean.class)){
//					displayMessage("frog");
//					if((boolean)ob.equals(false)){
//						handler.deactivate();
//					}
//					else if((boolean)ob.equals(true)){
//						handler.activate();
//					}
//					if((boolean)ob.equals(true)){
//						displayMessage("Server need pub key");
//					}
//					else{
//						displayMessage("wut");
//					}
//				}
				
			} catch (ClassNotFoundException e) {
			//	e.printStackTrace();
			} catch (IOException e) {
			//	e.printStackTrace();
			} catch(NullPointerException e){
			//	e.printStackTrace();
			}
		}
	}
	
	private void processMessageEncryption(String message){
		//encryption of message that returns key
		//TO DO: ENCRYPTION
		//displayMessage("ME: " + message +"\n");

		//byte[] messageByte = cryption.encrypt(message);
		
		//send encrypted message
		sendMessage(message);
	}
	
	private void sendMessage(String message){
		try {
			output.writeObject(message);
			output.flush();
			//System.out.print("send to server successful");
		} catch (Exception e) {
			displayMessage("");
			System.out.print("send unsuccessful\nerror: " + e);
			
			//e.printStackTrace();
		}
	}
	
	// process messsage recieved by client
	private void processMessageDecryption(String message){
		// decryption of key that returns message
		// TO DO: DECRYPTION
		
		//display decrypted message
		displayMessage(message + "\n");
	}
	
	/**
	 * 
	 * @param messageToDisplay
	 */
	private void displayMessage(final String messageToDisplay){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					displayArea.append(messageToDisplay);
				}
			}
		);
	}
//	private void closeConnection(){
//		try{
//			output.close();
//			input.close();
//			connection.close();
//		}catch(IOException io){
//			
//		}
//	}
	
	private class MyKeyListener implements KeyListener {
		private boolean listening = true;
		public void activate(){
			listening = true;
		}
		public void deactivate(){
			listening = false;
		}
		@Override
		public void keyPressed(KeyEvent e) {
			if(listening){
				if(e.getKeyCode() == KeyEvent.VK_ENTER && !messageField.getText().equals("")){
					processMessageEncryption(messageField.getText());
					messageField.setText("");
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {}

		@Override
		public void keyTyped(KeyEvent arg0) {}
		
	}
}
