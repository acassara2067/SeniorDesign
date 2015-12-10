import javax.swing.JFrame;

public class ClientTest {
	public static void main(String args[]){
		Client application;
		
		if(args.length == 0){
			application = new Client("127.0.0.1"); // localhost
		}
		else{
			application = new Client(args[0]); // use args
		}
		application.setVisible(true);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
