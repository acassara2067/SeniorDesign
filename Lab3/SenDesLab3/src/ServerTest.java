import javax.swing.JFrame;

public class ServerTest {
	public static void main(String args[]){
		Server application = new Server();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.execute();
	}

	
	// when clients disconnect, server is displaying that they have connected
	// could be something to do with locks? or runnables?
}
