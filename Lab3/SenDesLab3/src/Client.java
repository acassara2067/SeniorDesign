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
	
	private JTextArea displayArea;
	private JPanel userActionPanel;
	private JTextField messageField;
	private JButton send;
	
	public Client(String host){
		super("Messenger Client");
		clientHost = host;
		
		//Create GUI
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		displayArea.setVisible(true);
		add(new JScrollPane(displayArea));
		
		userActionPanel = new JPanel();
		userActionPanel.setVisible(true);
		add(userActionPanel, BorderLayout.SOUTH);
		
		messageField = new JTextField();
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
		// connect to server and get streams
		try{
			//make connection to server
			connection = new Socket(
				InetAddress.getByName(clientHost), 12345);
			
			// get streams for input and output
			input = new ObjectInputStream(connection.getInputStream());
			output = new ObjectOutputStream(connection.getOutputStream());	
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		
		// create and start worker thread for this client
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
					processMessageDecryption(((Scanner) ob).nextLine());
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
		} catch (IOException e) {
			e.printStackTrace();
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
}
