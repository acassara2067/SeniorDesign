import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
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
		ExecutorService worker = Executors.newFixedThreadPool(1);
		worker.execute(this); // execute client
	}

	// control thread that allows continuous update of displayArea
	@Override
	public void run() {
		//Send public key
		try {
			output.writeObject(cryption.getPublicKey());
			output.flush();
		} catch (IOException e1) {
		}
		
		// continually keep the connection open and read messages from the server
		while(true){
			try {
				// if there is input data from the server
				Object ob = input.readObject();
				
				//input is class String
				if(ob.getClass().equals(String.class)){
					displayMessage(((String) ob).toString() + "\n");
				}
				else if(ob instanceof byte[]){
					processMessageDecryption((byte[]) ob);
				}
				else if(ob instanceof PublicKey){
					try {
						cryption.setEncryptionKey((PublicKey)ob);
					} 
					catch (GeneralSecurityException e) {

					}
				}
				else{
					displayMessage("err: " +ob.toString());
				}				
			} 
			catch (ClassNotFoundException e) {

			} 
			catch (IOException e) {
			} 
			catch(NullPointerException e){

			}
		}
	}
	
	private void processMessageEncryption(String message){
		//encryption of message that returns key
		displayMessage("Encrypting message: " + message +"\n");
		//send encrypted message
		
		try{
			byte[] messageByte = cryption.encrypt(message);
			displayMessage("ME (encrypted): " + messageByte.toString() + "\n\n");
			output.writeObject(cryption.encrypt(message));
			output.flush();
		}
		catch(Exception e){
			
		}
		
	}
	
	// process messsage recieved by client
	private void processMessageDecryption(byte[] messageByte){
		// decryption of key that returns message
		displayMessage("decrypting message: " + messageByte.toString() + "\n");
		displayMessage("OTHER CLIENT (decrypted): " + cryption.decrypt(messageByte) + "\n\n");
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
		/* possibly use to prevent client from sending messages before other client connects
		 * but it still sends the messages as soon as they connect which isn't the worst option
		public void activate(){
			listening = true;
		}
		public void deactivate(){
			listening = false;
		}
		*/
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
