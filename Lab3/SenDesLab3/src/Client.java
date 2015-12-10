import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame implements Runnable{
	private static final long serialVersionUID = 1826871141583020576L;
	private String clientHost;	//host name for server
	private Socket connection; // connection to server
	private ObjectInputStream input; // input from server
	private ObjectOutputStream output;
	private final int port = 23555; 
	private JTextArea displayArea; // textfield to display conversation
	private JPanel userActionPanel;
	private JTextField messageField;
	private JButton send;
	
	public Client(String host){
		super("Messenger Client");
		clientHost = host; // set name of server
		
		//Create GUI
		setSize(300, 300);
		setVisible(true);
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		displayArea.setVisible(true);
		displayArea.setText("This is where conversation is displayed\n");
		add(new JScrollPane(displayArea), BorderLayout.CENTER);
		
		
		userActionPanel = new JPanel();
		userActionPanel.setLayout(new BorderLayout());
		userActionPanel.setVisible(true);
		add(userActionPanel, BorderLayout.SOUTH);
		
		messageField = new JTextField();
		messageField.setEditable(true);
		messageField.setText("Type Message Here");
		messageField.setVisible(true);
		userActionPanel.add(messageField);
		
		ActionListener sendMessageHandler = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				processMessageEncryption(messageField.getText());
			}
		};
		
		send = new JButton("Send");
		send.addActionListener(sendMessageHandler);
		send.setVisible(true);
		userActionPanel.add(send, BorderLayout.EAST);
		
		startClient();
	}
	
	// start the client thread
	public void startClient(){
		try{
			connectToServer();
			getStreams();
			handleConnection();
		}catch(Exception e){
			
		}finally{
			closeConnection();
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
		ExecutorService worker = Executors.newFixedThreadPool(1);
		worker.execute(this); // execute client
	}

	// control thread that allows continuous update of displayArea
	@Override
	public void run() {
		// continually keep the connection open and read messages from the server
		while(true){
			try {
				Object ob = input.readObject();
				// if there is input data from the server
				if(ob.getClass().equals(String.class)){
					processMessageDecryption(((String) ob).toString());
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch(NullPointerException e){
				e.printStackTrace();
			}
		}
	}
	
	private void processMessageEncryption(String message){
		//encryption of message that returns key
		//TO DO: ENCRYPTION
		
		//send encrypted message
		sendMessage(message);
	}
	
	private void sendMessage(String message){
		try {
			output.writeObject(message);
			output.flush();
			displayMessage("ME: " + message+"/n");
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	// process messsage recieved by client
	private void processMessageDecryption(String message){
		// decryption of key that returns message
		// TO DO: DECRYPTION
		
		//display decrypted message
		displayMessage("Other Client: " + message + "\n");
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
	private void closeConnection(){
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException io){
			
		}
	}
}
